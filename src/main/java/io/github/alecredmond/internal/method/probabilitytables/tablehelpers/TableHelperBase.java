package io.github.alecredmond.internal.method.probabilitytables.tablehelpers;

import io.github.alecredmond.exceptions.NodeStateConflictException;
import io.github.alecredmond.exceptions.ProbabilityTableRequestException;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;
import io.github.alecredmond.internal.method.probabilitytables.tablebuilders.TableBuilder;
import io.github.alecredmond.internal.method.vectoriterator.misciterators.ProbabilityMapper;
import java.io.Serializable;
import java.util.*;
import java.util.function.Supplier;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Setter
@Slf4j
public abstract class TableHelperBase<T extends ProbabilityTable> {
  protected T table;
  protected boolean safeMode = true;

  protected TableHelperBase(T table) {
    this.table = table;
  }

  public Double getProbability(Collection<NodeState> states) {
    try {
      if (safeMode) TableUtils.assertAllNodesPresent(states, table.getNodes());
      return TableUtils.getProbability(states, table);
    } catch (NodeStateConflictException | ProbabilityTableRequestException e) {
      log.error(e.getMessage());
      return null;
    }
  }

  public <S extends Serializable> Double getProbabilityFromIDs(Collection<S> stateIds) {
    try {
      return TableUtils.getProbability(getStates(stateIds, table.getNodes()), table);
    } catch (NodeStateConflictException | ProbabilityTableRequestException e) {
      log.error(e.getMessage());
      return null;
    }
  }

  protected <S extends Serializable> Collection<NodeState> getStates(
      Collection<S> stateIds, Set<Node> expectedNodes) {
    return safeMode
        ? TableUtils.assertAllIdsPresent(stateIds, expectedNodes, table)
        : TableUtils.convertIdsToStates(stateIds, table);
  }

  public T copyTable() {
    return supplyTableBuilder().get().copyTable(table);
  }

  protected abstract Supplier<TableBuilder<T>> supplyTableBuilder();

  public Map<Set<NodeState>, Double> buildProbabilitySetMap() {
    return new ProbabilityMapper(table).getProbabilityMap();
  }
}
