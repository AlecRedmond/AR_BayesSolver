package io.github.alecredmond.export.application.inference;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import java.util.*;
import lombok.Data;

@Data
public class SolverResults {
  private final int cycles;
  private final Map<ProbabilityConstraint, SolverConstraintResult> constraintResults;
  private final double lastError;

  public SolverConstraintResult getResult(ProbabilityConstraint constraint) {
    return constraintResults.get(constraint);
  }

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
