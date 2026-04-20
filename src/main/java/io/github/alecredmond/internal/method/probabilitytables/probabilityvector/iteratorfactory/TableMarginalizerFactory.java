package io.github.alecredmond.internal.method.probabilitytables.probabilityvector.iteratorfactory;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.TableMarginalizer;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.VectorIterator;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.IntStream;

public class TableMarginalizerFactory extends BaseVectorIteratorFactory<ProbabilityTable> {

  public TableMarginalizerFactory() {
    super();
  }

  public TableMarginalizer build(ProbabilityTable table) {
    return (TableMarginalizer) build(table.getVector(), table);
  }

  @Override
  protected void performRequestItemLogic(ProbabilityTable table) {
    Set<Node> conditions = table.getConditions();
    boolean[] eventPos = vectorOdometer.getEventStatePosition();
    boolean[] condiPos = vectorOdometer.getConditionStatePosition();
    Node[] nodes = vectorOdometer.getNodeArray();

    IntStream.range(0, nodes.length)
        .forEach(
            i -> {
              boolean isCondition = conditions.contains(nodes[i]);
              eventPos[i] = !isCondition;
              condiPos[i] = isCondition;
            });
  }

  @Override
  protected TableMarginalizer constructIterator() {
    return new TableMarginalizer(vectorOdometer);
  }

  @Override
  protected void initializeOdometer(
          ProbabilityTable requestItem, NodeState[] states, int[] stateIndexes, Node[] nodeArray) {
    IntStream.range(0, nodeArray.length)
        .forEach(
            i -> {
              Node node = nodeArray[i];
              int stateIndex = stateIndexes[i];
              states[i] = node.getNodeStates().get(stateIndex);
            });
  }

  @Override
  protected void initializeEventAndConditionStates(
      ProbabilityTable table, boolean[] eventPos, boolean[] condiPos, Node[] nodeArray) {
    Set<Node> conditions = table.getConditions();
    Node[] nodes = vectorOdometer.getNodeArray();
    IntStream.range(0, nodes.length)
        .forEach(
            i -> {
              boolean isCondition = conditions.contains(nodes[i]);
              eventPos[i] = !isCondition;
              condiPos[i] = isCondition;
            });
  }

  @Override
  protected void initializeStateIsEvent(
      ProbabilityTable requestItem,
      boolean[][] stateIsEvent,
      boolean[] eventStatePosition,
      NodeState[][] stateArrays) {
    int length = eventStatePosition.length;
    IntStream.range(0, length)
        .forEach(
            i -> {
              boolean set = eventStatePosition[i];
              boolean[] statePos = new boolean[stateArrays[i].length];
              Arrays.fill(statePos, set);
              stateIsEvent[i] = statePos;
            });
  }
}
