package io.github.alecredmond.method.probabilitytables;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.export.ConditionalTable;
import io.github.alecredmond.application.probabilitytables.export.ProbabilityTable;
import io.github.alecredmond.application.probabilitytables.export.probabilityvector.ProbabilityVector;
import io.github.alecredmond.application.probabilitytables.internal.probabilityvector.VectorCombinationKey;
import io.github.alecredmond.method.probabilitytables.probabilityvector.ProbabilityVectorIterator;
import io.github.alecredmond.method.probabilitytables.probabilityvector.VectorCombinationKeyFactory;
import java.util.*;
import java.util.concurrent.atomic.DoubleAdder;
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

  public static double sumProbabilities(VectorCombinationKey comboKey, ProbabilityTable table) {
    ProbabilityVector vector = table.getVector();
    double[] probability = vector.getProbabilities();
    DoubleAdder adder = new DoubleAdder();
    ITERATOR.iterateKeyCombos(vector, comboKey, (key, index) -> adder.add(probability[index]));
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
        String.format("Request %s to table %s %s", requestString, table.getTableID(), endMessage));
  }

  public static void marginalizeJointTable(ProbabilityTable table) {
    double[] probabilities = table.getVector().getProbabilities();
    double tableSum = Arrays.stream(probabilities).sum();
    double ratio = tableSum == 0.0 ? 0.0 : 1 / tableSum;
    IntStream.range(0, probabilities.length)
        .forEach(i -> probabilities[i] = ratio * probabilities[i]);
  }

  public static List<Set<NodeState>> generateStateCombinations(
      Set<Node> nodes, ProbabilityTable table) {
    return generateStateCombinations(new ArrayList<>(), nodes, table);
  }

  private static List<Set<NodeState>> generateStateCombinations(
      Collection<NodeState> lockedStates, Set<Node> includedNodes, ProbabilityTable table) {
    if (includedNodes.isEmpty()) {
      return new ArrayList<>();
    }
    List<Set<NodeState>> combos = new ArrayList<>();
    Node[] nodes = table.getVector().getNodeArray();

    List<NodeState> states = new ArrayList<>(lockedStates);

    lockExcludedNodes(includedNodes, nodes, states);

    ITERATOR.iterateKeyCombos(
        states,
        table,
        (k, index) ->
            combos.add(
                IntStream.range(0, k.length)
                    .mapToObj(i -> nodes[i].getNodeStates().get(k[i]))
                    .filter(state -> includedNodes.contains(state.getNode()))
                    .collect(Collectors.toCollection(LinkedHashSet::new))));

    return combos;
  }

  private static void lockExcludedNodes(
      Set<Node> includedNodes, Node[] nodes, List<NodeState> states) {
    Arrays.stream(nodes)
        .filter(n -> !includedNodes.contains(n))
        .map(n -> n.getNodeStates().getFirst())
        .forEach(states::add);
  }

  public static void marginalizeConditionalTable(ConditionalTable table) {
    ProbabilityVector vector = table.getVector();
    VectorCombinationKey marginalizationKey =
        new VectorCombinationKeyFactory().buildMarginalisationKey(table);

    int[] tumblerKey = marginalizationKey.getStateKey().clone();
    boolean[] lockConditions = marginalizationKey.getInnerLock();
    boolean[] lockEvents = marginalizationKey.getOuterLock();

    double[] probs = vector.getProbabilities();

    DoubleAdder adder = new DoubleAdder();

    ITERATOR.iterateKeyCombos(
        vector,
        tumblerKey,
        lockEvents,
        (conditionKey, conditionIndex) -> {
          ITERATOR.iterateKeyCombos(
              vector,
              conditionKey,
              lockConditions,
              (eventKey, eventIndex) -> adder.add(probs[eventIndex]));
          double sumAcrossConditions = adder.sumThenReset();
          if (sumAcrossConditions == 0) return;
          double ratio = 1 / sumAcrossConditions;
          ITERATOR.iterateKeyCombos(
              vector,
              conditionKey,
              lockConditions,
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
}
