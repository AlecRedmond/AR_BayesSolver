package io.github.alecredmond.internal.method.probabilitytables;

import static io.github.alecredmond.internal.method.node.NodeUtils.formatIDsToString;

import io.github.alecredmond.exceptions.ProbabilityTableRequestException;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ConditionalTable;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.application.probabilitytables.base.SingleEventTable;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.vectoriterator.misciterators.StateCombinationGenerator;
import java.io.Serializable;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TableUtils {

  private TableUtils() {}

  public static double getProbability(Collection<NodeState> states, ProbabilityTable table) {
    return table.getProbabilities()[getIndex(states, table)];
  }

  public static int getIndex(Collection<NodeState> states, ProbabilityTable table) {
    ProbabilityVector vector = table.getVector();
    int[] strideLengths = vector.getStrideLengths();
    int index = 0;
    for (NodeState state : states) {
      int stateValue = vector.getStateValueMap().getOrDefault(state, 0);
      int nodeIndex = vector.getNodeIndexMap().getOrDefault(state.getNode(), 0);
      index += strideLengths[nodeIndex] * stateValue;
    }
    return index;
  }

  public static <S extends Serializable> Collection<NodeState> assertAllIdsPresent(
      Collection<S> stateIds, Set<Node> expected, ProbabilityTable table) {
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

  public static <S extends Serializable> List<NodeState> convertIdsToStates(
      Collection<S> ids, ProbabilityTable table) {
    Map<Serializable, NodeState> idMap = table.getNodeStateIDMap();
    List<Serializable> missing = new ArrayList<>();
    List<NodeState> states = new ArrayList<>();
    ids.forEach(
        id ->
            Optional.ofNullable(idMap.get(id)).ifPresentOrElse(states::add, () -> missing.add(id)));
    if (missing.isEmpty()) return states;
    throw new ProbabilityTableRequestException(
        "IDs %s not found in table %s!".formatted(formatIDsToString(missing), table));
  }

  public static void marginalizeJointTable(ProbabilityTable table) {
    double[] probabilities = table.getProbabilities();
    double tableSum = Arrays.stream(probabilities).sum();
    double ratio = tableSum == 0.0 ? 0.0 : 1 / tableSum;
    for (int i = 0; i < probabilities.length; i++) {
      probabilities[i] = ratio * probabilities[i];
    }
  }

  public static <T extends Collection<NodeState>, R extends T> List<T> generateStateCombinations(
      Set<Node> includedNodes, Supplier<R> supplier, ProbabilityTable table) {
    if (includedNodes.isEmpty()) return new ArrayList<>();
    return new StateCombinationGenerator(table).generateCombos(includedNodes, supplier);
  }

  public static Set<Node> getCommonNodes(ProbabilityTable tableA, ProbabilityTable tableB) {
    return NodeUtils.getOverlap(tableA.getNodes(), tableB.getNodes());
  }

  public static Map<NodeState, Double> buildConditionalProbMap(
      Collection<NodeState> conditionStates, ConditionalTable table) {
    Map<NodeState, Double> map = new LinkedHashMap<>();
    List<NodeState> events = table.getNetworkNode().getNodeStates();
    double[] probs = table.getProbabilities();
    int firstIndex = getIndex(conditionStates, table);
    IntStream.range(0, events.size()).forEach(i -> map.put(events.get(i), probs[firstIndex + i]));
    return map;
  }

  public static Map<NodeState, Double> buildMarginalProbMap(ProbabilityTable table) {
    if (!(table instanceof SingleEventTable<?> singleEventTable)) return new HashMap<>();
    List<NodeState> states = singleEventTable.getEventNode().getNodeStates();
    double[] prob = table.getProbabilities();
    Map<NodeState, Double> map = new LinkedHashMap<>();
    for (int i = 0; i < prob.length; i++) {
      map.put(states.get(i), prob[i]);
    }
    return map;
  }

  public static String buildTableName(
      List<Serializable> eventIds, List<Serializable> conditionIds) {
    StringBuilder sb = new StringBuilder("P(");
    sb.append(formatIDsToString(eventIds));
    if (!conditionIds.isEmpty()) {
      sb.append("|");
      sb.append(formatIDsToString(conditionIds));
    }
    return sb.append(")").toString();
  }
}
