package io.github.alecredmond.export.application.inference;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.method.inference.BayesSolver;
import java.util.*;
import lombok.Data;

/**
 * A data class containing the results of a {@link BayesSolver} proportional fitting run. The class
 * contains information on the number of cycles completed, the final absolute error, and a map
 * linking each {@link ProbabilityConstraint} on the network to its associated {@link
 * SolverConstraintResult} for a more granular, per-constraint breakdown.
 *
 * @author Alec Redmond
 */
@Data
public class SolverResults {
  /** The number of cycles completed by the solver */
  private final int cycles;

  /**
   * A map that connects each constraint to its specific result, including details of the error and
   * loss rates per cycle
   */
  private final Map<ProbabilityConstraint, SolverConstraintResult> constraintResults;

  /**
   * The sum of R-squared errors between the fitted data and the expected data defined in the
   * constraints on the solver's final cycle.
   */
  private final double lastError;

  /**
   * Returns the granular results of a single constraint over the course of the solver's run,
   * including details of the error and loss rates per cycle.
   *
   * @param constraint a constraint that was active in the network during the solving process
   * @return the result object for the given constraint
   */
  public SolverConstraintResult getResult(ProbabilityConstraint constraint) {
    return constraintResults.get(constraint);
  }

  /**
   * Returns the list of constraint results, in descending order of last cycle error, and limited to
   * the given percentage of the total final error. This is useful for finding constraints that are
   * unable to find a fit within the data, or groups of constraints that overwrite each other. These
   * constraints are likely to be outliers by several orders of magnitude and can usually be found
   * within 2 standard deviations, or 95.45%, of the overall error.
   *
   * @param percent percentage of the final error
   * @return a list of constraint results, in descending error order, summing to the given
   *     percentage of the final error
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
