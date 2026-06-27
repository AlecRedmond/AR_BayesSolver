package io.github.alecredmond.internal.method.inference.solver.cptmapper;

import io.github.alecredmond.export.application.constraints.MarginalConstraint;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.RootNodeTable;
import io.github.alecredmond.internal.method.constraints.types.marginalconstraint.MarginalConstraintValidator;
import java.util.Collection;

public class RootCPTMapperIterator extends CptMapperIterator<RootNodeTable, MarginalConstraint> {

  public RootCPTMapperIterator(
      RootNodeTable networkTable,
      Collection<MarginalConstraint> constraints,
      MarginalConstraintValidator validator,
      BayesianNetworkData networkData) {
    super(networkTable, constraints, validator, networkData);
  }

  @Override
  protected String getIllegalSumString(MissingEntryCheck entryCheck) {
    return "Probabilities over table %s did not sum to 1.0! - was %.6f"
        .formatted(networkTable.getTableName(), 1.0 - entryCheck.sum);
  }

  @Override
  protected MarginalConstraint buildMissingFromRow(MissingEntryCheck entryCheck) {
    double probability = entryCheck.sum;
    NodeState state = entryCheck.missing.iterator().next();
    return new MarginalConstraint(state, probability);
  }
}
