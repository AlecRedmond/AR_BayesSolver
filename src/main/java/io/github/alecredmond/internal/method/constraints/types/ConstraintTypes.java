package io.github.alecredmond.internal.method.constraints.types;

import io.github.alecredmond.export.application.constraints.ConditionalConstraint;
import io.github.alecredmond.export.application.constraints.JointProbabilityConstraint;
import io.github.alecredmond.export.application.constraints.MarginalConstraint;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintStrategy;
import io.github.alecredmond.internal.method.constraints.types.conditionalconstraint.ConditionalConstraintStrategy;
import io.github.alecredmond.internal.method.constraints.types.jointconstraint.JointConstraintStrategy;
import io.github.alecredmond.internal.method.constraints.types.marginalconstraint.MarginalConstraintStrategy;
import java.util.function.Supplier;
import lombok.Getter;

@Getter
public enum ConstraintTypes {
  MARGINAL(MarginalConstraint.class, MarginalConstraintStrategy::new),
  CONDITIONAL(ConditionalConstraint.class, ConditionalConstraintStrategy::new),
  JOINT(JointProbabilityConstraint.class, JointConstraintStrategy::new);

  private final Class<? extends ProbabilityConstraint> constraintClass;
  private final Supplier<ConstraintStrategy<?>> strategySupplier;

  ConstraintTypes(
      Class<? extends ProbabilityConstraint> constraintClass,
      Supplier<ConstraintStrategy<?>> strategySupplier) {
    this.constraintClass = constraintClass;
    this.strategySupplier = strategySupplier;
  }
}
