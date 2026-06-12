package io.github.alecredmond.internal.method.probabilitytables.tablehelpers;

import io.github.alecredmond.exceptions.NodeStateConflictException;
import io.github.alecredmond.exceptions.ProbabilityTableRequestException;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ConditionalTable;
import io.github.alecredmond.export.method.probabilitytables.ConditionalTableHelper;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;
import io.github.alecredmond.internal.method.probabilitytables.tablebuilders.ConditionalTableBuilder;
import io.github.alecredmond.internal.method.probabilitytables.tablebuilders.TableBuilder;
import io.github.alecredmond.internal.method.vectoriterator.misciterators.TableMarginalizer;
import java.io.Serializable;
import java.util.*;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConditionalTableHelperImpl extends TableHelperBase<ConditionalTable>
    implements ConditionalTableHelper {
  private final TableMarginalizer marginalizer;

  public ConditionalTableHelperImpl(ConditionalTable table) {
    super(table);
    this.marginalizer = new TableMarginalizer(table);
  }

  @Override
  public void marginalizeTable() {
    marginalizer.marginalize();
  }

  @Override
  protected Supplier<TableBuilder<ConditionalTable>> supplyTableBuilder() {
    return ConditionalTableBuilder::new;
  }

  @Override
  public <S extends Serializable> Map<NodeState, Double> getConditionalProbByIds(
      Collection<S> conditionIDs) {
    Collection<NodeState> states;
    try {
      states = getStates(conditionIDs, table.getConditions());
    } catch (ProbabilityTableRequestException e) {
      log.error(e.getMessage());
      return new HashMap<>();
    }
    return getConditionalProb(states);
  }

  @Override
  public Map<NodeState, Double> getConditionalProb(Collection<NodeState> conditionStates) {
    try {
      if (safeMode) TableUtils.assertAllNodesPresent(conditionStates, table.getConditions());
      return TableUtils.buildConditionalProbMap(conditionStates, table);
    } catch (NodeStateConflictException | ProbabilityTableRequestException e) {
      log.error(e.getMessage());
      return new HashMap<>();
    }
  }
}
