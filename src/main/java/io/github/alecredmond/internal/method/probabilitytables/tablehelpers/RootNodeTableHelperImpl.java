package io.github.alecredmond.internal.method.probabilitytables.tablehelpers;

import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.RootNodeTable;
import io.github.alecredmond.export.method.probabilitytables.RootNodeTableHelper;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;
import io.github.alecredmond.internal.method.probabilitytables.tablebuilders.RootNodeTableBuilder;
import io.github.alecredmond.internal.method.probabilitytables.tablebuilders.TableBuilder;
import java.io.Serializable;
import java.util.*;
import java.util.function.Supplier;

public class RootNodeTableHelperImpl extends TableHelperBase<RootNodeTable>
    implements RootNodeTableHelper {

  public RootNodeTableHelperImpl(RootNodeTable table) {
    super(table);
  }

  @Override
  public void marginalizeTable() {
    TableUtils.marginalizeJointTable(table);
  }

  @Override
  public Map<NodeState, Double> getConditionalProb(Collection<NodeState> condition) {
    return buildProbabilityMap();
  }

  @Override
  public Map<NodeState, Double> getConditionalProbByIds(Collection<Serializable> conditionIDs) {
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
  public Double getProbabilityById(Serializable id) {
    return super.getProbabilityFromIDs(List.of(id));
  }

  @Override
  protected Supplier<TableBuilder<RootNodeTable>> supplyTableBuilder() {
    return RootNodeTableBuilder::new;
  }
}
