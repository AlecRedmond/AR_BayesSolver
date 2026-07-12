package io.github.alecredmond.internal.method.probabilitytables.tablehelpers.impl;

import io.github.alecredmond.exceptions.NodeStateConflictException;
import io.github.alecredmond.exceptions.ProbabilityTableRequestException;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ConditionalTable;
import io.github.alecredmond.export.probabilitytables.ConditionalTableQueryTool;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;
import io.github.alecredmond.internal.method.probabilitytables.tablebuilders.ConditionalTableBuilder;
import io.github.alecredmond.internal.method.probabilitytables.tablebuilders.TableBuilder;
import io.github.alecredmond.internal.method.probabilitytables.tablehelpers.base.NetworkQueryToolBase;
import io.github.alecredmond.internal.method.vectoriterator.misciterators.TableNormalizer;
import java.io.Serializable;
import java.util.*;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConditionalTableQueryToolImpl extends NetworkQueryToolBase<ConditionalTable>
    implements ConditionalTableQueryTool {
  private final TableNormalizer normalizer;

  public ConditionalTableQueryToolImpl(ConditionalTable table) {
    super(table);
    this.normalizer = new TableNormalizer(table);
  }

  @Override
  public void normalizeTable() {
    normalizer.normalize();
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
