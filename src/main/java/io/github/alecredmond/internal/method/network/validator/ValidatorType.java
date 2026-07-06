package io.github.alecredmond.internal.method.network.validator;

import java.util.function.Supplier;
import lombok.Getter;

@Getter
public enum ValidatorType {
  ALL_NODES_HAVE_STATES(NetworkStatesValidator::new),
  ID_VALIDATOR(NetworkIdValidator::new),
  STRUCTURE_VALIDATOR(NetworkStructureValidator::new),
  CONSTRAINT_VALIDATOR(NetworkConstraintValidator::new);

  private final Supplier<NetworkValidator> validatorSupplier;

  ValidatorType(Supplier<NetworkValidator> validatorSupplier) {
    this.validatorSupplier = validatorSupplier;
  }
}
