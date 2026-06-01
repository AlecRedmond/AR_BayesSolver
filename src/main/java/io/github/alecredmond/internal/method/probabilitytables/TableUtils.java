package io.github.alecredmond.internal.method.probabilitytables;

import io.github.alecredmond.exceptions.ProbabilityTableRequestException;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ConditionalTable;
import io.github.alecredmond.export.application.probabilitytables.MarginalTable;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.vectoriterator.misciterators.StateCombinationGenerator;
import java.io.Serializable;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TableUtils {

  private TableUtils() {}

  public static double getProbability(Collection<NodeState> states, ProbabilityTable table) {
    return table.getVector().getProbabilities()[getIndex(states, table)];
  }

  public static int getIndex(Collection<NodeState> states, ProbabilityTable table) {
    ProbabilityVector vector = table.getVector();
    int[] stepMultiplier = vector.getStepMultiplier();
    int index = 0;
    for (NodeState state : states) {
      int stateValue = vector.getStateValueMap().getOrDefault(state, 0);
      int nodeIndex = vector.getNodeIndexMap().getOrDefault(state.getNode(), 0);
      index += stepMultiplier[nodeIndex] * stateValue;
    }
    return index;
  }

  public static Collection<NodeState> assertAllIdsPresent(
      Collection<Serializable> stateIds, Set<Node> expected, ProbabilityTable table) {
    return assertAllNodesPresent(convertIdsToStates(stateIds, table), expected);
  }

  public static Collection<NodeState> assertAllNodesPresent(
      Collection<NodeState> states, Set<Node> allNodes) {
    Map<Node, NodeState> request = NodeUtils.generateRequest(states);
    if (request.keySet().containsAll(allNodes)) return states;
    throw new ProbabilityTableRequestException(
        "request %s does not contain all nodes requested %s"
            .formatted(
                NodeUtils.formatStatesToString(states), NodeUtils.formatNodesToString(allNodes)));
  }

  public static List<NodeState> convertIdsToStates(
      Collection<Serializable> ids, ProbabilityTable table) {
    Map<Serializable, NodeState> idMap = table.getNodeStateIDMap();
    List<Serializable> missing = new ArrayList<>();
    List<NodeState> states = new ArrayList<>();
    ids.forEach(
        id ->
            Optional.ofNullable(idMap.get(id)).ifPresentOrElse(states::add, () -> missing.add(id)));
    if (missing.isEmpty()) return states;
    throw new ProbabilityTableRequestException(
        "IDs %s not found in table %s!".formatted(NodeUtils.formatIDsToString(missing), table));
  }

  public static <T extends ProbabilityTable> void setProbability(
      Collection<NodeState> states, double probability, T table) {
    double[] probs = table.getVector().getProbabilities();
    int index = getIndex(states, table);
    probs[index] = probability;
    if (probability != 1.0) return;
    setComplementStatesToZero(states, table);
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
    return new StateCombinationGenerator(table).generateCombos(includedNodes, supplier);
  }

  public static Set<Node> getCommonNodes(ProbabilityTable tableA, ProbabilityTable tableB) {
    return NodeUtils.getOverlap(tableA.getNodes(), tableB.getNodes());
  }

  public static Map<NodeState, Double> buildConditionalProbMap(
      Collection<NodeState> conditionStates, ConditionalTable table) {
    Map<NodeState, Double> map = new LinkedHashMap<>();
    List<NodeState> events = table.getNetworkNode().getNodeStates();
    double[] probs = table.getVector().getProbabilities();
    int firstIndex = getIndex(conditionStates, table);
    IntStream.range(0, events.size()).forEach(i -> map.put(events.get(i), probs[firstIndex + i]));
    return map;
  }

  public static Map<NodeState, Double> buildMarginalProbMap(MarginalTable table) {
    List<NodeState> states = table.getNetworkNode().getNodeStates();
    double[] prob = table.getVector().getProbabilities();
    return IntStream.range(0, prob.length)
        .mapToObj(i -> Map.entry(states.get(i), prob[i]))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static <T extends ProbabilityTable> void setComplementStatesToZero(
      Collection<NodeState> states, T table) {
    if (table.getEvents().size() > 1) {
      throw new IllegalStateException(
          "Should not be more than 1 event node in a table! Table: %s"
              .formatted(table.getTableName()));
    }
    Map<Node, NodeState> request = NodeUtils.generateRequest(states);

    Set<NodeState> conditions =
        request.values().stream()
            .filter(s -> table.getConditions().contains(s.getNode()))
            .collect(Collectors.toCollection(HashSet::new));

    states.stream()
        .map(NodeState::getNode)
        .filter(table.getEvents()::contains)
        .flatMap(n -> n.getNodeStates().stream())
        .filter(ns -> !request.containsValue(ns))
        .forEach(
            comp -> {
              conditions.add(comp);
              setProbability(conditions, 0.0, table);
              conditions.remove(comp);
            });
  }
}
