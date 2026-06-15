package io.github.alecredmond.internal.method.constraints;

import io.github.alecredmond.export.application.constraints.*;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintStrategy;
import io.github.alecredmond.internal.method.constraints.types.combinedconstraint.SumConstraintStrategy;
import io.github.alecredmond.internal.method.constraints.types.conditionalconstraint.ConditionalConstraintStrategy;
import io.github.alecredmond.internal.method.constraints.types.jointconstraint.JointConstraintStrategy;
import io.github.alecredmond.internal.method.constraints.types.marginalconstraint.MarginalConstraintStrategy;
import java.util.function.Supplier;
import lombok.Getter;

@Getter
public enum ConstraintType {
  MARGINAL(MarginalConstraint.class, MarginalConstraintStrategy::new),
  CONDITIONAL(ConditionalConstraint.class, ConditionalConstraintStrategy::new),
  JOINT(JointProbabilityConstraint.class, JointConstraintStrategy::new),
  SUM(SumProbabilityConstraint.class, SumConstraintStrategy::new);

  private final Class<? extends ProbabilityConstraint> constraintClass;
  private final Supplier<ConstraintStrategy<?>> strategySupplier;

  ConstraintType(
      Class<? extends ProbabilityConstraint> constraintClass,
      Supplier<ConstraintStrategy<?>> strategySupplier) {
    this.constraintClass = constraintClass;
    this.strategySupplier = strategySupplier;
  }
}
