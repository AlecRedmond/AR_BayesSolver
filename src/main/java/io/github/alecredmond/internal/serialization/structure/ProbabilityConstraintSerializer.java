package io.github.alecredmond.internal.serialization.structure;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.serialization.constraint.SerializedProbabilityConstraint;
import io.github.alecredmond.internal.method.constraints.ConstraintRegistry;
import io.github.alecredmond.internal.method.constraints.ConstraintType;
import io.github.alecredmond.internal.method.constraints.strategy.ConstraintSerializer;
import io.github.alecredmond.internal.method.constraints.strategy.ConstraintStrategy;
import io.github.alecredmond.internal.method.constraints.strategy.ValidatedConstraint;
import io.github.alecredmond.internal.method.network.validator.NetworkConstraintValidator;
import io.github.alecredmond.internal.serialization.SerializationData;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ProbabilityConstraintSerializer {
  private final ConstraintRegistry registry = new ConstraintRegistry();

  public List<SerializedProbabilityConstraint<ProbabilityConstraint>> serializeAll(
      BayesianNetworkData data) {
    new NetworkConstraintValidator().validateData(data);
    return data.getConstraints().stream().map(this::serializeConstraint).toList();
  }

  private <T extends ProbabilityConstraint>
      SerializedProbabilityConstraint<ProbabilityConstraint> serializeConstraint(T constraint) {
    ConstraintSerializer<ProbabilityConstraint> serializer = registry.getSerializer(constraint);
    return Optional.ofNullable(serializer)
        .map(s -> s.serialize(constraint))
        .orElseThrow(throwSerializerNotFound(constraint));
  }

  private Supplier<ConstraintValidationException> throwSerializerNotFound(Object object) {
    return () ->
        new ConstraintValidationException(
            "Object %s mapped to no valid ConstraintType".formatted(object));
  }

  public <T extends ProbabilityConstraint> void deserialize(
      List<SerializedProbabilityConstraint<T>> serializedConstraints, SerializationData data) {
    Set<ProbabilityConstraint> constraints = data.getNetworkData().getConstraints();
    serializedConstraints.stream()
        .map(serialized -> deSerializeConstraint(serialized, data))
        .forEach(constraints::add);
    new NetworkConstraintValidator().validateData(data.getNetworkData());
  }

  private <P extends ProbabilityConstraint> ProbabilityConstraint deSerializeConstraint(
      SerializedProbabilityConstraint<P> serialized, SerializationData data) {
    ConstraintStrategy<P> strategy = getStrategy(serialized);
    P constraint = strategy.getConstraintSerializer().deSerialize(serialized, data);
    ValidatedConstraint<P> validated =
        strategy.getConstraintValidator().validateConstraint(constraint, data.getNetworkData());
    return validated.getConstraint();
  }

  @SuppressWarnings("unchecked")
  private <T extends ProbabilityConstraint> ConstraintStrategy<T> getStrategy(
      SerializedProbabilityConstraint<T> serialized) {
    ConstraintType type = ConstraintType.valueOf(serialized.getConstraintType());
    ConstraintStrategy<?> strategy = registry.getStrategy(type);
    if (strategy == null) {
      throw throwSerializerNotFound(serialized).get();
    }
    if (!strategy.constraintClass().equals(serialized.getConstraintClass())) {
      throw new ConstraintValidationException(
          "Strategy %s does not match constraint type %s!".formatted(strategy, type));
    }
    return (ConstraintStrategy<T>) strategy;
  }
}
