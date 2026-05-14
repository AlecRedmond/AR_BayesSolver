package io.github.alecredmond.export.application.inference;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.method.inference.BayesSolver;
import lombok.Data;

/**
 * A result segment tracking the error per cycle of a {@link ProbabilityConstraint} during a {@link
 * BayesSolver} run. Contains the measured constraint, the last reported error, and arrays for the
 * error and loss per cycle.
 *
 * @see SolverResults
 * @author Alec Redmond
 */
@Data
public class SolverConstraintResult {
  /** The constraint used in the solver run */
  private final ProbabilityConstraint constraint;

  /**
   * The R-Squared error between the constraint's given probability and the actual value of the data
   * on the final cycle of the run.
   */
  private final double lastError;

  /**
   * The R-squared error between the constraint's given probability and the actual value, indexed
   * per cycle.
   */
  private final double[] errors;

  /** The change in error over time, indexed per cycle. */
  private final double[] losses;
}
