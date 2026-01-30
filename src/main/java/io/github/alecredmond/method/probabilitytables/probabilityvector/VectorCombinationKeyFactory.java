package io.github.alecredmond.method.probabilitytables.probabilityvector;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.application.probabilitytables.probabilityvector.VectorCombinationKey;
import io.github.alecredmond.method.node.NodeUtils;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class VectorCombinationKeyFactory {

  public VectorCombinationKey buildKey(ProbabilityTable table, Collection<NodeState> request) {
    Map<Node, NodeState> requestMap = NodeUtils.generateRequest(request);

    if (!requestMap.keySet().equals(table.getNodes())) {
      throw new IllegalArgumentException(
          String.format("the Request to table %s did not match the keyset", table.getTableID()));
    }

    return buildKey(requestMap, table.getVector());
  }

  public VectorCombinationKey buildKey(Map<Node, NodeState> requestMap, ProbabilityVector vector) {
    int keyLength = vector.getNodeArray().length;
    int[] tumblerKey = new int[keyLength];
    boolean[] positionLocked = new boolean[keyLength];
    boolean[] invertedLock = new boolean[keyLength];
    Arrays.fill(invertedLock, true);
    requestMap.forEach(
        (node, state) -> {
          int nodeIndex = vector.getNodeIndexMap().get(node);
          int stateValue = vector.getStateValueMap().get(state);
          tumblerKey[nodeIndex] = stateValue;
          positionLocked[nodeIndex] = true;
          invertedLock[nodeIndex] = false;
        });
    return new VectorCombinationKey(requestMap, tumblerKey, positionLocked, invertedLock);
  }

  public VectorCombinationKey buildMarginalisationKey(ProbabilityTable table) {
    Set<Node> conditions = table.getConditions();
    ProbabilityVector vector = table.getVector();
    int keyLength = vector.getNodeArray().length;
    int[] tumblerKey = new int[keyLength];
    boolean[] conditionLocks = new boolean[keyLength];
    boolean[] eventLocks = new boolean[keyLength];

    conditions.forEach(condition -> conditionLocks[vector.getNodeIndexMap().get(condition)] = true);
    IntStream.range(0, eventLocks.length).forEach(i -> eventLocks[i] = !conditionLocks[i]);
    return new VectorCombinationKey(null, tumblerKey, conditionLocks, eventLocks);
  }
}
