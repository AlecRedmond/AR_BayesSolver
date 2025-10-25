package io.github.alecredmond.method.network;

import static io.github.alecredmond.application.inference.SampleGeneratorType.*;
import static org.junit.jupiter.api.Assertions.*;

import io.github.alecredmond.application.inference.InferenceEngineConfigs;
import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.printer.PrinterConfigs;
import io.github.alecredmond.application.probabilitytables.MarginalTable;
import io.github.alecredmond.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.exceptions.BayesNetIDException;
import io.github.alecredmond.exceptions.ConstraintBuilderException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BayesianNetworkTest {

  boolean debugSolveLengthyTests = true; // Set to false when performing a maven build
  boolean debugPrintSamplesToConsole = false;
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
      assertEquals("UNNAMED_NETWORK", unnamedNet.getNetworkData().getNetworkName());
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
      net.addNode("A");
      BayesianNetworkData data = net.getNetworkData();
      assertNotNull(net.getNode("A"));
      assertEquals(1, data.getNodeIDsMap().size());
    }

    @Test
    void addNode_duplicateID_shouldThrowException() {
      net.addNode("A");
      assertThrows(IllegalArgumentException.class, () -> net.addNode("A"));
    }

    @Test
    void addNode_nullID_shouldThrowException() {
      assertThrows(Exception.class, () -> net.addNode(null));
    }

    @Test
    void addNode_withStates_shouldSucceed() {
      net.addNode("A", List.of("A_T", "A_F"));
      Node nodeA = net.getNode("A");

      assertNotNull(nodeA);
      assertEquals(2, nodeA.getNodeStates().size());
      assertNotNull(net.getNodeState("A_T"));
      assertNotNull(net.getNodeState("A_F"));
    }

    @Test
    void addNode_withStates_duplicateNodeID_shouldThrowException() {
      net.addNode("A", List.of("A_T", "A_F"));
      assertThrows(
          IllegalArgumentException.class, () -> net.addNode("A", List.of("A_T_2", "A_F_2")));
    }

    @Test
    void addNode_withStates_duplicateStateIDInNetwork_shouldThrowException() {
      net.addNode("A", List.of("A_T", "A_F"));
      assertThrows(IllegalArgumentException.class, () -> net.addNode("B", List.of("A_T", "B_F")));
    }

    @Test
    void addNode_withStates_duplicateStateIDInCollection_shouldThrowException() {
      assertThrows(Exception.class, () -> net.addNode("B", List.of("B_T", "B_T")));
    }

    @Test
    void addNode_withEmptyStates_shouldSucceed() {
      net.addNode("A", List.of());
      Node nodeA = net.getNode("A");
      assertNotNull(nodeA);
      assertTrue(nodeA.getNodeStates().isEmpty());
    }

    @Test
    void addNode_withNullStatesCollection_shouldThrowException() {
      assertThrows(Exception.class, () -> net.addNode("A", null));
    }

    @Test
    void removeNode_existingNode_shouldSucceed() {
      net.addNode("A");
      assertNotNull(net.getNode("A"));
      net.removeNode("A");
      assertThrows(IllegalArgumentException.class, () -> net.getNode("A"));
      assertTrue(net.getNetworkData().getNodeIDsMap().isEmpty());
    }

    @Test
    void removeNode_nonExistentNode_shouldNotThrowException() {
      net.addNode("A");
      assertDoesNotThrow(() -> net.removeNode("B"));
      assertEquals(1, net.getNetworkData().getNodeIDsMap().size());
    }

    @Test
    void removeNode_withNullID_shouldNotThrowException() {
      net.addNode("A");
      assertDoesNotThrow(() -> net.removeNode(null));
      assertEquals(1, net.getNetworkData().getNodeIDsMap().size());
    }

    @Test
    void removeNode_shouldAlsoRemoveStatesAndEdges() {
      net.addNode("A", List.of("A_T", "A_F"));
      net.addNode("B", List.of("B_T", "B_F"));
      net.addParent("B", "A");

      assertNotNull(net.getNode("A"));
      assertNotNull(net.getNodeState("A_T"));
      assertTrue(net.getNode("B").getParents().contains(net.getNode("A")));

      net.removeNode("A");

      assertThrows(IllegalArgumentException.class, () -> net.getNode("A"));
      assertThrows(IllegalArgumentException.class, () -> net.getNodeState("A_T"));
      assertTrue(net.getNode("B").getParents().isEmpty()); // Check edge is gone
    }

    @Test
    void removeAllNodes_shouldSucceed() {
      net.addNode("A");
      net.addNode("B");
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
      net.addNode("A");
      net.addNodeState("A", "A_T");
      assertEquals(1, net.getNode("A").getNodeStates().size());
      assertNotNull(net.getNodeState("A_T"));
    }

    @Test
    void addNodeStates_multipleStates_shouldSucceed() {
      net.addNode("A");
      net.addNodeStates("A", List.of("A_T", "A_F"));
      assertEquals(2, net.getNode("A").getNodeStates().size());
      assertNotNull(net.getNodeState("A_T"));
      assertNotNull(net.getNodeState("A_F"));
    }

    @Test
    void addNodeState_duplicateState_shouldThrowException() {
      net.addNode("A", List.of("A_T"));
      assertThrows(BayesNetIDException.class, () -> net.addNodeState("A", "A_T"));
    }

    @Test
    void addNodeState_duplicateStateInNetwork_shouldThrowException() {
      net.addNode("A", List.of("A_T"));
      net.addNode("B");
      assertThrows(BayesNetIDException.class, () -> net.addNodeState("B", "A_T"));
    }

    @Test
    void addNodeState_toNonExistentNode_shouldThrowException() {
      assertThrows(Exception.class, () -> net.addNodeState("Z", "Z_T"));
    }

    @Test
    void addNodeState_nullState_shouldThrowException() {
      net.addNode("A");
      assertThrows(Exception.class, () -> net.addNodeState("A", null));
    }

    @Test
    void removeNodeState_shouldSucceed() {
      net.addNode("A", List.of("A_T", "A_F"));
      assertEquals(2, net.getNode("A").getNodeStates().size());

      net.removeNodeState("A", "A_T");
      assertEquals(1, net.getNode("A").getNodeStates().size());
      assertThrows(IllegalArgumentException.class, () -> net.getNodeState("A_T"));
      assertNotNull(net.getNodeState("A_F"));
    }

    @Test
    void removeNodeState_nonExistentState_shouldNotThrow() {
      net.addNode("A", List.of("A_T", "A_F"));
      assertDoesNotThrow(() -> net.removeNodeState("A", "A_M"));
      assertEquals(2, net.getNode("A").getNodeStates().size());
    }

    @Test
    void removeNodeState_fromNonExistentNode_shouldNotThrow() {
      assertDoesNotThrow(() -> net.removeNodeState("Z", "Z_T"));
    }

    @Test
    void removeNodeStates_shouldSucceed() {
      net.addNode("A", List.of("A_T", "A_F"));
      assertFalse(net.getNode("A").getNodeStates().isEmpty());

      net.removeNodeStates("A");
      assertTrue(net.getNode("A").getNodeStates().isEmpty());
      assertThrows(IllegalArgumentException.class, () -> net.getNodeState("A_T"));
      assertThrows(IllegalArgumentException.class, () -> net.getNodeState("A_F"));
    }

    @Test
    void removeNodeStates_fromNodeWithNoStates_shouldNotThrow() {
      net.addNode("A");
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
      net.addNode("A");
      net.addNode("B");
      net.addNode("C");
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
  class ConstraintTests {

    @BeforeEach
    void buildNetwork() {
      net.addNode("A", List.of("A_T", "A_F"));
      net.addNode("B", List.of("B_T", "B_F"));
      net.addParent("B", "A");
    }

    @Test
    void addConstraint_prior_shouldSucceed() {
      assertDoesNotThrow(() -> net.addConstraint("A_T", 0.2));
      double prob = net.getNetworkData().getConstraints().getFirst().getProbability();
      assertEquals(0.2, prob);
    }

    @Test
    void addConstraint_prior_invalidProbability_shouldThrowException() {
      assertThrows(ConstraintBuilderException.class, () -> net.addConstraint("A_T", 1.5));
      assertThrows(ConstraintBuilderException.class, () -> net.addConstraint("A_T", -0.5));
    }

    @Test
    void addConstraint_prior_forNonExistentState_shouldThrowException() {
      assertThrows(ConstraintBuilderException.class, () -> net.addConstraint("Z_T", 0.5));
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
          ConstraintBuilderException.class, () -> net.addConstraint("B_T", List.of("A_T"), 1.5));
      assertThrows(
          ConstraintBuilderException.class, () -> net.addConstraint("B_T", List.of("A_T"), -0.5));
    }

    @Test
    void addConstraint_conditional_nonExistentEventState_shouldThrowException() {
      assertThrows(
          ConstraintBuilderException.class, () -> net.addConstraint("Z_T", List.of("A_T"), 0.5));
    }

    @Test
    void addConstraint_conditional_nonExistentConditionState_shouldThrowException() {
      assertThrows(
          ConstraintBuilderException.class, () -> net.addConstraint("B_T", List.of("Z_T"), 0.5));
    }

    @Test
    void addConstraint_conditional_withEmptyConditions_shouldSucceedAsPrior() {
      assertDoesNotThrow(() -> net.addConstraint("A_T", List.of(), 0.2));
    }

    @Test
    void addConstraint_invalidConstraintBuilder_shouldThrowException() {
      assertThrows(
          ConstraintBuilderException.class,
          () -> net.addConstraint("B_T", List.of("A_T", "B_F"), 0.5));
    }

    @Test
    void addConstraint_nonLocalConstraint_shouldSucceed() {
      assertDoesNotThrow(() -> net.addConstraint("A_T", List.of("B_T"), 0.6));
      assertDoesNotThrow(() -> net.solveNetwork());
    }
  }

  @Nested
  class SolverConfigTests {
    @Test
    void solverCyclesLimit_shouldSucceed() {
      assertDoesNotThrow(() -> net.solverCyclesLimit(500));
    }

    @Test
    void solverCyclesLimit_invalid_shouldThrowException() {
      assertThrows(IllegalArgumentException.class, () -> net.solverCyclesLimit(0));
      assertThrows(IllegalArgumentException.class, () -> net.solverCyclesLimit(-100));
    }

    @Test
    void solverTimeLimit_shouldSucceed() {
      assertDoesNotThrow(() -> net.solverTimeLimit(30));
    }

    @Test
    void solverTimeLimit_invalid_shouldThrowException() {
      assertThrows(IllegalArgumentException.class, () -> net.solverTimeLimit(0));
      assertThrows(IllegalArgumentException.class, () -> net.solverTimeLimit(-10));
    }

    @Test
    void logIntervalSeconds_shouldSucceed() {
      assertDoesNotThrow(() -> net.logIntervalSeconds(5));
    }

    @Test
    void logIntervalSeconds_invalid_shouldThrowException() {
      assertThrows(IllegalArgumentException.class, () -> net.logIntervalSeconds(0));
      assertThrows(IllegalArgumentException.class, () -> net.logIntervalSeconds(-1));
    }

    @Test
    void solverConvergeThreshold_shouldSucceed() {
      assertDoesNotThrow(() -> net.solverConvergeThreshold(1E-6));
    }

    @Test
    void solverConvergeThreshold_invalid_shouldThrowException() {
      assertThrows(IllegalArgumentException.class, () -> net.solverConvergeThreshold(0.0));
      assertThrows(IllegalArgumentException.class, () -> net.solverConvergeThreshold(-0.1));
    }
  }

  @Nested
  class PrinterConfigTests {
    @Test
    void printerSettings_shouldNotThrow() {
      PrinterConfigs configs = net.getPrinterConfigs();
      assertDoesNotThrow(() -> configs.setSaveDirectory("./output"));
      assertDoesNotThrow(() -> configs.setOpenFileOnCreation(true));
      assertDoesNotThrow(() -> configs.setOpenFileOnCreation(false));
      assertDoesNotThrow(() -> configs.setPrintToConsole(true));
      assertDoesNotThrow(() -> configs.setPrintToConsole(false));
      assertDoesNotThrow(() -> configs.setProbDecimalPlaces(4));
    }

    @Test
    void printerProbDecimalPlaces_invalid_shouldThrowException() {
      PrinterConfigs configs = net.getPrinterConfigs();
      assertThrows(IllegalArgumentException.class, () -> configs.setProbDecimalPlaces(-1));
    }

    @Test
    void printNetwork_beforeSolve_shouldImplicitlySolve() {
      net.addNode("A", List.of("A_T", "A_F")).addConstraint("A_T", 0.2);
      PrinterConfigs configs = net.getPrinterConfigs();
      assertDoesNotThrow(
          () -> {
            configs.setPrintToConsole(true);
            net.printNetwork();
          });
    }

    @Test
    void printObserved_beforeObserve_shouldImplicitlySolveAndObserveMarginals() {
      net.addNode("A", List.of("A_T", "A_F")).addConstraint("A_T", 0.2);
      PrinterConfigs configs = net.getPrinterConfigs();
      assertDoesNotThrow(
          () -> {
            configs.setPrintToConsole(true);
            net.printObserved();
          });
    }
  }

  @Nested
  class InferenceEngineConfigTests {
    InferenceEngineConfigs configs;

    @BeforeEach
    void initializeConfigs() {
      configs = net.getInferenceEngineConfigs();
    }

    @Test
    void changeSamplerType_shouldSucceed() {
      assertDoesNotThrow(() -> configs.setSampleGenerator(LIKELIHOOD_WEIGHTING_SAMPLER));
    }

    @Test
    void changeSolverCyclesLimit_shouldSucceed() {
      assertDoesNotThrow(() -> configs.setSolverCyclesLimit(1000));
      assertDoesNotThrow(() -> configs.setSolverCyclesLimit(10_000));
      assertDoesNotThrow(() -> configs.setSolverCyclesLimit(100_000));
      assertDoesNotThrow(() -> configs.setSolverCyclesLimit(Integer.MAX_VALUE));
    }

    @Test
    void changeSolverCyclesToInvalid_shouldThrowException() {
      List<Integer> invalid = List.of(0, -10, Integer.MIN_VALUE);
      for (Integer i : invalid) {
        assertThrows(IllegalArgumentException.class, () -> configs.setSolverCyclesLimit(i));
      }
    }

    @Test
    void changeSolverConvergeThreshold_shouldSucceed() {
      List<Double> valid = List.of(1E-3, 1E-5, 1E-9, 1E-256, Double.MAX_VALUE);
      for (double d : valid) {
        assertDoesNotThrow(() -> configs.setSolverConvergeThreshold(d));
      }
    }

    @Test
    void changeSolverConvergeInvalid_shouldThrowException() {
      List<Double> invalid = List.of(-1E-3, 0.0, -15.0);
      for (double d : invalid) {
        assertThrows(IllegalArgumentException.class, () -> configs.setSolverConvergeThreshold(d));
      }
    }

    @Test
    void changeLogIntervalToInvalid_shouldThrowException() {
      List<Integer> invalid = List.of(0, -10, Integer.MIN_VALUE);
      for (Integer i : invalid) {
        assertThrows(IllegalArgumentException.class, () -> configs.setSolverLogIntervalSeconds(i));
      }
    }

    @Test
    void changeTimeLimitToInvalid_shouldThrowException() {
      List<Integer> invalid = List.of(0, -10, Integer.MIN_VALUE);
      for (Integer i : invalid) {
        assertThrows(IllegalArgumentException.class, () -> configs.setSolverTimeLimitSeconds(i));
      }
    }
  }

  @Nested
  class InferenceAndQueryTests {

    @BeforeEach
    void buildNetwork() {
      // Using the Rain/Sprinkler example as a base for queries
      net.addNode("RAIN", List.of("RAIN:TRUE", "RAIN:FALSE"))
          .addNode("SPRINKLER", List.of("SPRINKLER:TRUE", "SPRINKLER:FALSE"))
          .addNode("WET_GRASS", List.of("WET_GRASS:TRUE", "WET_GRASS:FALSE"))
          .addParent("SPRINKLER", "RAIN")
          .addParents("WET_GRASS", List.of("SPRINKLER", "RAIN"))
          .addConstraint("RAIN:TRUE", 0.2) // P(R) = 0.2
          .addConstraint("SPRINKLER:TRUE", List.of("RAIN:TRUE"), 0.01) // P(S|R) = 0.01
          .addConstraint("SPRINKLER:TRUE", List.of("RAIN:FALSE"), 0.4) // P(S|~R) = 0.4
          .addConstraint("WET_GRASS:TRUE", List.of("RAIN:TRUE", "SPRINKLER:TRUE"), 0.99)
          .addConstraint("WET_GRASS:TRUE", List.of("RAIN:FALSE", "SPRINKLER:TRUE"), 0.9)
          .addConstraint("WET_GRASS:TRUE", List.of("RAIN:FALSE", "SPRINKLER:FALSE"), 0.0)
          .addConstraint("WET_GRASS:TRUE", List.of("RAIN:TRUE", "SPRINKLER:FALSE"), 0.9);
      // Missing constraints will be auto-completed by solver (e.g., P(RAIN:FALSE) = 0.8)
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
      net.observeMarginals();
      MarginalTable rainTable = net.getObservedTable("RAIN");
      assertEquals(
          0.8, rainTable.getProbability(List.of(rainTable.getNodeState("RAIN:FALSE"))), 1E-9);
    }

    @Test
    void observeMarginals_beforeSolve_shouldImplicitlySolve() {
      assertDoesNotThrow(() -> net.observeMarginals());
      MarginalTable rainTable = net.getObservedTable("RAIN");
      assertNotNull(rainTable);
      assertEquals(
          0.2, rainTable.getProbability(List.of(rainTable.getNodeState("RAIN:TRUE"))), 1E-9);
    }

    @Test
    void observeNetwork_shouldUpdateProbabilities() {
      net.solveNetwork().observeMarginals();
      // P(RAIN:TRUE) is 0.2 initially
      assertEquals(0.2, net.getObservedTable("RAIN").getProbability("RAIN:TRUE"), 1E-6);

      // Observe WET_GRASS:TRUE
      net.observeNetwork(List.of("WET_GRASS:TRUE"));
      // P(RAIN:TRUE | WET_GRASS:TRUE) should be different (and higher)
      // This is the "explaining away" problem
      double pRainGivenWet = net.getObservedTable("RAIN").getProbability("RAIN:TRUE");
      assertTrue(pRainGivenWet > 0.2);
      // Exact value P(R|W) = P(W|R)P(R)/P(W)
      // P(W|R) = P(W|R,S)P(S|R) + P(W|R,~S)P(~S|R) = 0.99*0.01 + 0.9*0.99 = 0.9009
      // P(W|~R) = P(W|~R,S)P(S|~R) + P(W|~R,~S)P(~S|~R) = 0.9*0.4 + 0.0*0.6 = 0.36
      // P(W) = P(W|R)P(R) + P(W|~R)P(~R) = 0.9009*0.2 + 0.36*0.8 = 0.18018 + 0.288 = 0.46818
      // P(R|W) = (0.9009 * 0.2) / 0.46818 = 0.18018 / 0.46818 = 0.38485...
      assertEquals(0.384852, pRainGivenWet, 1E-6);
    }

    @Test
    void observeNetwork_withConflictingEvidence_shouldThrowException() {
      // Observing RAIN:TRUE and RAIN:FALSE
      assertThrows(Exception.class, () -> net.observeNetwork(List.of("RAIN:TRUE", "RAIN:FALSE")));
    }

    @Test
    void observeNetwork_withNonExistentState_shouldThrowException() {
      assertThrows(Exception.class, () -> net.observeNetwork(List.of("ZOMBIE:TRUE")));
    }

    @Test
    void observeNetwork_withEmptyList_shouldBeSameAsObserveMarginals() {
      net.solveNetwork();
      net.observeMarginals();
      double pRainMarginal = net.getObservedTable("RAIN").getProbability("RAIN:TRUE");

      net.observeNetwork(List.of());
      double pRainObservedEmpty = net.getObservedTable("RAIN").getProbability("RAIN:TRUE");

      assertEquals(pRainMarginal, pRainObservedEmpty);
    }

    @Test
    void getNetworkTable_shouldReturnTable() {
      net.solveNetwork();
      ProbabilityTable rainTable = net.getNetworkTable("RAIN");
      assertNotNull(rainTable);
      assertEquals(1, rainTable.getNodes().size()); // Just RAIN

      ProbabilityTable grassTable = net.getNetworkTable("WET_GRASS");
      assertNotNull(grassTable);
      assertEquals(3, grassTable.getNodes().size()); // WET_GRASS, RAIN, SPRINKLER
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
      net.observeNetwork(List.of("WET_GRASS:TRUE"));
      MarginalTable rainTable = net.getObservedTable("RAIN");
      assertNotNull(rainTable);
      assertEquals(0.384852, rainTable.getProbability("RAIN:TRUE"), 1E-6);
    }

    @Test
    void getObservedTable_nonExistentNode_shouldThrowException() {
      net.observeNetwork(List.of("WET_GRASS:TRUE"));
      assertThrows(Exception.class, () -> net.getObservedTable("ZOMBIE"));
    }

    @Test
    void getObservedTable_beforeObserve_shouldReturnMarginals() {
      net.solveNetwork();
      // No observe...() call
      MarginalTable rainTable = net.getObservedTable("RAIN");
      assertNotNull(rainTable);
      // Should be equal to the prior P(RAIN:TRUE)
      assertEquals(0.2, rainTable.getProbability("RAIN:TRUE"), 1E-6);
    }

    @Test
    void observeProbability_singleEvent_shouldReturnCorrectProb() {
      net.observeMarginals();
      double pRainTrue = net.getProbabilityFromCurrentObservations(List.of("RAIN:TRUE"));
      assertEquals(0.2, pRainTrue, 1E-9);
    }

    @Test
    void observeProbability_jointEvent_shouldReturnCorrectProb() {
      net.observeMarginals();
      // P(RAIN:TRUE, SPRINKLER:FALSE) = P(S:F | R:T) * P(R:T)
      // P(S:T | R:T) = 0.01, so P(S:F | R:T) = 0.99
      // P(R:T, S:F) = 0.99 * 0.2 = 0.198
      double pJoint =
          net.getProbabilityFromCurrentObservations(List.of("RAIN:TRUE", "SPRINKLER:FALSE"));
      assertEquals(0.198, pJoint, 1E-9);
    }

    @Test
    void observeProbability_conflictingEvent_shouldReturnZero() {
      net.observeMarginals();
      double pConflict =
          net.getProbabilityFromCurrentObservations(List.of("RAIN:TRUE", "RAIN:FALSE"));
      assertEquals(0.0, pConflict, 1E-9);
    }

    @Test
    void observeProbability_emptyEvent_shouldReturnOne() {
      net.observeMarginals();
      double pEmpty = net.getProbabilityFromCurrentObservations(List.of());
      assertEquals(1.0, pEmpty, 1E-9);
    }

    @Test
    void generateSamples_shouldReturnCorrectNumberOfSamples() {
      net.observeMarginals();
      List<List<String>> samples = net.generateSamples(null, null, 100, String.class);
      assertEquals(100, samples.size());
    }

    @Test
    void generateSamples_withIncludeNodes_shouldOnlyIncludeNodes() {
      net.observeMarginals();
      List<List<String>> samples = net.generateSamples(null, List.of("RAIN"), 10, String.class);
      assertEquals(10, samples.size());
      for (List<String> sample : samples) {
        assertEquals(1, sample.size()); // Only one node state
        assertTrue(sample.getFirst().equals("RAIN:TRUE") || sample.getFirst().equals("RAIN:FALSE"));
      }
    }

    @Test
    void generateSamples_withExcludeNodes_shouldExcludeNodes() {
      net.observeMarginals();
      List<List<String>> samples = net.generateSamples(List.of("RAIN"), null, 10, String.class);
      assertEquals(10, samples.size());
      for (List<String> sample : samples) {
        assertEquals(2, sample.size()); // SPRINKLER and WET_GRASS
        for (String state : sample) {
          assertFalse(state.startsWith("RAIN:"));
        }
      }
    }

    @Test
    void generateSamples_zeroSamples_shouldReturnEmptyList() {
      net.observeMarginals();
      List<List<String>> samples = net.generateSamples(null, null, 0, String.class);
      assertNotNull(samples);
      assertTrue(samples.isEmpty());
    }

    @Test
    void generateSamples_negativeSamples_shouldThrowException() {
      net.observeMarginals();
      assertThrows(
          IllegalArgumentException.class, () -> net.generateSamples(null, null, -10, String.class));
    }
  }

  @Nested
  class ScenarioTests {

    @Test
    void testSolves_RainSprinkler() {
      net = BayesianNetwork.newNetwork("RAIN_SPRINKLER_GRASS");

      PrinterConfigs printerConfigs = net.getPrinterConfigs();
      printerConfigs.setProbDecimalPlaces(3);
      printerConfigs.setPrintToConsole(true);

      net.addNode("RAIN", List.of("RAIN:TRUE", "RAIN:FALSE"))
          .addNode("SPRINKLER", List.of("SPRINKLER:TRUE", "SPRINKLER:FALSE"))
          .addNode("WET_GRASS", List.of("WET_GRASS:TRUE", "WET_GRASS:FALSE"))
          .addParent("SPRINKLER", "RAIN")
          .addParents("WET_GRASS", List.of("SPRINKLER", "RAIN"))
          .addConstraint("RAIN:TRUE", 0.2)
          .addConstraint("SPRINKLER:TRUE", List.of("RAIN:TRUE"), 0.01)
          .addConstraint("SPRINKLER:TRUE", List.of("RAIN:FALSE"), 0.4)
          .addConstraint("WET_GRASS:TRUE", List.of("RAIN:TRUE", "SPRINKLER:TRUE"), 0.99)
          .addConstraint("WET_GRASS:TRUE", List.of("RAIN:FALSE", "SPRINKLER:TRUE"), 0.9)
          .addConstraint("WET_GRASS:TRUE", List.of("RAIN:FALSE", "SPRINKLER:FALSE"), 0.0)
          .addConstraint("WET_GRASS:TRUE", List.of("RAIN:TRUE", "SPRINKLER:FALSE"), 0.9)
          .solveNetwork()
          .observeMarginals()
          .printNetwork()
          .observeNetwork(List.of("WET_GRASS:TRUE"))
          .printObserved();

      net.observeMarginals();
      int numOfSamples = 100_000;

      String testState = "RAIN:TRUE";
      String includedNode = "RAIN";

      generateSamples(net, numOfSamples, includedNode, testState);

      net.observeNetwork(List.of("WET_GRASS:TRUE"));
      System.out.println("\n--- Now testing P(RAIN:TRUE | WET_GRASS:TRUE) ---");
      generateSamples(net, numOfSamples, includedNode, testState);
    }

    private void generateSamples(
        BayesianNetwork network, int numOfSamples, String includedNode, String testState) {

      double observedProb = network.getProbabilityFromCurrentObservations(List.of(testState));
      double expected = observedProb * numOfSamples;
      double delta = Math.sqrt(numOfSamples) * 3; // 3 standard deviations
      long lowerBound = Math.max(0, (long) (expected - delta));
      long upperBound = (long) (expected + delta);

      List<List<String>> samples =
          network.generateSamples(List.of(), List.of(includedNode), numOfSamples, String.class);

      long count = samples.stream().flatMap(Collection::stream).filter(testState::equals).count();

      if (debugPrintSamplesToConsole) getLines(samples).forEach(System.out::println);

      System.out.printf(
          "Test State: %s%nExpected: %.2f (%.0f samples)%nAllowed Range: [%d, %d]%nActual Sample Count: %d%n",
          testState, observedProb, expected, lowerBound, upperBound, count);

      assertTrue(
          count >= lowerBound && count <= upperBound,
          String.format(
              "Sample count %d for %s is outside expected range [%d, %d]",
              count, testState, lowerBound, upperBound));
    }

    private List<String> getLines(List<List<String>> samples) {
      List<String> lines = new ArrayList<>();
      for (List<String> sample : samples) {
        StringBuilder sb = new StringBuilder();
        for (String s : sample) {
          sb.append(s).append(", ");
        }
        if (sb.length() > 2) {
          sb.setLength(sb.length() - 2);
        }
        lines.add(sb.toString());
      }
      return lines;
    }

    @Test
    void testNetworkAH_NonLocalConstraints() {
      net.getPrinterConfigs().setPrintToConsole(true);

      assertDoesNotThrow(
          () ->
              net =
                  BayesianNetwork.newNetwork("A_TO_H")
                      .addNode("A", List.of("A+", "A-"))
                      .addNode("B", List.of("B+", "B-"))
                      .addNode("C", List.of("C+", "C-"))
                      .addNode("D", List.of("D+", "D-"))
                      .addNode("E", List.of("E+", "E-"))
                      .addNode("F", List.of("F+", "F-"))
                      .addNode("G", List.of("G+", "G-"))
                      .addNode("H", List.of("H+", "H-"))
                      .addParents("D", List.of("A", "B"))
                      .addParents("E", List.of("B", "C"))
                      .addParents("F", List.of("D"))
                      .addParents("G", List.of("D", "E"))
                      .addParents("H", List.of("E"))
                      .addConstraint("D+", List.of("A+"), 0.33)
                      .addConstraint("D-", List.of("B+"), 0.57)
                      .addConstraint("E+", List.of("B+"), 0.61)
                      .addConstraint("E-", List.of("C+"), 0.37)
                      .addConstraint("F+", List.of("D+"), 0.26)
                      .addConstraint("F-", List.of("D-"), 0.92)
                      .addConstraint("G+", List.of("D+"), 0.87)
                      .addConstraint("G-", List.of("E+"), 0.50)
                      .addConstraint("H+", List.of("E+"), 0.43)
                      .addConstraint("H-", List.of("E-"), 0.18)
                      .addConstraint("A+", List.of("H+"), 0.25) // Non-local constraint
                      .solveNetwork()
                      .observeMarginals()
                      .printObserved());
    }

    @Test
    void testFantasyGraph_ComplexNetwork() {
      if (!debugSolveLengthyTests) return;
      net = BayesianNetwork.newNetwork("FANTASY_ELECTION");

      net.getPrinterConfigs().setPrintToConsole(true);

      assertDoesNotThrow(
          () ->
              net.addNode(
                      "DISTRICT_TYPE",
                      List.of(
                          "DISTRICT_TYPE:URBAN",
                          "DISTRICT_TYPE:SUBURBAN",
                          "DISTRICT_TYPE:RURAL",
                          "DISTRICT_TYPE:FRONTIER"))
                  .addNode(
                      "DISTRICT",
                      List.of(
                          "DISTRICT:CAPITAL_CITY",
                          "DISTRICT:CITY_SUBURBS",
                          "DISTRICT:FARM_TOWN",
                          "DISTRICT:MINING_OUTPOST",
                          "DISTRICT:OTHER"))
                  .addNode(
                      "RACE",
                      List.of(
                          "RACE:HUMAN",
                          "RACE:ANK",
                          "RACE:ORC",
                          "RACE:GOBLIN",
                          "RACE:DWARF",
                          "RACE:ELF"))
                  .addNode(
                      "AGE",
                      List.of("AGE:CHILD", "AGE:YOUNG_ADULT", "AGE:MIDDLE_AGE", "AGE:ELDERLY"))
                  .addNode(
                      "WEALTH",
                      List.of(
                          "WEALTH:MARGINAL",
                          "WEALTH:LOW",
                          "WEALTH:MIDDLE",
                          "WEALTH:HIGH",
                          "WEALTH:ULTRA"))
                  .addNode(
                      "OUTLOOK",
                      List.of(
                          "OUTLOOK:REVOLUTIONARY",
                          "OUTLOOK:PROGRESSIVE",
                          "OUTLOOK:MODERATE",
                          "OUTLOOK:CONSERVATIVE",
                          "OUTLOOK:REACTIONARY",
                          "OUTLOOK:APATHY"))
                  .addNode(
                      "VOTE",
                      List.of(
                          "VOTE:SDP",
                          "VOTE:VNG",
                          "VOTE:CPK",
                          "VOTE:UNF",
                          "VOTE:SND",
                          "VOTE:FPK",
                          "VOTE:KNC",
                          "VOTE:CSD",
                          "VOTE:OTH",
                          "VOTE:NONE"))
                  .addParents("DISTRICT_TYPE", List.of()) // Root node
                  .addParents("DISTRICT", List.of("DISTRICT_TYPE"))
                  .addParents("RACE", List.of("DISTRICT"))
                  .addParents("AGE", List.of("RACE", "DISTRICT"))
                  .addParents("WEALTH", List.of("RACE", "DISTRICT"))
                  .addParents("OUTLOOK", List.of("WEALTH", "AGE", "DISTRICT"))
                  .addParents("VOTE", List.of("RACE", "AGE", "DISTRICT", "OUTLOOK"))
                  // --- Constraints from user file ---
                  // Known marginals
                  // DISTRICT
                  .addConstraint("DISTRICT:CAPITAL_CITY", 600E3 / 320E6)
                  .addConstraint("DISTRICT:CITY_SUBURBS", 600E3 / 320E6)
                  .addConstraint("DISTRICT:FARM_TOWN", 600E3 / 320E6)
                  .addConstraint("DISTRICT:MINING_OUTPOST", 600E3 / 320E6)
                  // RACE
                  .addConstraint("RACE:HUMAN", 0.65)
                  .addConstraint("RACE:ANK", 0.14)
                  .addConstraint("RACE:ORC", 0.07)
                  .addConstraint("RACE:GOBLIN", 0.06)
                  .addConstraint("RACE:DWARF", 0.05)
                  .addConstraint("RACE:ELF", 0.03)
                  // VOTE
                  .addConstraint("VOTE:SDP", 0.1090)
                  .addConstraint("VOTE:VNG", 0.0941)
                  .addConstraint("VOTE:CPK", 0.0834)
                  .addConstraint("VOTE:UNF", 0.0685)
                  .addConstraint("VOTE:SND", 0.0214)
                  .addConstraint("VOTE:FPK", 0.0173)
                  .addConstraint("VOTE:KNC", 0.0066)
                  .addConstraint("VOTE:CSD", 0.0057)
                  .addConstraint("VOTE:OTH", 0.0066)
                  // WEALTH
                  .addConstraint("WEALTH:MARGINAL", 0.287)
                  .addConstraint("WEALTH:LOW", 0.325)
                  .addConstraint("WEALTH:HIGH", 0.12)
                  .addConstraint("WEALTH:ULTRA", 0.001)
                  // AGE
                  .addConstraint("AGE:CHILD", 0.388)
                  .addConstraint("AGE:YOUNG_ADULT", 0.318)
                  .addConstraint("AGE:MIDDLE_AGE", 0.209)
                  .addConstraint("AGE:ELDERLY", 0.085)
                  // DISTRICT_TYPE
                  .addConstraint("DISTRICT_TYPE:URBAN", 0.35)
                  .addConstraint("DISTRICT_TYPE:SUBURBAN", 0.25)
                  .addConstraint("DISTRICT_TYPE:RURAL", 0.30)
                  .addConstraint("DISTRICT_TYPE:FRONTIER", 0.10)
                  // ---
                  // CONDITIONALS
                  // DISTRICT_TYPE | DISTRICT
                  .addConstraint("DISTRICT_TYPE:URBAN", List.of("DISTRICT:CAPITAL_CITY"), 1.0)
                  .addConstraint("DISTRICT_TYPE:SUBURBAN", List.of("DISTRICT:CITY_SUBURBS"), 1.0)
                  .addConstraint("DISTRICT_TYPE:RURAL", List.of("DISTRICT:FARM_TOWN"), 1.0)
                  .addConstraint("DISTRICT_TYPE:FRONTIER", List.of("DISTRICT:MINING_OUTPOST"), 1.0)
                  // ... (omitted many constraints for brevity, but they'd be here) ...
                  // Age|Race
                  .addConstraint("AGE:MIDDLE_AGE", List.of("RACE:ORC"), 0.177)
                  .addConstraint("AGE:YOUNG_ADULT", List.of("RACE:ANK"), 0.335)
                  .addConstraint("AGE:CHILD", List.of("RACE:DWARF"), 0.303)
                  // ...
                  // NON-LOCAL CONDITIONALS
                  // VOTE | DISTRICT, OUTLOOK
                  .addConstraint(
                      "VOTE:CPK", List.of("OUTLOOK:REVOLUTIONARY", "DISTRICT:CAPITAL_CITY"), 0.8)
                  // ...
                  // OTHERS
                  .addConstraint("VOTE:NONE", List.of("AGE:CHILD"), 1.0)
                  .addConstraint("OUTLOOK:APATHY", List.of("VOTE:NONE"), 0.75)
                  .addConstraint("OUTLOOK:REVOLUTIONARY", List.of("WEALTH:MARGINAL"), 0.44)
                  .addConstraint("OUTLOOK:REVOLUTIONARY", List.of("VOTE:CPK"), 0.65)
                  .addConstraint("OUTLOOK:PROGRESSIVE", List.of("RACE:DWARF"), 0.46)
                  .addConstraint("OUTLOOK:CONSERVATIVE", List.of("WEALTH:HIGH"), 0.48)
                  .addConstraint("VOTE:UNF", List.of("RACE:ANK"), 0.02)
                  .addConstraint("RACE:ORC", List.of("VOTE:KNC"), 0.59)
                  .addConstraint("WEALTH:ULTRA", List.of("RACE:ANK"), 0.0005)
                  // ...
                  .solveNetwork()
                  .observeMarginals()
                  .printObserved()
                  .observeNetwork(List.of("DISTRICT:CAPITAL_CITY"))
                  .observeNetwork(List.of("RACE:ANK", "AGE:YOUNG_ADULT"))
                  .printObserved());

      net.getPrinterConfigs().setPrintToConsole(true);
      net.printNetwork();

      int numOfSamples = 100_000;
      String testState = "VOTE:CPK";
      String includedNode = "VOTE";

      generateSamples(net, numOfSamples, includedNode, testState);
    }
  }
}
