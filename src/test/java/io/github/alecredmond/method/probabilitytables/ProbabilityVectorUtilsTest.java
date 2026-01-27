package io.github.alecredmond.method.probabilitytables;

import static org.junit.jupiter.api.Assertions.*;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.probabilityvector.ProbabilityVector;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProbabilityVectorUtilsTest {
  ProbabilityVectorUtils test;
  ProbabilityVector vector;

  @BeforeEach
  void buildBasicProbabilityVector() {
    Node rain = new Node("RAIN", List.of("RAIN:TRUE", "RAIN:FALSE"));
    Node sprinkler = new Node("SPRINKLER", List.of("SPRINKLER:TRUE", "SPRINKLER:FALSE"));
    Node wetGrass = new Node("WET_GRASS", List.of("WET_GRASS:TRUE", "WET_GRASS:FALSE"));
    Set<Node> nodes = new LinkedHashSet<>();
    nodes.add(rain);
    nodes.add(sprinkler);
    nodes.add(wetGrass);
    vector = new ProbabilityVectorBuilder().build(nodes);

    double[] probs = vector.getProbability();

    probs[0] = 1.00; // 0 R+ S+ W+ = 1.00
    probs[1] = 0.00; // 1 R+ S+ W- = 0.00
    probs[2] = 0.95; // 2 R+ S- W+ = 0.95
    probs[3] = 0.05; // 3 R+ S- W- = 0.05
    probs[4] = 0.90; // 4 R- S+ W+ = 0.90
    probs[5] = 0.10; // 5 R- S+ W- = 0.10
    probs[6] = 0.15; // 6 R- S- W+ = 0.15
    probs[7] = 0.85; // 7 R- S- W- = 0.85

    test = new ProbabilityVectorUtils(vector);
  }

  @Test
  void sumProbabilitiesWithStates() {
    Map<Node, NodeState> request = new HashMap<>();
    NodeState rainTrue = vector.getNodes()[0].getNodeStates().getFirst();
    NodeState sprinklerTrue = vector.getNodes()[1].getNodeStates().getFirst();
    request.put(rainTrue.getNode(), rainTrue);
    request.put(sprinklerTrue.getNode(), sprinklerTrue);

    double sum = test.sumProbabilitiesWithStates(request);

    assertEquals(1.0, sum, 1e-6);

    request.remove(rainTrue.getNode());

    sum = test.sumProbabilitiesWithStates(request);

    assertEquals(2.0, sum, 1e-6);
  }

  @Test
  void collectIndexesWithStates() {
    Map<Node, NodeState> request = new HashMap<>();
    NodeState rainTrue = vector.getNodes()[0].getNodeStates().getFirst();
    NodeState sprinklerTrue = vector.getNodes()[1].getNodeStates().getFirst();
    request.put(rainTrue.getNode(), rainTrue);
    request.put(sprinklerTrue.getNode(), sprinklerTrue);

    List<Integer> collectedIndexes = test.collectIndexesWithStates(request);
    List<Integer> expectedIndexes = new ArrayList<>(List.of(0, 1));

    assertTrue(collectedIndexes.containsAll(expectedIndexes));

    expectedIndexes.addAll(List.of(4, 5));
    request.remove(rainTrue.getNode());

    collectedIndexes = test.collectIndexesWithStates(request);
    assertTrue(collectedIndexes.containsAll(expectedIndexes));
  }

    @Test
    void generateStateCombinations() {

    }
}
