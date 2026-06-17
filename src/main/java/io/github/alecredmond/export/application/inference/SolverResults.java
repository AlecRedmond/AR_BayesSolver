package io.github.alecredmond.export.application.inference;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.method.inference.BayesSolver;
import java.time.Duration;
import java.util.*;
import lombok.Data;

/**
 * Contains the results of a {@link BayesSolver} proportional fitting run, including the number of
 * cycles completed, the aggregate R-squared error on the final cycle, and a per-constraint
 * breakdown linking each {@link ProbabilityConstraint} to its associated {@link
 * SolverConstraintResult}.
 *
 * @author Alec Redmond
 */
@Data
public class SolverResults {
  /** The number of cycles completed by the solver */
  private final int cycles;

  /**
   * A map that connects each constraint to its specific result, including details of per-cycle
   * error and loss rates. The map is a {@link LinkedHashMap}, sorted in reverse order of final
   * cycle R-squared error.
   */
  private final Map<ProbabilityConstraint, SolverConstraintResult> constraintResults;

  /**
   * The aggregate R-squared error between the fitted data and the expected data defined in the
   * constraints, measured on the solver's final cycle.
   */
  private final double lastError;

  private final Duration solverRunDuration;

  /**
   * Returns the detailed result for a single constraint over the course of the solver run,
   * including per-cycle error and loss.
   *
   * @param constraint a constraint that was active in the network during solving.
   * @return the {@link SolverConstraintResult} for the given constraint, or {@code null} if the
   *     constraint was not present in this run.
   */
  public SolverConstraintResult getResult(ProbabilityConstraint constraint) {
    return constraintResults.get(constraint);
  }

  /**
   * Returns constraint results in descending order of final-cycle error, accumulated up to the
   * given percentage of the total final error. This is useful for identifying constraints that
   * cannot be satisfied within the data, or that conflict with other constraints. Such constraints
   * tend to be outliers by several orders of magnitude and are typically found within the worst
   * 95.45% (2 standard deviations) of the total error.
   *
   * @param percent the cumulative percentage of total error to cover, as a value between 0 and 100
   *     (e.g., {@code 95} to retrieve the constraints responsible for 95% of the total error).
   * @return a list of {@link SolverConstraintResult} objects in descending order of final-cycle
   *     error, whose combined error sums to approximately the given percentage of the total.
   */
  public List<SolverConstraintResult> getWorstNthPercentile(Number percent) {
    double goal = lastError * (percent.doubleValue() / 100.0);
    List<SolverConstraintResult> worstResults = new ArrayList<>();
    for (SolverConstraintResult result : constraintResults.values()) {
      if (goal <= 0.0) break;
      worstResults.add(result);
      goal -= result.getLastError();
    }
    return worstResults;
  }
}
