package io.github.alecredmond.internal.method.probabilitytables.tablehelpers.impl;

import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.RootNodeTable;
import io.github.alecredmond.export.method.probabilitytables.RootNodeTableQueryTool;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;
import io.github.alecredmond.internal.method.probabilitytables.tablebuilders.RootNodeTableBuilder;
import io.github.alecredmond.internal.method.probabilitytables.tablebuilders.TableBuilder;
import io.github.alecredmond.internal.method.probabilitytables.tablehelpers.base.NetworkQueryToolBase;
import java.io.Serializable;
import java.util.*;
import java.util.function.Supplier;

public class RootNodeTableQueryToolImpl extends NetworkQueryToolBase<RootNodeTable>
    implements RootNodeTableQueryTool {

  public RootNodeTableQueryToolImpl(RootNodeTable table) {
    super(table);
  }

  @Override
  public void normalizeTable() {
    TableUtils.marginalizeJointTable(table);
  }

  @Override
  public Map<NodeState, Double> getConditionalProb(Collection<NodeState> condition) {
    return buildProbabilityMap();
  }

  @Override
  public <S extends Serializable> Map<NodeState, Double> getConditionalProbByIds(
      Collection<S> conditionIDs) {
    return buildProbabilityMap();
  }

  @Override
  public Map<NodeState, Double> buildProbabilityMap() {
    return TableUtils.buildMarginalProbMap(table);
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
  protected Supplier<TableBuilder<RootNodeTable>> supplyTableBuilder() {
    return RootNodeTableBuilder::new;
  }
}
