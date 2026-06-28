package io.github.alecredmond.internal.method.probabilitytables.tablehelpers;

import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ObservedTable;
import io.github.alecredmond.export.method.probabilitytables.ObservedTableQueryTool;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;
import io.github.alecredmond.internal.method.probabilitytables.tablebuilders.ObservedTableBuilder;
import io.github.alecredmond.internal.method.probabilitytables.tablebuilders.TableBuilder;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ObservedTableQueryToolImpl extends QueryToolBase<ObservedTable>
    implements ObservedTableQueryTool {
  public ObservedTableQueryToolImpl(ObservedTable table) {
    super(table);
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
    return TableUtils.buildMarginalProbMap(table);
  }

  @Override
  public void normalizeTable() {
    TableUtils.marginalizeJointTable(table);
  }

  @Override
  protected Supplier<TableBuilder<ObservedTable>> supplyTableBuilder() {
    return ObservedTableBuilder::new;
  }

}
