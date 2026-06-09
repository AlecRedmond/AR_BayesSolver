package io.github.alecredmond.export.method.inference;

import static io.github.alecredmond.export.method.network.NetworkScenario.RAIN_NETWORK;
import static org.junit.jupiter.api.Assertions.*;

import io.github.alecredmond.export.application.inference.SolverResults;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.export.method.network.NetworkScenario;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class NewInferenceEngineTest {
  static final List<NetworkScenario> scenarios = provideScenarios();
  InferenceEngine test;

  private static List<NetworkScenario> provideScenarios() {
    return Arrays.stream(NetworkScenario.values())
        .filter(scenario -> !scenario.equals(NetworkScenario.FANTASY_GRAPH))
        .toList();
  }

  @Nested
  class ObservationTests {
    static final double DELTA = 1e-3;

    public static Stream<Arguments> allObservations() {
      return scenarios.stream()
          .map(
              scenario -> {
                BayesianNetwork network = scenario.get();
                BayesSolver solver = BayesSolver.create(network);
                solver.solve();
                return Arguments.of(network, solver.getResults());
              });
    }

    @ParameterizedTest
    @MethodSource("allObservations")
    void measureProbability_shouldEqualConstraints(BayesianNetwork network, SolverResults results) {
      test = InferenceEngine.create(network);
      // network.printNetwork();
      assertTrue(network.isSolved());
      network
          .getNetworkData()
          .getConstraints()
          .forEach(
              constraint -> {
                test.observeNetwork(constraint.getConditionStates());
                double expected = constraint.getProbability();
                double actual = test.getCurrentProbability(constraint.getEventStates());
                assertTrue(results.getResult(constraint).getLastError() < DELTA);
                assertEquals(expected, actual, DELTA);
              });
    }
  }

  @Nested
  class CoherenceChecks {
    static List<ArgumentProvider> providers;

    @BeforeAll
    static void initProvider() {
      providers = new ArrayList<>();
      providers.add(
          new ArgumentProvider(
              RAIN_NETWORK.getSupplier(),
              "SPRINKLER",
              "WET_GRASS",
              "CLOUDY",
              Map.of("CLOUDY", List.of("CLOUDY:TRUE", "CLOUDY:FALSE")),
              List.of(
                  new ConstraintAdder("RAIN:TRUE", List.of("CLOUDY:TRUE"), 0.5),
                  new ConstraintAdder("RAIN:TRUE", List.of("CLOUDY:FALSE"), 0.1)),
              List.of("SPRINKLER:TRUE"),
              List.of("CLOUDY:TRUE"),
              List.of("WET_GRASS:TRUE")));
    }

    static Stream<Arguments> getArguments() {
      return providers.stream().map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("getArguments")
    void standardInference_shouldSucceed(ArgumentProvider provider) {
      test = InferenceEngine.create(provider.supplier.get());
      assertDoesNotThrow(() -> test.getCurrentObservations());
      assertDoesNotThrow(() -> test.observeNetworkFromIds(provider.evidenceAlwaysSucceeds));
      assertNotNull(test.getObservedTableById(provider.controlNodeId));
      assertNotNull(test.copyObservedTableById(provider.controlNodeId));
      assertEquals(1.0, test.getCurrentProbabilityById(provider.evidenceAlwaysSucceeds));
      assertNull(test.getObservedTableById(provider.addedNodeId));
    }

    @ParameterizedTest
    @MethodSource("getArguments")
    void removingNodes_ShouldSucceed(ArgumentProvider provider) {
      BayesianNetwork network = provider.supplier.get();
      test = InferenceEngine.create(network);
      assertDoesNotThrow(() -> test.getCurrentObservations());
      assertDoesNotThrow(() -> test.observeNetworkFromIds(provider.evidenceAlwaysSucceeds));
      network.removeNodeByID(provider.removedNodeId);
      assertDoesNotThrow(() -> test.getCurrentObservations());
      assertDoesNotThrow(() -> test.observeNetworkFromIds(provider.evidenceAlwaysSucceeds));
      assertEquals(1.0, test.getCurrentProbabilityById(provider.evidenceAlwaysSucceeds));
      assertNull(test.getObservedTableById(provider.removedNodeId));
    }

    @ParameterizedTest
    @MethodSource("getArguments")
    void addingNodes_shouldSucceed(ArgumentProvider provider) {
      BayesianNetwork network = provider.supplier.get();
      test = InferenceEngine.create(network);
      assertDoesNotThrow(() -> test.getCurrentObservations());
      assertDoesNotThrow(() -> test.observeNetworkFromIds(provider.evidenceAlwaysSucceeds));
      provider.addedNodesMap.forEach(
          (nodeId, stateIds) -> {
            network.addNewNode(nodeId, stateIds);
            network.addParents(nodeId, provider.controlNodeId);
          });
      provider.addedNodeConstraints.forEach(
          added -> network.addConstraint(added.eventId, added.conditionIds, added.prob));
      assertDoesNotThrow(() -> test.getCurrentObservations());
      assertDoesNotThrow(() -> test.observeNetworkFromIds(provider.evidenceWithNewNodes));
      assertEquals(1.0, test.getCurrentProbabilityById(provider.evidenceWithNewNodes));
      assertNotNull(test.getObservedTableById(provider.addedNodeId));
    }

    @AllArgsConstructor
    static class ConstraintAdder {
      String eventId;
      List<String> conditionIds;
      double prob;
    }

    @AllArgsConstructor
    static class ArgumentProvider {
      Supplier<BayesianNetwork> supplier;
      String controlNodeId;
      String removedNodeId;
      String addedNodeId;
      Map<String, List<String>> addedNodesMap;
      List<ConstraintAdder> addedNodeConstraints;
      List<String> evidenceAlwaysSucceeds;
      List<String> evidenceWithNewNodes;
      List<String> evidenceWithRemovedNodes;
    }
  }
}
