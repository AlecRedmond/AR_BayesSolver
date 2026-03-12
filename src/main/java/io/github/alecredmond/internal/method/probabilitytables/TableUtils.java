package io.github.alecredmond.internal.method.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ConditionalTable;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.application.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.VectorCombinationKey;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.ProbabilityVectorIterator;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.VectorCombinationKeyFactory;

import java.util.*;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TableUtils {
  private static final ProbabilityVectorIterator ITERATOR = new ProbabilityVectorIterator();

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
    VectorCombinationKey comboKey =
        new VectorCombinationKeyFactory().buildKey(table, matchedRequest);
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

  public static double sumAll(JunctionTreeTable junctionTreeTable) {
    return Arrays.stream(junctionTreeTable.getVector().getProbabilities()).sum();
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
        (k, index) ->
            combos.add(
                IntStream.range(0, k.length)
                    .mapToObj(i -> nodeArray[i].getNodeStates().get(k[i]))
                    .filter(state -> includedNodes.contains(state.getNode()))
                    .collect(Collectors.toCollection(supplier))));

    return combos;
  }

  private static List<NodeState> lockExcludedNodesFirstState(
      Set<Node> includedNodes, Node[] nodes) {
    return Arrays.stream(nodes)
        .filter(n -> !includedNodes.contains(n))
        .map(n -> n.getNodeStates().getFirst())
        .toList();
  }

  public static void marginalizeConditionalTable(ConditionalTable table) {
    ProbabilityVector vector = table.getVector();
    VectorCombinationKey marginalizationKey =
        new VectorCombinationKeyFactory().buildMarginalisationKey(table);

    double[] probs = vector.getProbabilities();

    DoubleAdder adder = new DoubleAdder();

    ITERATOR.iterateConditions(
        vector,
        marginalizationKey,
        (conditionKey, conditionIndex) -> {
          ITERATOR.iterateEvents(
              vector, marginalizationKey, (eventKey, eventIndex) -> adder.add(probs[eventIndex]));
          double sumAcrossConditions = adder.sumThenReset();
          if (sumAcrossConditions == 0) return;
          double ratio = 1 / sumAcrossConditions;
          ITERATOR.iterateEvents(
              vector,
              marginalizationKey,
              (eventKey, eventIndex) -> probs[eventIndex] = ratio * probs[eventIndex]);
        });
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
