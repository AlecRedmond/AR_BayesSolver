package io.github.alecredmond.internal.method.probabilitytables.probabilityvector.iteratorfactory;

import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.VectorOdometer;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.VectorIterator;

public interface VectorIteratorFactory<T extends VectorIterator> {
  T buildIterator(ProbabilityVector vector);

  static VectorOdometer blankOdometer(ProbabilityVector vector) {
    VectorOdometer vectorOdometer = new VectorOdometer();
    int keyLength = vector.getNodeArray().length;
    vectorOdometer.setProbabilities(vector.getProbabilities());
    vectorOdometer.setNodeArray(vector.getNodeArray());
    vectorOdometer.setStateArrays(vector.getStateArrays());
    vectorOdometer.setNumberOfStates(vector.getNumberOfStates());
    vectorOdometer.setStepMultiplier(vector.getStepMultiplier());
    vectorOdometer.setStates(new NodeState[keyLength]);
    vectorOdometer.setStateIndexes(new int[keyLength]);
    vectorOdometer.setOuterIteratorLocks(new boolean[keyLength]);
    vectorOdometer.setInnerIteratorLocks(new boolean[keyLength]);
    vectorOdometer.setNodeStateEvidenceArray(new boolean[keyLength][]);
    return vectorOdometer;
  }
}
