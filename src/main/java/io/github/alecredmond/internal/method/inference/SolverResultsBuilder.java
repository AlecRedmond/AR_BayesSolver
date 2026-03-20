package io.github.alecredmond.internal.method.inference;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.inference.SolverConstraintResult;
import io.github.alecredmond.export.application.inference.SolverResults;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class SolverResultsBuilder {

  public SolverResults buildResults(int cycle, Map<ProbabilityConstraint, double[]> resultsMap) {
    List<SolverConstraintResult> results =
        resultsMap.entrySet().stream()
            .map(this::buildConstraintResult)
            .sorted(Comparator.comparingDouble(SolverConstraintResult::getLastError).reversed())
            .toList();

    double lastError = results.stream().mapToDouble(SolverConstraintResult::getLastError).sum();
    return new SolverResults(cycle, results, lastError);
  }

  private SolverConstraintResult buildConstraintResult(
      Map.Entry<ProbabilityConstraint, double[]> entry) {
    ProbabilityConstraint constraint = entry.getKey();
    double[] errors = entry.getValue();
    double lastError = errors[errors.length - 1];
    double[] losses = new double[errors.length];
    IntStream.range(1, errors.length).forEach(i -> losses[i] = Math.abs(errors[i] - errors[i - 1]));
    return new SolverConstraintResult(constraint, lastError, errors, losses);
  }
}
