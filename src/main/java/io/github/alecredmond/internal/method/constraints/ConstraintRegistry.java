package io.github.alecredmond.internal.method.constraints;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintStrategy;
import io.github.alecredmond.internal.method.utils.MapUtils;

import java.util.Map;

public class ConstraintRegistry {
  private static final Map<Class<? extends ProbabilityConstraint>, ConstraintStrategy<?>> REGISTRY =
      buildRegistry();

  private ConstraintRegistry() {}

  @SuppressWarnings("unchecked")
  public static <T extends ProbabilityConstraint> ConstraintStrategy<T> getStrategy(
      Class<T> constraintClass) {
    return (ConstraintStrategy<T>) REGISTRY.get(constraintClass);
  }

  private static Map<Class<? extends ProbabilityConstraint>, ConstraintStrategy<?>>
      buildRegistry() {
    return MapUtils.mapFromInput(
        ConstraintTypes.values(),
        ConstraintTypes::getConstraintClass,
        t -> t.getStrategySupplier().get());
  }
}
