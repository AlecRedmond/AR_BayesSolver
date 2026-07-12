package io.github.alecredmond.internal.method.constraints;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
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
      ConstraintSerializer<ProbabilityConstraint, ?> getSerializer(V constraint) {
    return (ConstraintSerializer<ProbabilityConstraint, ?>)
        Optional.ofNullable(ConstraintType.getType(constraint))
            .map(registryMap::get)
            .map(ConstraintStrategy::getConstraintSerializer)
            .orElse(null);
  }

  public <T extends ProbabilityConstraint> ConstraintStrategy<T> getStrategy(
      @NonNull T constraint) {
    return getTypedStrategy(ConstraintType.getType(constraint));
  }

  @SuppressWarnings("unchecked")
  public <T extends ProbabilityConstraint> ConstraintStrategy<T> getTypedStrategy(
      ConstraintType type) {
    try {
      return (ConstraintStrategy<T>) registryMap.get(type);
    } catch (ClassCastException e) {
      return null;
    }
  }

  private Map<ConstraintType, ConstraintStrategy<?>> buildStrategyMap() {
    Map<ConstraintType, ConstraintStrategy<?>> map = new EnumMap<>(ConstraintType.class);
    Arrays.stream(ConstraintType.values()).forEach(v -> map.put(v, v.getStrategySupplier().get()));
    return map;
  }
}
