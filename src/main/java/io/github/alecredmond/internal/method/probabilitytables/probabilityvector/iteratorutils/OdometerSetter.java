package io.github.alecredmond.internal.method.probabilitytables.probabilityvector.iteratorutils;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.VectorOdometer;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class OdometerSetter {
  private final VectorOdometer vectorOdometer;
  private final OdometerResetLogic resetLogic;

  public OdometerSetter(VectorOdometer vectorOdometer, OdometerResetLogic resetLogic) {
    this.vectorOdometer = vectorOdometer;
    this.resetLogic = resetLogic;
  }

  public void set() {
    Node[] nodeArray = vectorOdometer.getNodeArray();
    int[] stateIndexes = vectorOdometer.getStateIndexes();
    NodeState[] states = vectorOdometer.getStates();
    boolean[] outerIteratorLocks = vectorOdometer.getOuterIteratorLocks();
    boolean[] innerIteratorLocks = vectorOdometer.getInnerIteratorLocks();
    boolean[][] isEvidenceArray = vectorOdometer.getNodeStateEvidenceArray();
    Map<NodeState, Integer> stateIndexMap = vectorOdometer.getStateValueMap();

    Function<Node, NodeState> initialStateMapper = resetLogic.initialStatePositionSetter();
    Function<Node, boolean[]> evidenceMapper = resetLogic.buildEvidenceMaps();
    Predicate<Node> outerFn = resetLogic.checkLockOuter();
    Predicate<Node> innerFn = resetLogic.checkLockInner();

    for (int x = 0; x < nodeArray.length; x++) {
      Node node = nodeArray[x];
      NodeState state = initialStateMapper.apply(node);
      states[x] = state;
      stateIndexes[x] = stateIndexMap.get(state);
      outerIteratorLocks[x] = outerFn.test(node);
      innerIteratorLocks[x] = innerFn.test(node);
      isEvidenceArray[x] = evidenceMapper.apply(node);
    }

    resetLogic.postUpdateLogic().run();
  }
}
