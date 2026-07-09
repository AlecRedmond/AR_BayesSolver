package io.github.alecredmond.internal.method.inference.solver.cptmapper.iterator;

import io.github.alecredmond.export.application.constraints.ConditionalConstraint;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ConditionalTable;
import io.github.alecredmond.internal.method.constraints.strategy.CPTConstraintValidator;
import io.github.alecredmond.internal.method.constraints.types.conditionalconstraint.ConditionalConstraintValidator;
import io.github.alecredmond.internal.method.inference.solver.cptmapper.constraintsorter.ConditionalTableConstraintSorter;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.util.*;
import java.util.stream.Collectors;

public class ConditionalCPTMapperIterator
    extends CptMapperIterator<ConditionalTable, ConditionalConstraint> {

  public ConditionalCPTMapperIterator(
      ConditionalTable networkTable,
      Collection<ProbabilityConstraint> allConstraints,
      ConditionalConstraintValidator validator) {
    super(
        networkTable,
        allConstraints,
        validator,
        new ConditionalTableConstraintSorter(networkTable));
  }

  @Override
  protected ConditionalConstraint[] buildRowConstraintsArray() {
    return new ConditionalConstraint[networkTable.getNetworkNode().getNodeStates().size()];
  }

  @Override
  protected String getIllegalSumString(MissingEntryCheck entryCheck) {
    Set<NodeState> condition = getConditions(entryCheck);
    return "A probability row for condition %s in table %s does not add to exactly 1.0! - was %.6f"
        .formatted(
            NodeUtils.formatStatesToString(condition),
            networkTable.getTableName(),
            1 - entryCheck.remainder.doubleValue());
  }

  private Set<NodeState> getConditions(MissingEntryCheck entryCheck) {
    return Arrays.stream(entryCheck.rowConstraints)
        .filter(Objects::nonNull)
        .findAny()
        .map(ConditionalConstraint::getConditionStates)
        .orElseThrow();
  }

  @Override
  protected ConditionalConstraint validateAndInsertMissing(
      MissingEntryCheck entryCheck, CPTConstraintValidator<ConditionalConstraint, ?> validator) {
    double probability = entryCheck.remainder.doubleValue();
    NodeState[] missingStates = getFirstMissingStates(entryCheck);
    return validateConditionalConstraint(validator, missingStates, probability);
  }

  @Override
  protected Collection<ConditionalConstraint> addZeroProbabilityConstraints(
      MissingEntryCheck entryCheck, CPTConstraintValidator<ConditionalConstraint, ?> validator) {
    NodeState[][] missingConstraints = entryCheck.missingConstraints;
    return Arrays.stream(missingConstraints)
        .map(states -> validateConditionalConstraint(validator, states, 0.0))
        .toList();
  }

  private NodeState[] getFirstMissingStates(MissingEntryCheck entryCheck) {
    return Arrays.stream(entryCheck.missingConstraints)
        .filter(Objects::nonNull)
        .findFirst()
        .orElseThrow();
  }

  private ConditionalConstraint validateConditionalConstraint(
      CPTConstraintValidator<ConditionalConstraint, ?> validator,
      NodeState[] missingStates,
      double probability) {
    NodeState eventState = missingStates[missingStates.length - 1];
    Set<NodeState> conditionStates =
        Arrays.stream(missingStates, 0, missingStates.length - 1).collect(Collectors.toSet());
    ConditionalConstraint cc = new ConditionalConstraint(eventState, conditionStates, probability);
    return validator.validateCPTConstraint(cc).getConstraint();
  }
}
