package io.github.alecredmond.method.probabilitytables;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.application.probabilitytables.probabilityvector.VectorCombinationKey;
import io.github.alecredmond.method.probabilitytables.probabilityvector.ProbabilityVectorIterator;
import io.github.alecredmond.method.probabilitytables.probabilityvector.VectorCombinationKeyFactory;
import java.util.*;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TableUtils {
  private final ProbabilityVectorIterator iterator;
  private final ProbabilityTable table;
  private final VectorCombinationKey marginalisationKey;

  public TableUtils(ProbabilityTable table) {
    this.table = table;
    this.iterator = new ProbabilityVectorIterator();
    this.marginalisationKey = new VectorCombinationKeyFactory().buildMarginalisationKey(table);
  }

  public double sumProbabilities(Collection<NodeState> request) {
    VectorCombinationKey key = new VectorCombinationKeyFactory().buildKey(table, request);
    return sumProbabilities(key);
  }

  public double sumProbabilities(VectorCombinationKey comboKey) {
    ProbabilityVector vector = table.getVector();
    double[] probability = vector.getProbabilities();
    DoubleAdder adder = new DoubleAdder();
    iterator.iterateKeyCombos(vector, comboKey, (key, index) -> adder.add(probability[index]));
    double sum = adder.sum();
    if (Double.isNaN(sum)) {
      throwQueryError("matched probabilities summing to NaN", comboKey.getRequest().values());
    }
    return sum;
  }

  private void throwQueryError(String endMessage, Collection<NodeState> request) {
    StringBuilder requestString = new StringBuilder();
    request.forEach(ns -> requestString.append(ns.getStateID()).append(" "));
    throw new IllegalArgumentException(
        String.format("Request %s to table %s %s", requestString, table.getTableID(), endMessage));
  }

  public Set<Set<NodeState>> generateStateCombinations(Collection<NodeState> lockedStates) {
    return generateStateCombinations(lockedStates, table.getNodes());
  }

  public Set<Set<NodeState>> generateStateCombinations(
      Collection<NodeState> lockedStates, Set<Node> includedNodes) {
    Set<Set<NodeState>> combos = new LinkedHashSet<>();
    Node[] nodes = table.getVector().getNodeArray();

    List<NodeState> states = new ArrayList<>(lockedStates);
    // Lock excluded nodes to their first state for faster iteration
    Arrays.stream(nodes)
        .filter(n -> !includedNodes.contains(n))
        .map(n -> n.getNodeStates().getFirst())
        .forEach(states::add);

    iterator.iterateKeyCombos(
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

  public void marginalizeTable() {
    ProbabilityVector vector = table.getVector();

    int[] tumblerKey = marginalisationKey.getTumblerKey();
    boolean[] lockConditions = marginalisationKey.getInnerLock();
    boolean[] lockEvents = marginalisationKey.getOuterLock();

    double[] probs = vector.getProbabilities();

    DoubleAdder adder = new DoubleAdder();

    iterator.iterateKeyCombos(
        vector,
        tumblerKey,
        lockEvents,
        (conditionKey, conditionIndex) -> {
          iterator.iterateKeyCombos(
              vector,
              conditionKey,
              lockConditions,
              (eventKey, eventIndex) -> adder.add(probs[eventIndex]));
          double sumAcrossConditions = adder.sumThenReset();
          if (sumAcrossConditions == 0) return;
          double ratio = 1 / sumAcrossConditions;
          iterator.iterateKeyCombos(
              vector,
              conditionKey,
              lockConditions,
              (eventKey, eventIndex) -> probs[eventIndex] = ratio * probs[eventIndex]);
        });
  }

  public static Set<NodeState> collectStatesPresentInTable(
      Collection<NodeState> currentStates, ProbabilityTable table) {
    Set<Node> tableNodes = table.getNodes();
    return currentStates.stream()
        .filter(ns -> tableNodes.contains(ns.getNode()))
        .collect(Collectors.toCollection(HashSet::new));
  }

  public void confirmAllNodesQueried(Collection<NodeState> request) {
    Set<Node> nodeSet = new HashSet<>(table.getNodes());
    boolean duplicateNodes = false;
    for (NodeState state : request) {
      duplicateNodes = !nodeSet.remove(state.getNode());
      if (duplicateNodes) {
        throwQueryError("contained duplicate nodes", request);
      }
    }
    boolean allNodesQueried = nodeSet.isEmpty();
    if (!allNodesQueried) {
      throwQueryError("did not query all nodes", request);
    }
  }

  public void setProbability(Set<NodeState> request, double probability) {
    if (Double.isNaN(probability)) {
      throwQueryError("tried to set a probability to NaN", request);
    }
    ProbabilityVector vector = table.getVector();
    int index = collectIndexesWithStates(request).getFirst();
    vector.getProbabilities()[index] = probability;
  }

  public List<Integer> collectIndexesWithStates(Collection<NodeState> request) {
    List<Integer> indexes = new ArrayList<>();
    iterator.iterateKeyCombos(request, table, (key, index) -> indexes.add(index));
    return indexes;
  }
}
