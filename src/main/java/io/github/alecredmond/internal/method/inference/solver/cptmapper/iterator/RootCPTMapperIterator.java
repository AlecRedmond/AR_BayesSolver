package io.github.alecredmond.internal.method.inference.solver.cptmapper.iterator;

import io.github.alecredmond.export.application.constraints.MarginalConstraint;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.RootNodeTable;
import io.github.alecredmond.internal.method.constraints.strategy.CPTConstraintValidator;
import io.github.alecredmond.internal.method.constraints.types.marginalconstraint.MarginalConstraintValidator;
import io.github.alecredmond.internal.method.inference.solver.cptmapper.constraintsorter.RootConstraintSorter;
import java.util.*;

public class RootCPTMapperIterator extends CptMapperIterator<RootNodeTable, MarginalConstraint> {

  public RootCPTMapperIterator(
      RootNodeTable networkTable,
      Collection<ProbabilityConstraint> allConstraints,
      MarginalConstraintValidator validator) {
    super(networkTable, allConstraints, validator, new RootConstraintSorter(networkTable));
  }

  @Override
  protected MarginalConstraint[] buildRowConstraintsArray() {
    return new MarginalConstraint[networkTable.getNetworkNode().getNodeStates().size()];
  }

  @Override
  protected String getIllegalSumString(MissingEntryCheck entryCheck) {
    return "Probabilities over table %s did not sum to 1.0! - was %.6f"
        .formatted(networkTable.getTableName(), 1.0 - entryCheck.remainder.doubleValue());
  }

  @Override
  protected MarginalConstraint validateAndInsertMissing(
      MissingEntryCheck entryCheck, CPTConstraintValidator<MarginalConstraint, ?> validator) {
    double probability = entryCheck.remainder.doubleValue();
    NodeState state = findFirstMissingState(entryCheck);
    return buildMissingConstraint(validator, state, probability);
  }

  @Override
  protected Collection<MarginalConstraint> addZeroProbabilityConstraints(
      MissingEntryCheck entryCheck, CPTConstraintValidator<MarginalConstraint, ?> validator) {
    NodeState[][] missingConstraints = entryCheck.missingConstraints;
    return Arrays.stream(missingConstraints)
        .filter(Objects::nonNull)
        .map(missingConstraint -> missingConstraint[0])
        .map(missing -> buildMissingConstraint(validator, missing, 0.0))
        .toList();
  }

  private NodeState findFirstMissingState(MissingEntryCheck entryCheck) {
    return Arrays.stream(entryCheck.missingConstraints)
        .filter(Objects::nonNull)
        .findFirst()
        .map(stateArray -> stateArray[0])
        .orElseThrow();
  }

  private static MarginalConstraint buildMissingConstraint(
      CPTConstraintValidator<MarginalConstraint, ?> validator, NodeState missing, double prob) {
    return validator.validateCPTConstraint(new MarginalConstraint(missing, prob)).getConstraint();
  }
}
