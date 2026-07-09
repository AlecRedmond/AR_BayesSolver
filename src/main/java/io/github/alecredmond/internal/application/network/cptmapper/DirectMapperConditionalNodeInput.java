package io.github.alecredmond.internal.application.network.cptmapper;

import io.github.alecredmond.export.application.constraints.ConditionalConstraint;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.probabilitytables.ConditionalTable;
import io.github.alecredmond.internal.method.constraints.types.conditionalconstraint.ConditionalConstraintValidator;
import io.github.alecredmond.internal.method.inference.solver.cptmapper.iterator.ConditionalCPTMapperIterator;
import io.github.alecredmond.internal.method.inference.solver.cptmapper.iterator.CptMapperIterator;

public class DirectMapperConditionalNodeInput
    extends DirectMapperNodeInput<
        ConditionalTable, ConditionalConstraint, ConditionalConstraintValidator> {

  public DirectMapperConditionalNodeInput(
      Node node, ConditionalTable networkTable, ConditionalConstraintValidator validator) {
    super(node, networkTable, validator);
  }

  @Override
  protected CptMapperIterator<ConditionalTable, ConditionalConstraint> buildIterator() {
    return new ConditionalCPTMapperIterator(networkTable, validConstraints, validator);
  }
}
