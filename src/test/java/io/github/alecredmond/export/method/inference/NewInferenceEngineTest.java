package io.github.alecredmond.export.method.inference;

import static io.github.alecredmond.TestConfigs.SOLVE_LONG_TESTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.alecredmond.export.application.constraints.*;
import io.github.alecredmond.export.application.inference.SolverConstraintResult;
import io.github.alecredmond.export.application.inference.SolverResults;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.export.method.network.NetworkScenario;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class NewInferenceEngineTest {
  static final List<NetworkScenario> scenarios = provideScenarios();
  InferenceEngine test;

  private static List<NetworkScenario> provideScenarios() {
    return Arrays.stream(NetworkScenario.values())
        .filter(scenario -> !scenario.equals(NetworkScenario.FANTASY_GRAPH) || SOLVE_LONG_TESTS)
        .toList();
  }

  @Nested
  class ObservationTests {
    static final double DELTA = 1e-3;

    public static Stream<Arguments> conditionalObservations() {
      return argumentsOfConstraints(ConditionalConstraint.class);
    }

    static <T extends ProbabilityConstraint> Stream<Arguments> argumentsOfConstraints(
        Class<T> tClass) {
      return scenarios.stream()
          .flatMap(
              scenario -> {
                BayesianNetwork network = scenario.get().solveNetwork();
                return network.getNetworkData().getConstraints().stream()
                    .filter(tClass::isInstance)
                    .map(tClass::cast)
                    .map(constraint -> Arguments.of(network, constraint));
              });
    }

    public static Stream<Arguments> allObservations() {
      return scenarios.stream()
          .flatMap(
              scenario -> {
                BayesianNetwork network = scenario.get();
                BayesSolver solver = BayesSolver.create(network);
                solver.solve();
                SolverResults results = solver.getResults();
                return network.getNetworkData().getConstraints().stream()
                    .map(
                        constraint ->
                            Arguments.of(network, constraint, results.getResult(constraint)));
              });
    }

    static Stream<Arguments> marginalObservations() {
      return argumentsOfConstraints(MarginalConstraint.class);
    }

    @ParameterizedTest
    @MethodSource("marginalObservations")
    void checkMarginals_shouldEqualConstraints(
        BayesianNetwork network, MarginalConstraint constraint) {
      test = InferenceEngine.create(network);
      double expected = constraint.getProbability();
      double actual = test.getCurrentProbability(constraint.getEventStates());
      assertEquals(expected, actual, DELTA);
    }

    @ParameterizedTest
    @MethodSource("allObservations")
    void measureProbability_shouldEqualConstraints(
        BayesianNetwork network, ProbabilityConstraint constraint, SolverConstraintResult result) {
      test = InferenceEngine.create(network);
      test.observeNetwork(constraint.getConditionStates());
      double expected = constraint.getProbability();
      double actual = test.getCurrentProbability(constraint.getEventStates());
      assertTrue(result.getLastError() < DELTA);
      assertEquals(expected, actual, DELTA);
    }

    @ParameterizedTest
    @MethodSource("conditionalObservations")
    void checkConditionals_shouldEqualConstraints(
        BayesianNetwork network, ConditionalConstraint constraint) {
      InferenceEngine engine = InferenceEngine.create(network);
      engine.observeNetwork(constraint.getConditionStates());
      double expected = constraint.getProbability();
      double actual = engine.getCurrentProbability(constraint.getEventStates());
      assertEquals(expected, actual, DELTA);
    }
  }
}
