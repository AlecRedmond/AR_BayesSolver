package io.github.alecredmond.export.application.inference;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import lombok.Data;

@Data
public class SolverConstraintResult {
  private final ProbabilityConstraint constraint;
  private final double lastError;
  private final double[] errors;
  private final double[] losses;
}
