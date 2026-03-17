package io.github.alecredmond.method.network;

import static io.github.alecredmond.method.network.NetworkScenarios.*;
import static org.junit.jupiter.api.Assertions.*;

import io.github.alecredmond.exceptions.BayesNetIDException;
import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.application.constraints.ConditionalConstraint;
import io.github.alecredmond.export.application.constraints.MarginalConstraint;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.MarginalTable;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.export.method.sampler.SampleCollection;
import java.io.Serializable;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BayesianNetworkTest {
  static final int NUMBER_OF_SAMPLES = 100_000;
  static final int STANDARD_DEVIATIONS = 3;
  // Set to false when performing a maven build
  static final boolean DEBUG_SOLVE_LENGTHY_TESTS = false;
  static final boolean PRINT_RESULTS = false;
  BayesianNetwork net;

  @BeforeEach
  void setUp() {
    net = BayesianNetwork.newNetwork("TestNetwork");
  }

  @Nested
  class NetworkCreationTests {
    @Test
    void newNetwork_noName_shouldNotBeNull() {
      BayesianNetwork unnamedNet = BayesianNetwork.newNetwork();
      assertNotNull(unnamedNet);
      assertNotNull(unnamedNet.getNetworkData());
      assertEquals("UNNAMED NETWORK", unnamedNet.getNetworkData().getNetworkName());
    }

    @Test
    void newNetwork_withName_shouldNotBeNullAndHaveName() {
      assertNotNull(net);
      assertNotNull(net.getNetworkData());
      assertEquals("TestNetwork", net.getNetworkData().getNetworkName());
    }
  }

  @Nested
  class NodeManipulationTests {

    @Test
    void addNode_singleNode_shouldSucceed() {
      net.addNewNode("A");
      BayesianNetworkData data = net.getNetworkData();
      assertNotNull(net.getNode("A"));
      assertEquals(1, data.getNodeIDsMap().size());

      net.addNode(new Node("B", List.of("B+", "B-")));
      assertNotNull(net.getNode("B"));
      assertEquals(2, data.getNodeIDsMap().size());
    }

    @Test
    void addNode_duplicateID_shouldThrowException() {
      net.addNewNode("A");
      assertThrows(IllegalArgumentException.class, () -> net.addNewNode("A"));
      assertThrows(IllegalArgumentException.class, () -> net.addNode(new Node("A")));
    }

    @Test
    void addNode_nullID_shouldThrowException() {
      assertThrows(Exception.class, () -> net.addNode(null));
    }

    @Test
    void addNode_withStates_shouldSucceed() {
      net.addNewNode("A", List.of("A_T", "A_F"));
      Node nodeA = net.getNode("A");

      assertNotNull(nodeA);
      assertEquals(2, nodeA.getNodeStates().size());
      assertNotNull(net.getNodeState("A_T"));
      assertNotNull(net.getNodeState("A_F"));
    }

    @Test
    void addNode_withStates_duplicateNodeID_shouldThrowException() {
      net.addNewNode("A", List.of("A_T", "A_F"));
      assertThrows(
          IllegalArgumentException.class, () -> net.addNewNode("A", List.of("A_T_2", "A_F_2")));
    }

    @Test
    void addNode_withStates_duplicateStateIDInNetwork_shouldThrowException() {
      net.addNewNode("A", List.of("A_T", "A_F"));
      assertThrows(
          IllegalArgumentException.class, () -> net.addNewNode("B", List.of("A_T", "B_F")));
    }

    @Test
    void addNode_withStates_duplicateStateIDInCollection_shouldThrowException() {
      assertThrows(Exception.class, () -> net.addNewNode("B", List.of("B_T", "B_T")));
    }

    @Test
    void addNode_withEmptyStates_shouldSucceed() {
      net.addNewNode("A", List.of());
      Node nodeA = net.getNode("A");
      assertNotNull(nodeA);
      assertTrue(nodeA.getNodeStates().isEmpty());
    }

    @Test
    void addNode_withNullStatesCollection_shouldThrowException() {
      assertThrows(Exception.class, () -> net.addNewNode("A", null));
    }

    @Test
    void removeNode_existingNode_shouldSucceed() {
      net.addNewNode("A");
      assertNotNull(net.getNode("A"));
      net.removeNodeByID("A");
      assertThrows(IllegalArgumentException.class, () -> net.getNode("A"));
      assertTrue(net.getNetworkData().getNodeIDsMap().isEmpty());
    }

    @Test
    void removeNode_nonExistentNode_shouldNotThrowException() {
      net.addNewNode("A");
      assertDoesNotThrow(() -> net.removeNodeByID("B"));
      assertEquals(1, net.getNetworkData().getNodeIDsMap().size());
    }

    @Test
    void removeNode_withNullID_shouldNotThrowException() {
      net.addNewNode("A");
      assertDoesNotThrow(() -> net.removeNode(null));
      assertEquals(1, net.getNetworkData().getNodeIDsMap().size());
    }

    @Test
    void removeNode_shouldAlsoRemoveStatesAndEdges() {
      net.addNewNode("A", List.of("A_T", "A_F"));
      net.addNewNode("B", List.of("B_T", "B_F"));
      net.addParent("B", "A");

      assertNotNull(net.getNode("A"));
      assertNotNull(net.getNodeState("A_T"));
      assertTrue(net.getNode("B").getParents().contains(net.getNode("A")));

      net.removeNodeByID("A");

      assertThrows(IllegalArgumentException.class, () -> net.getNode("A"));
      assertThrows(IllegalArgumentException.class, () -> net.getNodeState("A_T"));
      assertTrue(net.getNode("B").getParents().isEmpty()); // Check edge is gone
    }

    @Test
    void removeAllNodes_shouldSucceed() {
      net.addNewNode("A");
      net.addNewNode("B");
      net.removeAllNodes();
      assertTrue(net.getNetworkData().getNodeIDsMap().isEmpty());
      assertTrue(net.getNetworkData().getNodeStateIDsMap().isEmpty());
    }

    @Test
    void removeAllNodes_onEmptyNetwork_shouldSucceed() {
      assertDoesNotThrow(() -> net.removeAllNodes());
      assertTrue(net.getNetworkData().getNodeIDsMap().isEmpty());
    }

    @Test
    void addNodeState_singleState_shouldSucceed() {
      net.addNewNode("A");
      net.addNodeState("A", "A_T");
      assertEquals(1, net.getNode("A").getNodeStates().size());
      assertNotNull(net.getNodeState("A_T"));
    }

    @Test
    void addNodeStates_multipleStates_shouldSucceed() {
      net.addNewNode("A");
      net.addNodeStates("A", List.of("A_T", "A_F"));
      assertEquals(2, net.getNode("A").getNodeStates().size());
      assertNotNull(net.getNodeState("A_T"));
      assertNotNull(net.getNodeState("A_F"));
    }

    @Test
    void addNodeState_duplicateState_shouldThrowException() {
      net.addNewNode("A", List.of("A_T"));
      assertThrows(BayesNetIDException.class, () -> net.addNodeState("A", "A_T"));
    }

    @Test
    void addNodeState_duplicateStateInNetwork_shouldThrowException() {
      net.addNewNode("A", List.of("A_T"));
      net.addNewNode("B");
      assertThrows(BayesNetIDException.class, () -> net.addNodeState("B", "A_T"));
    }

    @Test
    void addNodeState_toNonExistentNode_shouldThrowException() {
      assertThrows(Exception.class, () -> net.addNodeState("Z", "Z_T"));
    }

    @Test
    void addNodeState_nullState_shouldThrowException() {
      net.addNewNode("A");
      assertThrows(Exception.class, () -> net.addNodeState("A", null));
    }

    @Test
    void removeNodeState_shouldSucceed() {
      net.addNewNode("A", List.of("A_T", "A_F"));
      assertEquals(2, net.getNode("A").getNodeStates().size());

      net.removeNodeState("A", "A_T");
      assertEquals(1, net.getNode("A").getNodeStates().size());
      assertThrows(IllegalArgumentException.class, () -> net.getNodeState("A_T"));
      assertNotNull(net.getNodeState("A_F"));
    }

    @Test
    void removeNodeState_nonExistentState_shouldNotThrow() {
      net.addNewNode("A", List.of("A_T", "A_F"));
      assertDoesNotThrow(() -> net.removeNodeState("A", "A_M"));
      assertEquals(2, net.getNode("A").getNodeStates().size());
    }

    @Test
    void removeNodeState_fromNonExistentNode_shouldNotThrow() {
      assertDoesNotThrow(() -> net.removeNodeState("Z", "Z_T"));
    }

    @Test
    void removeNodeStates_shouldSucceed() {
      net.addNewNode("A", List.of("A_T", "A_F"));
      assertFalse(net.getNode("A").getNodeStates().isEmpty());

      net.removeNodeStates("A");
      assertTrue(net.getNode("A").getNodeStates().isEmpty());
      assertThrows(IllegalArgumentException.class, () -> net.getNodeState("A_T"));
      assertThrows(IllegalArgumentException.class, () -> net.getNodeState("A_F"));
    }

    @Test
    void removeNodeStates_fromNodeWithNoStates_shouldNotThrow() {
      net.addNewNode("A");
      assertDoesNotThrow(() -> net.removeNodeStates("A"));
    }

    @Test
    void removeNodeStates_fromNonExistentNode_shouldNotThrow() {
      assertDoesNotThrow(() -> net.removeNodeStates("Z"));
    }
  }

  @Nested
  class StructureManipulationTests {

    @BeforeEach
    void buildNodes() {
      net.addNewNode("A");
      net.addNewNode("B");
      net.addNewNode("C");
    }

    @Test
    void addParent_shouldSucceed() {
      net.addParent("B", "A");
      Node nodeB = net.getNode("B");
      Node nodeA = net.getNode("A");
      assertTrue(nodeB.getParents().contains(nodeA));
      assertTrue(nodeA.getChildren().contains(nodeB));
    }

    @Test
    void addParents_shouldSucceed() {
      net.addParents("C", List.of("A", "B"));
      Node nodeC = net.getNode("C");
      Node nodeA = net.getNode("A");
      Node nodeB = net.getNode("B");
      assertTrue(nodeC.getParents().containsAll(List.of(nodeA, nodeB)));
      assertTrue(nodeA.getChildren().contains(nodeC));
      assertTrue(nodeB.getChildren().contains(nodeC));
    }

    @Test
    void addParent_toNonExistentChild_shouldThrowException() {
      assertThrows(Exception.class, () -> net.addParent("Z", "A"));
    }

    @Test
    void addParent_nonExistentParent_shouldThrowException() {
      assertThrows(Exception.class, () -> net.addParent("A", "Z"));
    }

    @Test
    void addParent_createCycle_shouldThrowException() {
      net.addParent("B", "A");
      assertThrows(Exception.class, () -> net.addParent("A", "B"));
    }

    @Test
    void addParent_addSelfAsParent_shouldThrowException() {
      assertThrows(Exception.class, () -> net.addParent("A", "A"));
    }

    @Test
    void removeParent_shouldSucceed() {
      net.addParent("B", "A");
      Node nodeB = net.getNode("B");
      Node nodeA = net.getNode("A");
      assertTrue(nodeB.getParents().contains(nodeA));

      net.removeParent("B", "A");
      assertFalse(nodeB.getParents().contains(nodeA));
      assertFalse(nodeA.getChildren().contains(nodeB));
    }

    @Test
    void removeParent_nonExistentParentRelation_shouldNotThrow() {
      net.addParent("B", "A");
      assertDoesNotThrow(() -> net.removeParent("C", "A"));
      assertDoesNotThrow(() -> net.removeParent("B", "C"));
    }

    @Test
    void removeParents_shouldSucceed() {
      net.addParents("C", List.of("A", "B"));
      Node nodeC = net.getNode("C");
      assertFalse(nodeC.getParents().isEmpty());

      net.removeParents("C");
      assertTrue(nodeC.getParents().isEmpty());
    }

    @Test
    void removeParents_fromNodeWithNoParents_shouldNotThrow() {
      assertDoesNotThrow(() -> net.removeParents("A"));
    }

    @Test
    void removeParents_fromNonExistentNode_shouldThrowException() {
      assertThrows(Exception.class, () -> net.removeParents("Z"));
    }
  }

  @Nested
  class ProbabilityConstraintTests {
    NodeState aT;
    NodeState aF;
    NodeState bT;
    NodeState bF;

    @BeforeEach
    void buildNetwork() {
      Node a = new Node("A", List.of("A_T", "A_F"));
      Node b = new Node("B", List.of("B_T", "B_F"));
      aT = a.getNodeStates().getFirst();
      aF = a.getNodeStates().getLast();
      bT = b.getNodeStates().getFirst();
      bF = b.getNodeStates().getLast();
      net.addNode(a);
      net.addNode(b);
      net.addParent(b, a);
    }

    @Test
    void addConstraint_prior_shouldSucceed() {
      assertDoesNotThrow(() -> net.addConstraint("A_T", 0.2));
      double prob = net.getNetworkData().getConstraints().getFirst().getProbability();
      assertEquals(0.2, prob);
    }

    @Test
    void addConstraint_prior_invalidProbability_shouldThrowException() {
      assertThrows(ConstraintValidationException.class, () -> net.addConstraint("A_T", 1.5));
      assertThrows(ConstraintValidationException.class, () -> net.addConstraint("A_T", -0.5));
    }

    @Test
    void addConstraint_prior_forNonExistentState_shouldThrowException() {
      assertThrows(IllegalArgumentException.class, () -> net.addConstraint("Z_T", 0.5));
    }

    @Test
    void addConstraint_conditional_shouldSucceed() {
      assertDoesNotThrow(() -> net.addConstraint("B_T", List.of("A_T"), 0.8));
      assertDoesNotThrow(() -> net.addConstraint("B_T", List.of("A_F"), 0.1));
      assertDoesNotThrow(() -> net.solveNetwork());
    }

    @Test
    void addConstraint_conditional_invalidProbability_shouldThrowException() {
      assertThrows(
          ConstraintValidationException.class, () -> net.addConstraint("B_T", List.of("A_T"), 1.5));
      assertThrows(
          ConstraintValidationException.class,
          () -> net.addConstraint("B_T", List.of("A_T"), -0.5));
    }

    @Test
    void addConstraint_conditional_nonExistentEventState_shouldThrowException() {
      assertThrows(
          IllegalArgumentException.class, () -> net.addConstraint("Z_T", List.of("A_T"), 0.5));
    }

    @Test
    void addConstraint_conditional_nonExistentConditionState_shouldThrowException() {
      assertThrows(
          IllegalArgumentException.class, () -> net.addConstraint("B_T", List.of("Z_T"), 0.5));
    }

    @Test
    void addConstraint_conditional_withEmptyConditions_shouldSucceedAsPrior() {
      assertDoesNotThrow(() -> net.addConstraint("A_T", List.of(), 0.2));
    }

    @Test
    void addConstraint_invalidConstraintBuilder_shouldThrowException() {
      assertThrows(
          ConstraintValidationException.class,
          () -> net.addConstraint("B_T", List.of("A_T", "B_F"), 0.5));
    }

    @Test
    void addConstraint_nonLocalConstraint_shouldSucceed() {
      assertDoesNotThrow(() -> net.addConstraint("A_T", List.of("B_T"), 0.6));
      assertDoesNotThrow(() -> net.solveNetwork());
    }

    @Test
    void addConstraint_manuallyBuilt_shouldSucceed() {
      buildTestConstraints()
          .forEach(
              constraint -> {
                assertDoesNotThrow(() -> net.addConstraint(constraint));
                assertDoesNotThrow(() -> net.solveNetwork());
              });
    }

    List<ProbabilityConstraint> buildTestConstraints() {
      List<ProbabilityConstraint> probabilityConstraints = new ArrayList<>();
      probabilityConstraints.add(new ConditionalConstraint(aT, List.of(bT), 0.6));
      probabilityConstraints.add(new MarginalConstraint(aF, 0.4));
      probabilityConstraints.add(new ConditionalConstraint(bF, List.of(aF), 0.3));
      return probabilityConstraints;
    }

    @Test
    void addConstraints_manuallyBuilt_shouldSucceed() {
      assertDoesNotThrow(() -> net.addConstraints(buildTestConstraints()));
      assertDoesNotThrow(() -> net.solveNetwork());
    }

    @Test
    void getConstraints_validConstraints_shouldSucceed() {
      List<ProbabilityConstraint> probabilityConstraints = buildTestConstraints();
      net.addConstraints(probabilityConstraints);
      probabilityConstraints.stream()
          .filter(MarginalConstraint.class::isInstance)
          .map(MarginalConstraint.class::cast)
          .forEach(
              marginalConstraint -> {
                Serializable eventStateId = marginalConstraint.getEventState().getId();
                assertNotNull(net.getConstraint(eventStateId));
                assertNotNull(net.getConstraint(eventStateId, List.of()));
                assertEquals(marginalConstraint, net.getConstraint(eventStateId));
              });

      probabilityConstraints.stream()
          .filter(ConditionalConstraint.class::isInstance)
          .map(ConditionalConstraint.class::cast)
          .forEach(
              conditionalConstraint -> {
                Serializable eventStateId = conditionalConstraint.getEventState().getId();
                List<Serializable> conditionStateIDs =
                    conditionalConstraint.getConditionStates().stream()
                        .map(NodeState::getId)
                        .toList();
                assertNotNull(net.getConstraint(eventStateId, conditionStateIDs));
                assertEquals(
                    conditionalConstraint, net.getConstraint(eventStateId, conditionStateIDs));
              });
    }
  }

  @Nested
  class SolverTest {
      @BeforeEach
      void buildNetwork() {
          net = RAIN_NETWORK.get();
      }

      @Test
      void solveNetwork_onValidNetwork_shouldSucceed() {
          assertDoesNotThrow(() -> net.solveNetwork());
      }

      @Test
      void solveNetwork_onEmptyNetwork_shouldSucceed() {
          BayesianNetwork emptyNet = BayesianNetwork.newNetwork();
          assertDoesNotThrow(emptyNet::solveNetwork);
      }

      @Test
      void solveNetwork_withIncompleteConstraints_shouldSucceed() {
          assertDoesNotThrow(() -> net.solveNetwork());
          MarginalTable rainTable = net.getMarginalTable("RAIN");
          assertEquals(0.8, rainTable.getProbabilityFromIDs(List.of("RAIN:FALSE")), 1E-9);
      }
  }

  @Nested
  class ScenarioTests {

    @Test
    void testSolves_RainSprinkler() {
      net = RAIN_NETWORK.get().solveNetwork();
      if (PRINT_RESULTS) net.printNetwork();
      net.observeNetwork(List.of("WET_GRASS:TRUE"));
      if (PRINT_RESULTS) net.printMarginals();

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

      net.observeMarginals();

      String testState = "RAIN:TRUE";
      String includedNode = "RAIN";

      generateSamples(net, includedNode, testState);

      net.observeNetwork(List.of("WET_GRASS:TRUE"));
      System.out.println("\n--- Now testing P(RAIN:TRUE | WET_GRASS:TRUE) ---");
      generateSamples(net, includedNode, testState);
    }

    private void generateSamples(BayesianNetwork network, String includedNode, String testState) {

      double observedProb = network.getProbabilityFromCurrentObservations(List.of(testState));
      double expected = observedProb * NUMBER_OF_SAMPLES;
      double expectedDelta = Math.sqrt(NUMBER_OF_SAMPLES) * STANDARD_DEVIATIONS;
      long lowerBound = Math.max(0, (long) (expected - expectedDelta));
      long upperBound = (long) (expected + expectedDelta);

      SampleCollection sampleCollection = net.generateSamples(NUMBER_OF_SAMPLES);
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
      assertDoesNotThrow(() -> net = AH_NETWORK.get().solveNetwork().observeMarginals());

      if (PRINT_RESULTS) net.printNetwork().printMarginals();

      String testState = "B+";
      String includedNode = "B";
      generateSamples(net, includedNode, testState);
    }

    @Test
    void testFantasyGraph_ComplexNetwork() {
      if (!DEBUG_SOLVE_LENGTHY_TESTS) return;
      net = FANTASY_GRAPH.get();
      assertDoesNotThrow(() -> net.solveNetwork().observeMarginals());
      if (PRINT_RESULTS) net.printMarginals();
      net.observeNetwork(List.of("VOTE:CPK"));
      if (PRINT_RESULTS) net.printMarginals();
      net.observeNetwork(List.of("VOTE:UNF"));
      if (PRINT_RESULTS) net.printMarginals();
      net.observeNetwork(List.of("RACE:ANK", "AGE:YOUNG_ADULT"));
      if (PRINT_RESULTS) {
        net.printMarginals();
        net.printNetwork();
      }

      String testState = "VOTE:CPK";
      String includedNode = "VOTE";

      generateSamples(net, includedNode, testState);
    }
  }
}
