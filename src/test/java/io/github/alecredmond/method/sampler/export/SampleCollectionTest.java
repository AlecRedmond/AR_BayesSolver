package io.github.alecredmond.method.sampler.export;

import static org.junit.jupiter.api.Assertions.*;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.method.network.NetworkScenarios;
import io.github.alecredmond.method.network.export.BayesianNetwork;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SampleCollectionTest {
  static final boolean DEBUG_SOLVE_LENGTHY_TESTS = false;
  static final int NUMBER_OF_SAMPLES = 100_000;
  static List<SamplePackage> packages;

  public static Stream<Arguments> provideSamplePackages() {
    return packages.stream().map(Arguments::of);
  }

  @BeforeAll
  static void init() {
    packages = new ArrayList<>();
    packages.add(
        new SamplePackage(
            NetworkScenarios.RAIN_NETWORK.get(),
            NUMBER_OF_SAMPLES,
            Set.of("WET_GRASS:TRUE"),
            Set.of("SPRINKLER"),
            Set.of("RAIN:TRUE"),
            false));
    packages.add(
        new SamplePackage(
            NetworkScenarios.AH_NETWORK.get(),
            NUMBER_OF_SAMPLES,
            Set.of("H+"),
            Set.of("C", "E"),
            Set.of("F+"),
            true));
    if (!DEBUG_SOLVE_LENGTHY_TESTS) return;
    packages.add(
        new SamplePackage(
            NetworkScenarios.FANTASY_GRAPH.get(),
            NUMBER_OF_SAMPLES,
            Set.of("DISTRICT:OTHER"),
            Set.of("RACE", "WEALTH"),
            Set.of("OUTLOOK:REVOLUTIONARY"),
            true));
  }

  @ParameterizedTest
  @MethodSource("provideSamplePackages")
  void getNetworkObservations(SamplePackage samplePackage) {
    Map<Node, NodeState> observed =
        samplePackage.getObservedStates().stream()
            .map(ns -> Map.entry(ns.getNode(), ns))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    assertEquals(observed, samplePackage.getTest().getNetworkObservations());
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

  @ParameterizedTest
  @MethodSource("provideSamplePackages")
  void size(SamplePackage samplePackage) {
    int numberOfSamples = samplePackage.getNumberOfSamples();
    SampleCollection test = samplePackage.getTest();
    assertEquals(numberOfSamples, test.size());
  }

  @ParameterizedTest
  @MethodSource("provideSamplePackages")
  void setExportNodesById(SamplePackage samplePackage) {
    SampleCollection test = samplePackage.getTest();

    Set<String> includeId = samplePackage.getExportNodeIds();

    test.setExportNodesById(includeId);

    Set<Node> excluded =
        Arrays.stream(test.getNodes())
            .filter(n -> !samplePackage.getExportNodes().contains(n))
            .collect(Collectors.toSet());

    test.getDistinctSamples()
        .forEach(
            sample ->
                Arrays.stream(sample.getExportArray())
                    .map(NodeState::getNode)
                    .forEach(
                        node -> {
                          assertTrue(includeId.contains((String) node.getId()));
                          assertFalse(excluded.contains(node));
                        }));
    test.resetExportNodes();
  }

  @ParameterizedTest
  @MethodSource("provideSamplePackages")
  void setSupplier(SamplePackage samplePackage) {
    SampleCollection test = samplePackage.getTest();
    test.setSupplier(HashSet::new);
    test.getDistinctSamples()
        .forEach(sample -> assertInstanceOf(HashSet.class, sample.getSampleCollection()));
    test.setSupplier(ArrayList::new);
    test.getDistinctSamples()
        .forEach(sample -> assertInstanceOf(ArrayList.class, sample.getSampleCollection()));
  }

  @ParameterizedTest
  @MethodSource("provideSamplePackages")
  void countSamplesWithStateIds(SamplePackage samplePackage) {
    BayesianNetwork network = samplePackage.getNetwork();
    if (samplePackage.isPrintMarginals()) {
      network.printObserved();
      network.printNetwork();
    }
    SampleCollection test = samplePackage.getTest();
    Set<String> measuredStateIds = samplePackage.getMeasuredStateIds();
    int numberOfSamples = samplePackage.getNumberOfSamples();
    double probOfObserved = network.getProbabilityFromCurrentObservations(measuredStateIds);
    double delta = Math.sqrt(numberOfSamples) * 3;
    double lowerBound = probOfObserved * numberOfSamples - delta;
    double upperBound = probOfObserved * numberOfSamples + delta;
    int counted = test.countSamplesWithStateIds(measuredStateIds);
    System.out.printf(
        "EXPECTED LOWER : %d%nEXPECTED UPPER: %d%nACTUAL: %d",
        (int) lowerBound, (int) upperBound, counted);
    assertTrue(counted >= lowerBound);
    assertTrue(counted <= upperBound);
  }
}
