package io.github.alecredmond.export.method.inference;

import static io.github.alecredmond.TestConfigs.SOLVE_LONG_TESTS;
import static io.github.alecredmond.export.method.network.NetworkScenario.*;
import static org.junit.jupiter.api.Assertions.*;

import io.github.alecredmond.export.application.inference.SolverConstraintResult;
import io.github.alecredmond.export.application.inference.SolverResults;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import java.util.List;
import org.junit.jupiter.api.Test;

class BayesSolverTest {
  BayesSolver test;

  @Test
  void getResults() {
    BayesianNetwork network = SOLVE_LONG_TESTS ? FANTASY_GRAPH.get() : CAR_TRIMS.get();
    test = BayesSolver.create(network);
    assertTrue(test.solve());
    SolverResults results = test.getResults();
    assertNotNull(results);

    double worstPercent = 99.73;

    List<SolverConstraintResult> worstResults = results.getWorstNthPercentile(worstPercent);

    System.out.printf(
        "Worst %.1f%% results are %d of total %d%n",
        worstPercent, worstResults.size(), results.getConstraintResults().size());

    int count = 0;
    for (SolverConstraintResult r : results.getConstraintResults().values()) {
      if (count == worstResults.size()) {
        System.out.printf("-----------------------------------%n");
      }
      double[] errors = r.getErrors();
      double[] losses = r.getLosses();
      int lastIndex = errors.length - 1;
      double lastErrorLossRatio = Math.abs(errors[lastIndex] / losses[lastIndex]);
      System.out.printf(
          "LAST LOG ERROR = %.2f :: LAST LOG ERROR/LOSS RATIO = %.2f :: %s%n",
          Math.log10(errors[errors.length - 1]), Math.log10(lastErrorLossRatio), r.getConstraint());
      count++;
    }
  }
}
