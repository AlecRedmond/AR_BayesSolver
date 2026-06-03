package io.github.alecredmond.export.application.inference;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.method.inference.BayesSolver;
import lombok.Data;

/**
 * Holds the per-cycle error and loss data for a single {@link ProbabilityConstraint} from a {@link
 * BayesSolver} run.
 *
 * @see SolverResults
 * @author Alec Redmond
 */
@Data
public class SolverConstraintResult {
  /** The constraint whose fitting results are recorded in this object. */
  private final ProbabilityConstraint constraint;

  /**
   * The R-squared error between the constraint's target probability and the fitted value on the
   * final solver cycle.
   */
  private final double lastError;

  /**
   * The R-squared error between the constraint's target probability and the fitted value at each
   * solver cycle, where index {@code i} corresponds to cycle {@code i}.
   */
  private final double[] errors;

  /**
   * The change in R-squared error between consecutive solver cycles, where index {@code i}
   * corresponds to cycle {@code i}. A negative value indicates the fit is improving; a positive
   * value indicates divergence.
   */
  private final double[] losses;
}
