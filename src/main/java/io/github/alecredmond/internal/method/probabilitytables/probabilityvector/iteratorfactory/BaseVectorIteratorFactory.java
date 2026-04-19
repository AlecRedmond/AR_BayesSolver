package io.github.alecredmond.internal.method.probabilitytables.probabilityvector.iteratorfactory;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.VectorOdometer;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.VectorIterator;

public abstract class BaseVectorIteratorFactory<T> implements VectorIteratorFactory<T> {
  protected VectorOdometer vectorOdometer;

  protected BaseVectorIteratorFactory() {
    vectorOdometer = new VectorOdometer();
  }

  @Override
  public VectorIterator build(ProbabilityVector vector, T requestItem) {
    buildBlank(vector);
    performRequestItemLogic(requestItem);
    return constructIterator();
  }

  private void buildBlank(ProbabilityVector vector) {
    int keyLength = vector.getNodeArray().length;
    vectorOdometer.setProbabilities(vector.getProbabilities());
    vectorOdometer.setNodeArray(vector.getNodeArray());
    vectorOdometer.setStateArrays(vector.getStateArrays());
    vectorOdometer.setNumberOfStates(vector.getNumberOfStates());
    vectorOdometer.setStepMultiplier(vector.getStepMultiplier());
    vectorOdometer.setOdometerStates(new NodeState[keyLength]);
    vectorOdometer.setOdometerValues(new int[keyLength]);
    vectorOdometer.setEventStatePosition(new boolean[keyLength]);
    vectorOdometer.setConditionStatePosition(new boolean[keyLength]);
    vectorOdometer.setStateIsEvent(new boolean[keyLength][]);
  }

  protected void performRequestItemLogic(T requestItem) {
    initializeOdometer(
        requestItem,
        vectorOdometer.getOdometerStates(),
        vectorOdometer.getOdometerValues(),
        vectorOdometer.getNodeArray());
    initializeEventAndConditionStates(
        requestItem,
        vectorOdometer.getEventStatePosition(),
        vectorOdometer.getConditionStatePosition(),
        vectorOdometer.getNodeArray());
    initializeStateIsEvent(
        requestItem,
        vectorOdometer.getStateIsEvent(),
        vectorOdometer.getEventStatePosition(),
        vectorOdometer.getStateArrays());
  }

  protected abstract VectorIterator constructIterator();

  protected abstract void initializeOdometer(
      T requestItem, NodeState[] odometer, int[] odometerIndexes, Node[] nodeArray);

  protected abstract void initializeEventAndConditionStates(
      T requestItem,
      boolean[] eventStatePosition,
      boolean[] conditionStatePosition,
      Node[] nodeArray);

  protected abstract void initializeStateIsEvent(
      T requestItem,
      boolean[][] complementStateIndexes,
      boolean[] eventStatePosition,
      NodeState[][] stateArrays);
}
