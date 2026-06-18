package io.github.alecredmond.internal.application.network;

import io.github.alecredmond.exceptions.BayesNetIDException;
import io.github.alecredmond.exceptions.ConstraintValidationException;
import java.util.function.Consumer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class NetworkErrorPolicy {
  private Consumer<ConstraintValidationException> constraintValidationExceptionPolicy;
  private Consumer<BayesNetIDException> bayesNetIDExceptionPolicy;

  public NetworkErrorPolicy() {
    constraintValidationExceptionPolicy =
        e -> {
          throw e;
        };
    bayesNetIDExceptionPolicy =
        e -> {
          throw e;
        };
  }
}
