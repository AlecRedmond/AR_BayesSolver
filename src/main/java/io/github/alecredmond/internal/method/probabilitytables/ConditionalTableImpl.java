package io.github.alecredmond.internal.method.probabilitytables;

import io.github.alecredmond.exceptions.NodeStateConflictException;
import io.github.alecredmond.exceptions.ProbabilityTableRequestException;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ConditionalTable;
import io.github.alecredmond.internal.application.probabilitytables.ConditionalTableData;
import io.github.alecredmond.internal.method.probabilitytables.tablebuilders.ConditionalTableBuilder;
import io.github.alecredmond.internal.method.vectoriterator.misciterators.CptConditionIterator;
import io.github.alecredmond.internal.method.vectoriterator.misciterators.TableNormalizer;
import java.io.Serializable;
import java.util.*;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EqualsAndHashCode(callSuper = true,onlyExplicitlyIncluded = true)
public class ConditionalTableImpl extends NetworkTableBase<ConditionalTableData>
    implements ConditionalTable {
  private final TableNormalizer normalizer;

  public ConditionalTableImpl(ConditionalTableData tableData) {
    super(tableData);
    this.normalizer = new TableNormalizer(this);
  }

  @Override
  public void normalizeTable() {
    normalizer.normalize();
  }

  @Override
  public ConditionalTable copyTable() {
    return new ConditionalTableBuilder().copyTable(this);
  }

  @Override
  protected CptConditionIterator supplyConditionIterator() {
    return new CptConditionIterator(this);
  }

    @Override
  public <S extends Serializable> Map<NodeState, Double> getConditionalProbByIds(
      Collection<S> conditionIDs) {
    Collection<NodeState> states;
    try {
      states = getStates(conditionIDs, tableData.getConditions());
    } catch (ProbabilityTableRequestException e) {
      log.error(e.getMessage());
      return new HashMap<>();
    }
    return getConditionalProb(states);
  }

  @Override
  public Map<NodeState, Double> getConditionalProb(Collection<NodeState> conditionStates) {
    try {
      if (safeMode) TableUtils.assertAllNodesPresent(conditionStates, tableData.getConditions());
      return TableUtils.buildConditionalProbMap(conditionStates, tableData);
    } catch (NodeStateConflictException | ProbabilityTableRequestException e) {
      log.error(e.getMessage());
      return new HashMap<>();
    }
  }
}
