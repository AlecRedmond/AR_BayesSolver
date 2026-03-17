package io.github.alecredmond.export.method.inference;

import static io.github.alecredmond.TestConfigs.PRINT_TABLES;
import static io.github.alecredmond.TestConfigs.SOLVE_LONG_TESTS;
import static io.github.alecredmond.method.network.NetworkScenarios.*;
import static org.junit.jupiter.api.Assertions.*;

import io.github.alecredmond.export.application.probabilitytables.MarginalTable;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.export.method.sampler.SampleCollection;
import java.util.Arrays;
import java.util.List;

import io.github.alecredmond.export.method.sampler.Sampler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class InferenceEngineTest {
  BayesianNetwork net;
  InferenceEngine test;

  @BeforeEach
  void init() {
    net = RAIN_NETWORK.get();
    test = net.buildInferenceEngine();
  }

  @Test
  void observeNetwork_shouldUpdateProbabilities() {
    InferenceEngine engine = InferenceEngine.create(net).resetObservations();
    assertEquals(0.2, engine.getObservedTableById("RAIN").getProbabilityFromId("RAIN:TRUE"), 1E-6);
    engine.observeNetworkFromIds("WET_GRASS:TRUE");
    double pRainGivenWet = net.getMarginalTable("RAIN").getProbabilityFromId("RAIN:TRUE");
    assertTrue(pRainGivenWet > 0.2);
    // Exact value P(R|W) = P(W|R)P(R)/P(W)
    // P(W|R) = P(W|R,S)P(S|R) + P(W|R,~S)P(~S|R) = 0.99*0.01 + 0.9*0.99 = 0.9009
    // P(W|~R) = P(W|~R,S)P(S|~R) + P(W|~R,~S)P(~S|~R) = 0.9*0.4 + 0.0*0.6 = 0.36
    // P(W) = P(W|R)P(R) + P(W|~R)P(~R) = 0.9009*0.2 + 0.36*0.8 = 0.18018 + 0.288 = 0.46818
    // P(R|W) = (0.9009 * 0.2) / 0.46818 = 0.18018 / 0.46818 = 0.38485
    assertEquals(0.384852, pRainGivenWet, 1E-6);
  }

  @Test
  void observeNetwork_withConflictingEvidence_shouldThrowException() {
    assertThrows(
        Exception.class, () -> test.observeNetworkFromIds(List.of("RAIN:TRUE", "RAIN:FALSE")));
  }

  @Test
  void observeNetwork_withNonExistentState_shouldThrowException() {
    assertThrows(Exception.class, () -> test.observeNetworkFromIds(List.of("ZOMBIE:TRUE")));
  }

  @Test
  void observeNetwork_withEmptyList_shouldBeSameAsObserveMarginals() {
    double pRainMarginal = net.getMarginalTable("RAIN").getProbabilityFromId("RAIN:TRUE");

    test.observeNetwork(List.of());
    double pRainObservedEmpty = net.getMarginalTable("RAIN").getProbabilityFromId("RAIN:TRUE");

    assertEquals(pRainMarginal, pRainObservedEmpty);
  }

  @Test
  void getNetworkTable_shouldReturnTable() {
    net.solveNetwork();
    ProbabilityTable rainTable = net.getNetworkTable("RAIN");
    assertNotNull(rainTable);
    assertEquals(1, rainTable.getNodes().size());

    ProbabilityTable grassTable = net.getNetworkTable("WET_GRASS");
    assertNotNull(grassTable);
    assertEquals(3, grassTable.getNodes().size());
  }

  @Test
  void getNetworkTable_nonExistentNode_shouldThrowException() {
    net.solveNetwork();
    assertThrows(Exception.class, () -> net.getNetworkTable("ZOMBIE"));
  }

  @Test
  void getNetworkTable_beforeSolve_shouldImplicitlySolve() {
    assertDoesNotThrow(
        () -> {
          ProbabilityTable rainTable = net.getNetworkTable("RAIN");
          assertNotNull(rainTable);
        });
  }

  @Test
  void getObservedTable_shouldReturnTable() {
    test.observeNetworkFromIds(List.of("WET_GRASS:TRUE"));
    MarginalTable rainTable = net.getMarginalTable("RAIN");
    assertNotNull(rainTable);
    assertEquals(0.384852, rainTable.getProbabilityFromId("RAIN:TRUE"), 1E-6);
  }

  @Test
  void getObservedTable_nonExistentNode_shouldThrowException() {
    test.observeNetworkFromIds(List.of("WET_GRASS:TRUE"));
    assertThrows(Exception.class, () -> net.getMarginalTable("ZOMBIE"));
  }

  @Test
  void getObservedTable_beforeObserve_shouldReturnMarginals() {
    net.solveNetwork();
    MarginalTable rainTable = net.getMarginalTable("RAIN");
    assertNotNull(rainTable);
    assertEquals(0.2, rainTable.getProbabilityFromId("RAIN:TRUE"), 1E-6);
  }

  @Test
  void observeProbability_singleEvent_shouldReturnCorrectProb() {
    test.resetObservations();
    double pRainTrue = test.getCurrentConditionalProbabilityById(List.of("RAIN:TRUE"));
    assertEquals(0.2, pRainTrue, 1E-9);
  }

  @Test
  void observeProbability_jointEvent_shouldReturnCorrectProb() {
    test.resetObservations();
    // P(RAIN:TRUE, SPRINKLER:FALSE) = P(S:F | R:T) * P(R:T)
    // P(S:T | R:T) = 0.01, so P(S:F | R:T) = 0.99
    // P(R:T, S:F) = 0.99 * 0.2 = 0.198
    double pJoint =
        test.getCurrentConditionalProbabilityById(List.of("RAIN:TRUE", "SPRINKLER:FALSE"));
    assertEquals(0.198, pJoint, 1E-9);
  }

  @Test
  void observeProbability_conflictingEvent_shouldReturnZero() {
    test.resetObservations();
    double pConflict =
        test.getCurrentConditionalProbabilityById(List.of("RAIN:TRUE", "RAIN:FALSE"));
    assertEquals(0.0, pConflict, 1E-9);
  }

  @Test
  void observeProbability_emptyEvent_shouldReturnOne() {
    double pEmpty = test.getCurrentConditionalProbabilityById(List.of());
    assertEquals(1.0, pEmpty, 1E-9);
  }

  @Nested
  class ScenarioTests {
    static final int NUMBER_OF_SAMPLES = 100_000;
    static final int STANDARD_DEVIATIONS = 3;
    InferenceEngine engine;

    @Test
    void testSolves_RainSprinkler() {
      engine = RAIN_NETWORK.get().solveNetwork().buildInferenceEngine();
      if (PRINT_TABLES) net.printNetwork();
      engine.observeNetworkFromIds(List.of("WET_GRASS:TRUE"));
      if (PRINT_TABLES) net.printMarginals();

      net.getNetworkData().getNetworkTablesMap().values().stream()
          .map(ProbabilityTable::getVector)
          .map(ProbabilityVector::getNodeArray)
          .forEach(
              nodes -> {
                StringBuilder sb = new StringBuilder();
                Arrays.stream(nodes)
                    .forEach(node -> sb.append(node.getId().toString()).append(" "));
                System.out.println(sb);
              });

      engine.resetObservations();

      String testState = "RAIN:TRUE";
      String includedNode = "RAIN";

      generateSamples(engine, net, includedNode, testState);

      engine.observeNetworkFromIds(List.of("WET_GRASS:TRUE"));
      System.out.println("\n--- Now testing P(RAIN:TRUE | WET_GRASS:TRUE) ---");
      generateSamples(engine, net, includedNode, testState);
    }

    private void generateSamples(InferenceEngine engine, BayesianNetwork network, String includedNode, String testState) {

      double observedProb = engine.getCurrentConditionalProbabilityById(List.of(testState));
      double expected = observedProb * NUMBER_OF_SAMPLES;
      double expectedDelta = Math.sqrt(NUMBER_OF_SAMPLES) * STANDARD_DEVIATIONS;
      long lowerBound = Math.max(0, (long) (expected - expectedDelta));
      long upperBound = (long) (expected + expectedDelta);



      SampleCollection sampleCollection =  Sampler.create(network).generateSamples(NUMBER_OF_SAMPLES);
      sampleCollection.setExportNodesById(List.of(includedNode));
      int count = sampleCollection.countSamplesWithStateIds(List.of(testState));

      System.out.printf(
          "Test State: %s%nExpected: %.2f (%.0f samples)%nAllowed Range: [%d, %d]%nActual Sample Count: %d%n",
          testState, observedProb, expected, lowerBound, upperBound, count);

      assertTrue(
          count >= lowerBound && count <= upperBound,
          String.format(
              "Sample count %d for %s is outside expected range [%d, %d]",
              count, testState, lowerBound, upperBound));
    }

    @Test
    void testNetworkAH_NonLocalConstraints() {
      assertDoesNotThrow(() -> engine = AH_NETWORK.get().solveNetwork().buildInferenceEngine());

      if (PRINT_TABLES) net.printNetwork().printMarginals();

      String testState = "B+";
      String includedNode = "B";
      generateSamples(engine, net, includedNode, testState);
    }

    @Test
    void testFantasyGraph_ComplexNetwork() {
      if (!SOLVE_LONG_TESTS) return;
      assertDoesNotThrow(() -> engine = FANTASY_GRAPH.get().buildInferenceEngine());
      if (PRINT_TABLES) net.printMarginals();
      engine.observeNetworkFromIds(List.of("VOTE:CPK"));
      if (PRINT_TABLES) net.printMarginals();
      engine.observeNetworkFromIds(List.of("VOTE:UNF"));
      if (PRINT_TABLES) net.printMarginals();
      engine.observeNetworkFromIds(List.of("RACE:ANK", "AGE:YOUNG_ADULT"));
      if (PRINT_TABLES) {
        net.printMarginals();
        net.printNetwork();
      }

      String testState = "VOTE:CPK";
      String includedNode = "VOTE";

      generateSamples(engine, net, includedNode, testState);
    }
  }
}
