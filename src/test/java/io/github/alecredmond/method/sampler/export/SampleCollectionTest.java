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
import org.junit.jupiter.api.Test;

class SampleCollectionTest {
  static final int NUMBER_OF_SAMPLES = 100_000;
  static final String OBSERVED_STATE_ID = "WET_GRASS:TRUE";
  static BayesianNetwork network;
  static SampleCollection test;
  static NodeState observedState;

  @BeforeAll
  static void init() {
    network = NetworkScenarios.RAIN_NETWORK.get().solveNetwork().observeNetwork(OBSERVED_STATE_ID);
    test = network.generateSamples(NUMBER_OF_SAMPLES);
    observedState = network.getNodeState(OBSERVED_STATE_ID);
  }

  @Test
  void getNetworkObservations() {
    Map<Node, NodeState> observed = Map.of(observedState.getNode(), observedState);
    assertEquals(observed, test.getNetworkObservations());
  }

  @Test
  void getNodes() {
    Node[] nodes = network.getNetworkData().getNodes().toArray(new Node[0]);
    Node[] testNodes = test.getNodes();
    IntStream.range(0, nodes.length).forEach(i -> assertEquals(nodes[i], testNodes[i]));
  }

  @Test
  void isEmpty() {
    assertFalse(test.isEmpty());
  }

  @Test
  void size() {
    assertEquals(NUMBER_OF_SAMPLES, test.size());
  }

  @Test
  void getDistinctSamples() {
    System.out.println(test.toString());
    assertEquals(4, test.getDistinctSamples().size());
  }

  @Test
  void setExportNodesById() {
    Set<String> includeId = Set.of("SPRINKLER");
    test.setExportNodesById(includeId);
    Set<Node> excluded =
        Stream.of("RAIN", "WET_GRASS").map(network::getNode).collect(Collectors.toSet());
    test.getDistinctSamples()
        .forEach(
            sample -> {
              Arrays.stream(sample.getExportArray())
                  .map(NodeState::getNode)
                  .forEach(
                      node -> {
                        assertTrue(includeId.contains((String) node.getId()));
                        assertFalse(excluded.contains(node));
                      });
            });
    test.resetExportNodes();
  }

  @Test
  void setSupplier() {
    test.setSupplier(HashSet::new);
    test.getDistinctSamples()
        .forEach(sample -> assertInstanceOf(HashSet.class, sample.getSampleCollection()));
    test.setSupplier(ArrayList::new);
    test.getDistinctSamples()
        .forEach(sample -> assertInstanceOf(ArrayList.class, sample.getSampleCollection()));
  }

  @Test
  void countSamplesWithStateIds() {
    String id = "SPRINKLER:TRUE";
    double probOfObserved = network.getProbabilityFromCurrentObservations(List.of(id));
    double delta = Math.sqrt(NUMBER_OF_SAMPLES) * 3;
    double lowerBound = probOfObserved * NUMBER_OF_SAMPLES - delta;
    double upperBound = probOfObserved * NUMBER_OF_SAMPLES + delta;
    int counted = test.countSamplesWithStateIds(List.of(id));
    assertTrue(counted >= lowerBound);
    assertTrue(counted <= upperBound);
  }
}
