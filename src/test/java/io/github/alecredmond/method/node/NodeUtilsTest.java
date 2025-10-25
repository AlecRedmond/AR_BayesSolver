package io.github.alecredmond.method.node;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.alecredmond.method.network.BayesianNetwork;
import io.github.alecredmond.application.node.Node;
import java.util.List;
import org.junit.jupiter.api.Test;

class NodeUtilsTest {

  @Test
  void generateNodeAndStateCombinations() {
    List<Node> nodes =
        BayesianNetwork.newNetwork("node_utils_test")
            .addNode("A", List.of("A+", "A-"))
            .addNode("B", List.of("B+", "B-"))
            .addNode("C", List.of("C+", "C-"))
            .addNode("D", List.of("D+", "D-"))
            .addParent("D", "C")
            .addParent("C", "B")
            .addParent("B", "A")
            .solveNetwork()
            .getNetworkData()
            .getNodes();

    assertBlockSizeCorrect(nodes);
  }

  private void assertBlockSizeCorrect(List<Node> nodes) {
    int nodesSize = nodes.size();
    for (int blockSize = 0; blockSize <= nodesSize; blockSize++) {
      int binom = binomCoeff(nodesSize, blockSize);
      var nodeSets = NodeUtils.generateNodeCombinations(nodes, blockSize);
      assertEquals(binom, nodeSets.size());
    }
  }

  static int binomCoeff(int n, int k) {
    int res = 1;

    if (k > n - k) k = n - k;

    for (int i = 0; i < k; ++i) {
      res *= (n - i);
      res /= (i + 1);
    }

    return res;
  }
}
