package io.github.alecredmond.internal.method.constraints;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintStrategy;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintValidator;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConstraintRegistry {
  private static final Map<Class<? extends ProbabilityConstraint>, ConstraintStrategy<?>> REGISTRY =
      buildRegistry();

  private ConstraintRegistry() {}

  @SuppressWarnings("unchecked")
  public static <T extends ProbabilityConstraint> ConstraintStrategy<T> getStrategy(
      Class<T> constraintClass) {
    return (ConstraintStrategy<T>) REGISTRY.get(constraintClass);
  }

  @SuppressWarnings("rawtypes")
  public static List<ConstraintValidator> buildValidatorList() {
    return REGISTRY.values().stream()
        .map(ConstraintStrategy::buildConstraintValidator)
        .map(ConstraintValidator.class::cast)
        .toList();
  }

  private static Map<Class<? extends ProbabilityConstraint>, ConstraintStrategy<?>>
      buildRegistry() {
    return Arrays.stream(ConstraintType.values())
        .map(type -> Map.entry(type.getConstraintClass(), type.getStrategySupplier().get()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }
}
