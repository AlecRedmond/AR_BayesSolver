package io.github.alecredmond.method.inference.junctiontree;

import static org.junit.jupiter.api.Assertions.*;

import io.github.alecredmond.application.inference.InferenceEngineConfigs;
import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.method.inference.InferenceEngine;
import io.github.alecredmond.method.network.BayesianNetwork;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JTAInitializerTest {
  BayesianNetworkData bayesianNetworkData;
  InferenceEngine engine;

  @BeforeEach
  void setData() {
    bayesianNetworkData =
        BayesianNetwork.newNetwork()
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
            .addConstraint("A+", List.of("H+"), 1.0)
            .solveNetwork()
            .getNetworkData();
  }


//  @Test
//  void buildsSolverConfiguration_shouldNotThrow() {
//    assertDoesNotThrow(() ->
//            JTAInitializer.buildSolverConfiguration(bayesianNetworkData)
//    );
//  }
/*
  @Test
  void buildInferenceConfiguration_shouldNotThrow(){
      assertDoesNotThrow(() -> engine.runSolver());
  }

  @Test
  void analyseResults() {
    JunctionTreeData jtd = JTAInitializer.buildSolverConfiguration(bayesianNetworkData);
    var cliques = jtd.getCliqueSet();
    var separators =
        jtd.getCliqueSet().stream()
            .flatMap(clique -> clique.getSeparators().stream())
            .collect(Collectors.toSet());
    int numLeafs = jtd.getLeafCliques().size();
    int numCliques = cliques.size();
    int numSeparators = separators.size();
    assertTrue(numLeafs >= 2);
    assertEquals(numSeparators, numCliques - 1);

    jtd.getAssociatedTables()
        .forEach(
            ((clique, probabilityTables) -> {
              System.out.println(clique.toString());
              StringBuilder sb = new StringBuilder();
              probabilityTables.forEach(pt -> sb.append(pt.getTableID()).append(" "));
              sb.append("\n");
              clique.getSeparatorMap().values().forEach(s -> sb.append(s.toString()));
              System.out.println(sb);
            }));
  }
   */
}
