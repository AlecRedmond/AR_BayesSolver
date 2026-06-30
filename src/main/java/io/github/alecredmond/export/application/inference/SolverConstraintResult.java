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
@SuppressWarnings("LombokGetterMayBeUsed")
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
   * solver cycle. Index {@code i} corresponds to cycle {@code i}.
   */
  private final double[] errors;

  /**
   * The change in R-squared error between consecutive solver cycles. Index {@code i} corresponds to
   * cycle {@code i}.
   *
   * <p>A negative value indicates the fit is improving; a positive value indicates divergence.
   */
  private final double[] losses;

  /**
   * Returns the probability constraint associated with these solver results.
   *
   * @return the {@link ProbabilityConstraint} instance.
   */
  public ProbabilityConstraint getConstraint() {
    return this.constraint;
  }

  /**
   * Returns the R-squared error recorded on the final cycle of the solver run.
   *
   * @return the final cycle's R-squared error as a double.
   */
  public double getLastError() {
    return this.lastError;
  }

  /**
   * Returns the historical array of R-squared errors across all completed solver cycles.
   *
   * @return an array of doubles representing per-cycle errors, where index {@code i} maps to cycle
   *     {@code i}.
   */
  public double[] getErrors() {
    return this.errors;
  }

  /**
   * Returns the historical array of loss rates (error deltas) across all completed solver cycles.
   *
   * @return an array of doubles representing consecutive cycle error changes, where index {@code i}
   *     maps to cycle {@code i}.
   */
  public double[] getLosses() {
    return this.losses;
  }
}
