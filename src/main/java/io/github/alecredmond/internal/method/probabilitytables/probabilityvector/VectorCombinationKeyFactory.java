package io.github.alecredmond.internal.method.probabilitytables.probabilityvector;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.VectorCombinationKey;
import io.github.alecredmond.internal.method.node.NodeUtils;
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

    if (!table.getNodes().containsAll(requestMap.keySet())) {
      throw new IllegalArgumentException(
          String.format("the Request to table %s did not match the keyset", table.getTableName()));
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
    return buildReadWriteKey(table, conditions);
  }

  public VectorCombinationKey buildReadWriteKey(ProbabilityTable table, Set<Node> sharedNodes) {
    ProbabilityVector vector = table.getVector();
    return buildReadWriteKey(vector, sharedNodes);
  }

  public VectorCombinationKey buildReadWriteKey(ProbabilityVector vector, Set<Node> sharedNodes) {
    int keyLength = vector.getNodeArray().length;
    int[] tumblerKey = new int[keyLength];
    boolean[] innerLocks = new boolean[keyLength];
    boolean[] outerLocks = new boolean[keyLength];

    sharedNodes.forEach(node -> innerLocks[vector.getNodeIndexMap().get(node)] = true);
    IntStream.range(0, outerLocks.length).forEach(i -> outerLocks[i] = !innerLocks[i]);
    return new VectorCombinationKey(null, tumblerKey, innerLocks, outerLocks);
  }
}
