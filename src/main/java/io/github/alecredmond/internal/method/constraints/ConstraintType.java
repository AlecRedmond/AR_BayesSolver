package io.github.alecredmond.internal.method.constraints;

import io.github.alecredmond.export.application.constraints.*;
import io.github.alecredmond.internal.method.constraints.strategy.ConstraintStrategy;
import io.github.alecredmond.internal.method.constraints.types.combinedconstraint.SumConstraintStrategy;
import io.github.alecredmond.internal.method.constraints.types.conditionalconstraint.ConditionalConstraintStrategy;
import io.github.alecredmond.internal.method.constraints.types.jointconstraint.JointConstraintStrategy;
import io.github.alecredmond.internal.method.constraints.types.marginalconstraint.MarginalConstraintStrategy;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.Getter;

@Getter
public enum ConstraintType {
  MARGINAL(MarginalConstraint.class::isInstance, MarginalConstraintStrategy::new),
  CONDITIONAL(ConditionalConstraint.class::isInstance, ConditionalConstraintStrategy::new),
  JOINT(JointProbabilityConstraint.class::isInstance, JointConstraintStrategy::new),
  SUM(SumProbabilityConstraint.class::isInstance, SumConstraintStrategy::new);

  private final Predicate<ProbabilityConstraint> isInstance;
  private final Supplier<ConstraintStrategy<?>> strategySupplier;

  ConstraintType(
      Predicate<ProbabilityConstraint> isInstance,
      Supplier<ConstraintStrategy<?>> strategySupplier) {
    this.isInstance = isInstance;
    this.strategySupplier = strategySupplier;
  }

  public static <P extends ProbabilityConstraint> ConstraintType getType(P constraint) {
    return Arrays.stream(ConstraintType.values())
        .filter(v -> v.isInstance.test(constraint))
        .findAny()
        .orElse(null);
  }
}
