package io.github.alecredmond.internal.method.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ConditionalTable;
import io.github.alecredmond.export.application.probabilitytables.MarginalTable;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.VectorCombinationKey;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.ProbabilityVectorIterator;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.VectorCombinationKeyFactory;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TableUtils {
  private static final ProbabilityVectorIterator ITERATOR = new ProbabilityVectorIterator();
  private static final VectorCombinationKeyFactory KEY_FACTORY = new VectorCombinationKeyFactory();

  private TableUtils() {}

  public static double getProbability(Collection<NodeState> states, ProbabilityTable table) {
    ProbabilityVector vector = table.getVector();
    int[] stepMultiplier = vector.getStepMultiplier();
    int index = 0;
    for (NodeState state : states) {
      int stateValue = vector.getStateValueMap().get(state);
      int nodeIndex = vector.getNodeIndexMap().get(state.getNode());
      index += stepMultiplier[nodeIndex] * stateValue;
    }
    return vector.getProbabilities()[index];
  }

  public static double sumProbabilities(Map<Node, NodeState> request, ProbabilityTable table) {
    List<NodeState> matchedRequest = matchRequestToTable(request, table);
    VectorCombinationKey comboKey = KEY_FACTORY.buildKey(table, matchedRequest);
    return sumProbabilities(comboKey, table);
  }

  private static List<NodeState> matchRequestToTable(
      Map<Node, NodeState> request, ProbabilityTable table) {
    return request.entrySet().stream()
        .filter(entry -> table.getNodes().contains(entry.getKey()))
        .map(Map.Entry::getValue)
        .toList();
  }

  public static double sumProbabilities(VectorCombinationKey comboKey, ProbabilityTable table) {
    ProbabilityVector vector = table.getVector();
    double[] probability = vector.getProbabilities();
    DoubleAdder adder = new DoubleAdder();
    ITERATOR.iterateEvents(vector, comboKey, (key, index) -> adder.add(probability[index]));
    double sum = adder.sum();
    if (Double.isNaN(sum)) {
      throwQueryError(
          "matched probabilities summing to NaN", comboKey.getRequest().values(), table);
    }
    return sum;
  }

  private static void throwQueryError(
      String endMessage, Collection<NodeState> request, ProbabilityTable table) {
    StringBuilder requestString = new StringBuilder();
    request.forEach(ns -> requestString.append(ns.getId().toString()).append(" "));
    throw new IllegalArgumentException(
        String.format(
            "Request %s to table %s %s", requestString, table.getTableName(), endMessage));
  }

  public static Map<NodeState, Double> buildMarginalMap(MarginalTable marginalTable) {
    return setToStateMap(buildProbabilityMap(marginalTable));
  }

  private static Map<NodeState, Double> setToStateMap(Map<Set<NodeState>, Double> setMap) {
    Map<NodeState, Double> map = new LinkedHashMap<>();
    setMap.forEach((set, prob) -> map.put(set.stream().findFirst().orElseThrow(), prob));
    return map;
  }

  public static Map<Set<NodeState>, Double> buildProbabilityMap(ProbabilityTable table) {
    return buildProbabilityMapInclusive(table, new ArrayList<>(), table.getNodes());
  }

  public static Map<Set<NodeState>, Double> buildProbabilityMapInclusive(
      ProbabilityTable table, Collection<NodeState> includedStates, Set<Node> includedNodes) {
    ProbabilityVector vector = table.getVector();
    double[] probabilities = vector.getProbabilities();
    Node[] nodeArray = vector.getNodeArray();

    Map<Set<NodeState>, Double> map = new LinkedHashMap<>();
    ITERATOR.iterateEvents(
        includedStates,
        table,
        (key, index) -> {
          Set<NodeState> stateSet = keyToStates(includedNodes, LinkedHashSet::new, key, nodeArray);
          double prob = probabilities[index];
          map.put(stateSet, prob);
        });
    return map;
  }

  private static <T extends Collection<NodeState>, R extends T> R keyToStates(
      Set<Node> includedNodes, Supplier<R> supplier, int[] k, Node[] nodeArray) {
    return IntStream.range(0, k.length)
        .mapToObj(i -> nodeArray[i].getNodeStates().get(k[i]))
        .filter(state -> includedNodes.contains(state.getNode()))
        .collect(Collectors.toCollection(supplier));
  }

  public static <T extends Serializable> Collection<NodeState> convertIDsToStates(
      Collection<T> ids, ProbabilityTable table) {
    Map<Serializable, NodeState> stateIdMap = table.getNodeStateIDMap();
    return ids.stream().filter(stateIdMap::containsKey).map(stateIdMap::get).distinct().toList();
  }

  public static Map<NodeState, Double> getMapForConditions(
      Collection<NodeState> conditionStates, ConditionalTable conditionalTable) {
    Set<Node> conditions = conditionalTable.getConditions();
    boolean allConditionsIncluded =
        conditionStates.stream()
            .map(NodeState::getNode)
            .collect(Collectors.toSet())
            .equals(conditions);
    if (!allConditionsIncluded) {
      return new HashMap<>();
    }
    return setToStateMap(
        buildProbabilityMapInclusive(
            conditionalTable, conditionStates, conditionalTable.getEvents()));
  }

  public static void marginalizeJointTable(ProbabilityTable table) {
    double[] probabilities = table.getVector().getProbabilities();
    double tableSum = Arrays.stream(probabilities).sum();
    double ratio = tableSum == 0.0 ? 0.0 : 1 / tableSum;
    IntStream.range(0, probabilities.length)
        .forEach(i -> probabilities[i] = ratio * probabilities[i]);
  }

  public static <T extends Collection<NodeState>, R extends T> List<T> generateStateCombinations(
      Set<Node> includedNodes, Supplier<R> supplier, ProbabilityTable table) {
    if (includedNodes.isEmpty()) {
      return new ArrayList<>();
    }
    List<T> combos = new ArrayList<>();
    Node[] nodeArray = table.getVector().getNodeArray();

    ITERATOR.iterateEvents(
        lockExcludedNodesFirstState(includedNodes, nodeArray),
        table,
        (k, index) -> combos.add(keyToStates(includedNodes, supplier, k, nodeArray)));

    return combos;
  }

  private static List<NodeState> lockExcludedNodesFirstState(
      Set<Node> includedNodes, Node[] nodes) {
    return Arrays.stream(nodes)
        .filter(n -> !includedNodes.contains(n))
        .map(n -> n.getNodeStates().getFirst())
        .toList();
  }

    public static void confirmAllNodesQueried(Collection<NodeState> request, ProbabilityTable table) {
    Set<Node> nodeSet = new HashSet<>(table.getNodes());
    boolean duplicateNodes;
    for (NodeState state : request) {
      duplicateNodes = !nodeSet.remove(state.getNode());
      if (duplicateNodes) {
        throwQueryError("contained duplicate nodes", request, table);
      }
    }
    boolean allNodesQueried = nodeSet.isEmpty();
    if (!allNodesQueried) {
      throwQueryError("did not query all nodes", request, table);
    }
  }

  public static Set<Node> getCommonNodes(ProbabilityTable tableA, ProbabilityTable tableB) {
    return NodeUtils.getOverlap(tableA.getNodes(), tableB.getNodes());
  }
}
