package io.github.alecredmond.internal.method.probabilitytables;

import io.github.alecredmond.exceptions.ProbabilityTableRequestException;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.StateCombinationGenerator;
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
      int stateValue = vector.getStateValueMap().get(state);
      int nodeIndex = vector.getNodeIndexMap().get(state.getNode());
      index += stepMultiplier[nodeIndex] * stateValue;
    }
    return index;
  }

  public static Collection<NodeState> assertAllIdsPresent(
      Collection<Serializable> states, Set<Node> expected, ProbabilityTable table) {
    return assertAllNodesPresent(convertIdsToStates(states, table), expected);
  }

  public static Collection<NodeState> assertAllNodesPresent(
      Collection<NodeState> states, Set<Node> allNodes) {
    Map<Node, NodeState> request = NodeUtils.generateRequest(states);
    if (request.keySet().equals(allNodes)) return states;
    throw new ProbabilityTableRequestException(
        "request %s does not contain all nodes requested %s"
            .formatted(
                NodeUtils.formatStatesToString(request.values()),
                NodeUtils.formatNodesToString(allNodes)));
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

  public static Collection<NodeState> assertAllIdsPresent(
      Collection<Serializable> states, ProbabilityTable table) {
    return assertAllNodesPresent(convertIdsToStates(states, table), table);
  }

  public static Collection<NodeState> assertAllNodesPresent(
      Collection<NodeState> states, ProbabilityTable table) {
    return assertAllNodesPresent(states, table.getNodes());
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

  private static void throwQueryError(
      String endMessage, Collection<NodeState> request, ProbabilityTable table) {
    StringBuilder requestString = new StringBuilder();
    request.forEach(ns -> requestString.append(ns.getId().toString()).append(" "));
    throw new IllegalArgumentException(
        String.format(
            "Request %s to table %s %s", requestString, table.getTableName(), endMessage));
  }

  public static Set<Node> getCommonNodes(ProbabilityTable tableA, ProbabilityTable tableB) {
    return NodeUtils.getOverlap(tableA.getNodes(), tableB.getNodes());
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
