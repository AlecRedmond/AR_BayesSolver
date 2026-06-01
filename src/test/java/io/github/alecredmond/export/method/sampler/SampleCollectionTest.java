package io.github.alecredmond.export.method.sampler;

import static io.github.alecredmond.TestConfigs.*;
import static io.github.alecredmond.export.method.network.NetworkScenario.*;
import static org.junit.jupiter.api.Assertions.*;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.inference.InferenceEngine;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.io.Serializable;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SampleCollectionTest {
  static List<SamplePackage> packages;

  static Stream<Arguments> provideSamplePackages() {
    return packages.stream().map(Arguments::of);
  }

  @BeforeAll
  static void init() {
    packages = new ArrayList<>();
    packages.add(
        new SamplePackage(
            AH_NETWORK.get(),
            NUMBER_OF_SAMPLES,
            Set.of("H+"),
            Set.of("C", "E"),
            Set.of("F+"),
            true));
    packages.add(
        new SamplePackage(
            SIMPLE_LINEAR.get(),
            NUMBER_OF_SAMPLES,
            Set.of(),
            Set.of("B", "C"),
            Set.of("D+"),
            true));
    packages.add(
        new SamplePackage(
            RAIN_NETWORK.get(),
            NUMBER_OF_SAMPLES,
            Set.of("WET_GRASS:TRUE"),
            Set.of("SPRINKLER"),
            Set.of("RAIN:TRUE"),
            false));
    if (!SOLVE_LONG_TESTS) return;
    packages.add(
        new SamplePackage(
            FANTASY_GRAPH.get(),
            NUMBER_OF_SAMPLES,
            Set.of("DISTRICT:OTHER"),
            Set.of("RACE", "WEALTH"),
            Set.of("OUTLOOK:REVOLUTIONARY"),
            true));
  }

  @Nested
  class Utils {
    public static Stream<Arguments> provideSamplePackages() {
      return SampleCollectionTest.provideSamplePackages();
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void getObservations(SamplePackage samplePackage) {
      Map<Node, NodeState> observed =
          samplePackage.getObservedStates().stream()
              .map(ns -> Map.entry(ns.getNode(), ns))
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
      assertEquals(observed, samplePackage.getTest().getObservations());
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void getNodes(SamplePackage samplePackage) {
      BayesianNetwork network = samplePackage.getNetwork();
      SampleCollection test = samplePackage.getTest();
      Node[] nodes = network.getNetworkData().getNodes().toArray(new Node[0]);
      Node[] testNodes = test.getNodes();
      IntStream.range(0, nodes.length).forEach(i -> assertEquals(nodes[i], testNodes[i]));
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void isEmpty(SamplePackage samplePackage) {
      SampleCollection test = samplePackage.getTest();
      assertFalse(test.isEmpty());
    }
  }

  @Nested
  class CountSampleTests {

    static Stream<Arguments> provideSamplePackages() {
      return SampleCollectionTest.provideSamplePackages();
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void countSamplesIncludingStateIds_multipleIDs(SamplePackage samplePackage) {
      Set<String> measuredStateIds = samplePackage.getMeasuredStateIds();
      measureCount(
          samplePackage,
          sc -> sc.countSamplesIncludingStateIds(measuredStateIds),
          e -> e.getCurrentProbabilityById(measuredStateIds));
    }

    void measureCount(
        SamplePackage samplePackage,
        ToIntFunction<SampleCollection> countFunc,
        ToDoubleFunction<InferenceEngine> probabilityFunc) {
      int numberOfSamples = samplePackage.getNumberOfSamples();
      double probOfObserved = probabilityFunc.applyAsDouble(samplePackage.getEngine());
      double delta = Math.sqrt(numberOfSamples) * ALLOWED_STDEV;
      double expected = probOfObserved * numberOfSamples;
      int counted = countFunc.applyAsInt(samplePackage.getTest());
      assertEquals(expected, counted, delta);
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void countSamplesIncludingStates_multipleStates(SamplePackage samplePackage) {
      Set<NodeState> measuredStates = samplePackage.getMeasuredStates();
      measureCount(
          samplePackage,
          sc -> sc.countSamplesIncludingStates(measuredStates),
          e -> e.getCurrentProbability(measuredStates));
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void countSamplesIncludingStates_singleState(SamplePackage samplePackage) {
      NodeState measuredState =
          samplePackage.getMeasuredStates().stream().findFirst().orElseThrow();
      measureCount(
          samplePackage,
          sc -> sc.countSamplesIncludingStates(measuredState),
          e -> e.getCurrentProbability(measuredState));
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void countSamplesIncludingStateIds_singleID(SamplePackage samplePackage) {
      String singleId = samplePackage.getMeasuredStateIds().stream().findFirst().orElseThrow();
      measureCount(
          samplePackage,
          sc -> sc.countSamplesIncludingStateIds(singleId),
          e -> e.getCurrentProbabilityById(singleId));
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void countSamples(SamplePackage samplePackage) {
      int numberOfSamples = samplePackage.getNumberOfSamples();
      SampleCollection test = samplePackage.getTest();
      assertEquals(numberOfSamples, test.countSamples());
    }
  }

  @Nested
  class GetSamples {
    static Stream<Arguments> provideSamplePackages() {
      return SampleCollectionTest.provideSamplePackages();
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void getSamplesIncludingStates_multipleStates(SamplePackage samplePackage) {
      Set<NodeState> measuredStates = samplePackage.getMeasuredStates();
      checkSamplesInRange(
          samplePackage,
          sc -> sc.getSamplesIncludingStates(measuredStates),
          set -> set.containsAll(measuredStates));
    }

    void checkSamplesInRange(
        SamplePackage samplePackage,
        Function<SampleCollection, List<Sample>> getSamples,
        Predicate<Set<NodeState>> assertion) {
      getSamples
          .apply(samplePackage.getTest())
          .forEach(sample -> assertTrue(assertion.test(sample.getDisplayedStates(HashSet::new))));
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void getSamplesIncludingStates_singleState(SamplePackage samplePackage) {
      NodeState state = samplePackage.getMeasuredStates().stream().findFirst().orElseThrow();
      checkSamplesInRange(
          samplePackage, sc -> sc.getSamplesIncludingStates(state), set -> set.contains(state));
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void getSamplesIncludingStateByIds_multipleIds(SamplePackage samplePackage) {
      Set<NodeState> measuredStates = samplePackage.getMeasuredStates();
      Set<String> measuredStateIds = samplePackage.getMeasuredStateIds();
      checkSamplesInRange(
          samplePackage,
          sc -> sc.getSamplesIncludingStatesById(measuredStateIds),
          set -> set.containsAll(measuredStates));
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void getSamplesIncludingStateByIds_singleState(SamplePackage samplePackage) {
      NodeState state = samplePackage.getMeasuredStates().stream().findFirst().orElseThrow();
      Serializable id = state.getId();
      checkSamplesInRange(
          samplePackage, sc -> sc.getSamplesIncludingStatesById(id), set -> set.contains(state));
    }
  }

  @Nested
  class SetDisplayedNodes {
    public static Stream<Arguments> provideSamplePackages() {
      return SampleCollectionTest.provideSamplePackages();
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void setDisplayedNodes_multipleNodes(SamplePackage samplePackage) {
      Set<Node> displayed = samplePackage.getExportNodes();
      assertDisplayedOnly(samplePackage, s -> s.setDisplayedNodes(displayed), displayed);
    }

    void assertDisplayedOnly(
        SamplePackage samplePackage,
        Consumer<SampleCollection> setDisplayFunc,
        Set<Node> expectedDisplay) {
      SampleCollection test = samplePackage.getTest();
      setDisplayFunc.accept(test);
      test.getSamples()
          .forEach(
              sample ->
                  assertEquals(
                      NodeUtils.getNodes(sample.getDisplayedStates(HashSet::new)),
                      expectedDisplay));
      test.displayAllNodes();
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void display_all_nodes(SamplePackage samplePackage) {
      Set<Node> displayed = new HashSet<>(samplePackage.getNetwork().getNetworkData().getNodes());
      assertDisplayedOnly(samplePackage, s -> s.setDisplayedNodes(displayed), displayed);
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void setDisplayedNodesById_multipleNodeIds(SamplePackage samplePackage) {
      Set<Node> displayed = samplePackage.getExportNodes();
      List<Serializable> ids = NodeUtils.getNodeIds(displayed);
      assertDisplayedOnly(samplePackage, s -> s.setDisplayedNodesById(ids), displayed);
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void setDisplayedNodes_singleNode(SamplePackage samplePackage) {
      Node displayed = samplePackage.getExportNodes().stream().findFirst().orElseThrow();
      assertDisplayedOnly(samplePackage, s -> s.setDisplayedNodes(displayed), Set.of(displayed));
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void setDisplayedNodesById_singleNodeId(SamplePackage samplePackage) {
      Node displayed = samplePackage.getExportNodes().stream().findFirst().orElseThrow();
      assertDisplayedOnly(
          samplePackage, s -> s.setDisplayedNodesById(displayed.getId()), Set.of(displayed));
    }
  }

  @Nested
  class SampleMethods {
    static Stream<Arguments> provideSamplePackages() {
      return SampleCollectionTest.provideSamplePackages();
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void countAll(SamplePackage samplePackage) {
      SampleCollection test = samplePackage.getTest();
      int countExpected = test.countSamples();
      int countActual = Sample.countAll(test.getSamples());
      assertEquals(countExpected, countActual);
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void getDisplayedStates(SamplePackage samplePackage) {
      SampleCollection test = samplePackage.getTest();
      Set<Node> exportNodes = samplePackage.getExportNodes();
      test.setDisplayedNodes(exportNodes);
      test.getSamples()
          .forEach(
              sample ->
                  assertTrue(
                      Arrays.stream(sample.getDisplayedStates())
                          .map(NodeState::getNode)
                          .allMatch(exportNodes::contains)));
    }
  }
}
