package io.github.alecredmond.internal.method.constraints;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.internal.method.constraints.strategy.ConstraintSerializer;
import io.github.alecredmond.internal.method.constraints.strategy.ConstraintStrategy;
import io.github.alecredmond.internal.method.constraints.strategy.ConstraintValidator;
import java.util.*;
import java.util.stream.Stream;
import lombok.NonNull;

public class ConstraintRegistry {
  private final Map<ConstraintType, ConstraintStrategy<?>> registryMap = buildStrategyMap();

  public <V extends ProbabilityConstraint> ConstraintValidator<?, ?> getValidator(V constraint) {
    return Optional.ofNullable(ConstraintType.getType(constraint))
        .map(registryMap::get)
        .map(ConstraintStrategy::getConstraintValidator)
        .orElse(null);
  }

  public Stream<ConstraintValidator<?, ?>> streamValidators() {
    return registryMap.values().stream().map(ConstraintStrategy::getConstraintValidator);
  }

  @SuppressWarnings("unchecked")
  public <V extends P, P extends ProbabilityConstraint>
      ConstraintSerializer<ProbabilityConstraint> getSerializer(V constraint) {
    return (ConstraintSerializer<ProbabilityConstraint>)
        Optional.ofNullable(ConstraintType.getType(constraint))
            .map(registryMap::get)
            .map(ConstraintStrategy::getConstraintSerializer)
            .orElse(null);
  }

  public <T extends ProbabilityConstraint> ConstraintStrategy<?> getStrategy(
      @NonNull T constraint) {
    ConstraintType type = ConstraintType.getType(constraint);
    return getStrategy(type);
  }

  public ConstraintStrategy<?> getStrategy(ConstraintType type) {
    return registryMap.get(type);
  }

  private Map<ConstraintType, ConstraintStrategy<?>> buildStrategyMap() {
    Map<ConstraintType, ConstraintStrategy<?>> map = new EnumMap<>(ConstraintType.class);
    Arrays.stream(ConstraintType.values()).forEach(v -> map.put(v, v.getStrategySupplier().get()));
    return map;
  }
}
