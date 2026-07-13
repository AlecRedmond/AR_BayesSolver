package io.github.alecredmond.internal.method.network.validator;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.internal.method.constraints.ConstraintRegistry;
import io.github.alecredmond.internal.method.constraints.strategy.ValidatedConstraint;
import java.util.*;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NetworkConstraintValidator implements NetworkValidator {
  private final ConstraintRegistry registry = new ConstraintRegistry();

  @Override
  public void validateData(BayesianNetworkData networkData) {
    Set<ProbabilityConstraint> constraints = networkData.getConstraints();
    List<ProbabilityConstraint> newlyValidated = new ArrayList<>();
    Iterator<ProbabilityConstraint> iterator = constraints.iterator();
    while (iterator.hasNext()) {
      ProbabilityConstraint constraint = iterator.next();
      if (constraint instanceof ValidatedConstraint<?>) {
        continue;
      }
      iterator.remove();
      newlyValidated.add(validateConstraint(constraint, networkData));
    }
    constraints.addAll(newlyValidated);
  }

  private ProbabilityConstraint validateConstraint(
      ProbabilityConstraint constraint, BayesianNetworkData networkData) {
    return registry
        .getValidator(constraint)
        .validateConstraint(constraint, networkData)
        .getConstraint();
  }
}
