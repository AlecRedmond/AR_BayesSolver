package com.artools.method.junctiontree;

import static org.junit.jupiter.api.Assertions.*;

import com.artools.application.junctiontree.JunctionTreeData;
import com.artools.application.network.BayesNetData;
import com.artools.method.network.BayesNet;
import java.util.List;
import java.util.stream.Collectors;

import com.artools.method.solver.netsampler.JunctionTreeDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JunctionTreeDataBuilderTest {
  BayesNetData bayesNetData;

  @BeforeEach
  void setData() {
    bayesNetData =
        new BayesNet()
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
            .solveNetwork()
            .getNetworkData();
  }

  @Test
  void doesNotThrowErrors() {
    assertDoesNotThrow(() -> JunctionTreeDataBuilder.build(bayesNetData));
  }

  @Test
  void analyseResults() {
    JunctionTreeData jtd = JunctionTreeDataBuilder.build(bayesNetData);
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
}
