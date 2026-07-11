package io.github.alecredmond.export.method.inference;

import static io.github.alecredmond.TestConfigs.DOUBLE_EQUALITY;
import static io.github.alecredmond.TestConfigs.SOLVE_LONG_TESTS;
import static org.junit.jupiter.api.Assertions.*;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.export.method.network.NetworkScenario;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BayesSolverTest {

  public static Stream<Arguments> streamScenarios() {
    return Arrays.stream(NetworkScenario.values())
        .filter(scenario -> SOLVE_LONG_TESTS || !scenario.equals(NetworkScenario.FANTASY_GRAPH))
        .map(NetworkScenario::get)
        .map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource("streamScenarios")
  void testSolvesTwiceWithSameResult(BayesianNetwork network) {
    BayesSolver solver = BayesSolver.create(network);
    List<Node> orderedNodes = List.copyOf(network.buildNetworkData().getNetworkData().getNodes());
    double[] firstSolveCPTsFlat;
    double[] secondSolveCPTsFlat;
    if (solver.writeCPTsFromConstraints()) {
      firstSolveCPTsFlat = buildFlatCptArray(orderedNodes, network);
      solver.writeCPTsFromConstraints();
    } else {
      solver.forceSolve(SolverAlgorithm.JUNCTION_TREE_IPFP);
      firstSolveCPTsFlat = buildFlatCptArray(orderedNodes, network);
      solver.forceSolve(SolverAlgorithm.JUNCTION_TREE_IPFP);
    }
    secondSolveCPTsFlat = buildFlatCptArray(orderedNodes, network);
    assertArraysEqual(firstSolveCPTsFlat, secondSolveCPTsFlat);
  }

  private static double[] buildFlatCptArray(List<Node> orderedNodes, BayesianNetwork network) {
    return orderedNodes.stream()
        .map(node -> network.getNetworkTables().get(node))
        .flatMapToDouble(networkTable -> Arrays.stream(networkTable.getProbabilities()))
        .toArray();
  }

  private void assertArraysEqual(double[] firstSolveCPTsFlat, double[] secondSolveCPTsFlat) {
    for (int i = 0; i < firstSolveCPTsFlat.length; i++) {
      assertEquals(firstSolveCPTsFlat[i], secondSolveCPTsFlat[i], DOUBLE_EQUALITY);
    }
  }
}
