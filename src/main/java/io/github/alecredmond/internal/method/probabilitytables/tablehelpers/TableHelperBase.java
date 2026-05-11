package io.github.alecredmond.internal.method.probabilitytables.tablehelpers;

import io.github.alecredmond.exceptions.NodeStateConflictException;
import io.github.alecredmond.exceptions.ProbabilityTableRequestException;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.method.probabilitytables.TableCopier;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;
import io.github.alecredmond.internal.method.vectoriterator.misciterators.ConstraintBuilderIterator;
import io.github.alecredmond.internal.method.vectoriterator.misciterators.ProbabilityMapper;
import java.io.Serializable;
import java.util.*;
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
      if (safeMode) TableUtils.assertAllNodesPresent(states, table);
      return TableUtils.getProbability(states, table);
    } catch (NodeStateConflictException | ProbabilityTableRequestException e) {
      log.error(e.getMessage());
      return null;
    }
  }

  public Double getProbabilityFromIDs(Collection<Serializable> stateIds) {
    try {
      return TableUtils.getProbability(getStates(stateIds), table);
    } catch (NodeStateConflictException | ProbabilityTableRequestException e) {
      log.error(e.getMessage());
      return null;
    }
  }

  protected Collection<NodeState> getStates(Collection<Serializable> stateIds) {
    return safeMode
        ? TableUtils.assertAllIdsPresent(stateIds, table)
        : TableUtils.convertIdsToStates(stateIds, table);
  }

  public boolean setProbability(Collection<NodeState> states, double probability) {
    try {
      if (safeMode) TableUtils.assertAllNodesPresent(states, table);
      TableUtils.setProbability(states, probability, table);
      return true;
    } catch (NodeStateConflictException | ProbabilityTableRequestException e) {
      log.error(e.getMessage());
      return false;
    }
  }

  public boolean setProbabilityById(Collection<Serializable> stateIds, double probability) {
    try {
      TableUtils.setProbability(getStates(stateIds), probability, table);
      return true;
    } catch (NodeStateConflictException | ProbabilityTableRequestException e) {
      log.error(e.getMessage());
      return false;
    }
  }

  public boolean setProbabilities(double[] doubleArray) {
    double[] probs = table.getVector().getProbabilities();
    if (probs.length == doubleArray.length) {
      System.arraycopy(doubleArray, 0, probs, 0, probs.length);
      return true;
    }
    log.error(
        "Could not copy array into table {}: array length was {}, required {}",
        table.getTableName(),
        doubleArray.length,
        probs.length);
    return false;
  }

  public T copyTable() {
    return new TableCopier().copyTable(table);
  }

  public Map<Set<NodeState>, Double> buildProbabilitySetMap() {
    return new ProbabilityMapper(table).getProbabilityMap();
  }

  public List<ProbabilityConstraint> generateConstraints() {
    return new ConstraintBuilderIterator(table).getBuilt();
  }
}
