package io.github.alecredmond.internal.method.constraints;

import io.github.alecredmond.internal.method.constraints.strategy.ConstraintStrategy;
import io.github.alecredmond.internal.method.constraints.types.conditionalconstraint.ConditionalConstraintStrategy;
import io.github.alecredmond.internal.method.constraints.types.jointconstraint.JointConstraintStrategy;
import io.github.alecredmond.internal.method.constraints.types.marginalconstraint.MarginalConstraintStrategy;
import io.github.alecredmond.internal.method.constraints.types.sumconstraint.SumConstraintStrategy;
import java.util.function.Supplier;
import lombok.Getter;

@Getter
public enum ConstraintType {
  MARGINAL(MarginalConstraintStrategy::new),
  CONDITIONAL(ConditionalConstraintStrategy::new),
  JOINT(JointConstraintStrategy::new),
  SUM(SumConstraintStrategy::new);

  private final Supplier<ConstraintStrategy<?>> strategySupplier;

  ConstraintType(Supplier<ConstraintStrategy<?>> strategySupplier) {
    this.strategySupplier = strategySupplier;
  }
}
