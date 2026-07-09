package io.github.alecredmond.internal.application.network.cptmapper;

import io.github.alecredmond.export.application.constraints.MarginalConstraint;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.probabilitytables.RootNodeTable;
import io.github.alecredmond.internal.method.constraints.types.marginalconstraint.MarginalConstraintValidator;
import io.github.alecredmond.internal.method.inference.solver.cptmapper.iterator.CptMapperIterator;
import io.github.alecredmond.internal.method.inference.solver.cptmapper.iterator.RootCPTMapperIterator;

public class DirectMapperRootNodeInput
    extends DirectMapperNodeInput<RootNodeTable, MarginalConstraint, MarginalConstraintValidator> {

  public DirectMapperRootNodeInput(
      Node node, RootNodeTable networkTable, MarginalConstraintValidator validator) {
    super(node, networkTable, validator);
  }

  @Override
  protected CptMapperIterator<RootNodeTable, MarginalConstraint> buildIterator() {
    return new RootCPTMapperIterator(networkTable, validConstraints, validator);
  }
}
