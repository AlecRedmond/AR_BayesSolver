package io.github.alecredmond.internal.method.probabilitytables;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ObservedTable;
import io.github.alecredmond.internal.application.probabilitytables.ObservedTableData;
import io.github.alecredmond.internal.method.probabilitytables.tablebuilders.ObservedTableBuilder;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true,onlyExplicitlyIncluded = true)
public class ObservedTableImpl extends ProbabilityTableBase<ObservedTableData>
    implements ObservedTable {
  public ObservedTableImpl(ObservedTableData tableData) {
    super(tableData);
  }

  @Override
  public Node getMeasuredNode() {
    return tableData.getMeasuredNode();
  }

  @Override
  public Map<Node, NodeState> getObservations() {
    return tableData.getObservations();
  }

  @Override
  public ObservedTable copyTable() {
    return new ObservedTableBuilder().copyTable(this);
  }

  @Override
  public Double getProbability(NodeState state) {
    return super.getProbability(List.of(state));
  }

  @Override
  public Double getProbabilityById(Serializable stateId) {
    return super.getProbabilityFromIDs(List.of(stateId));
  }

  @Override
  public Map<NodeState, Double> buildProbabilityMap() {
    return TableUtils.buildMarginalProbMap(tableData);
  }

  public void setObservations(Map<Node, NodeState> observationMap) {
    tableData.setObservations(observationMap);
  }

  @Override
  public void normalizeTable() {
    TableUtils.marginalizeJointTable(this);
  }
}
