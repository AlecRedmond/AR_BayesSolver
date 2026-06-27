package io.github.alecredmond.internal.method.inference.solver.cptmapper;

import io.github.alecredmond.export.application.constraints.ConditionalConstraint;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ConditionalTable;
import io.github.alecredmond.internal.method.constraints.types.conditionalconstraint.ConditionalConstraintValidator;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class ConditionalCPTMapperIterator
    extends CptMapperIterator<ConditionalTable, ConditionalConstraint> {

  public ConditionalCPTMapperIterator(
      ConditionalTable networkTable,
      Collection<ConditionalConstraint> constraints,
      ConditionalConstraintValidator validator) {
    super(networkTable, constraints, validator);
  }

  @Override
  protected String getIllegalSumString(MissingEntryCheck entryCheck) {
    Set<NodeState> condition = entryCheck.indexMap.keySet().iterator().next().getConditionStates();
    return "A probability row for condition %s in table %s does not add to exactly 1.0! - was %.6f"
        .formatted(
            NodeUtils.formatStatesToString(condition),
            networkTable.getTableName(),
            1 - entryCheck.sum);
  }

  @Override
  protected ConditionalConstraint buildMissingFromRow(MissingEntryCheck entryCheck) {
    double probability = entryCheck.sum;
    Set<NodeState> allStates = entryCheck.missing;

    NodeState eventState =
        allStates.stream()
            .filter(ns -> networkTable.getNetworkNode().equals(ns.getNode()))
            .findFirst()
            .orElseThrow();

    Set<NodeState> conditionStates =
        allStates.stream()
            .filter(ns -> networkTable.getConditions().contains(ns.getNode()))
            .collect(Collectors.toSet());

    return new ConditionalConstraint(eventState, conditionStates, probability);
  }
}
