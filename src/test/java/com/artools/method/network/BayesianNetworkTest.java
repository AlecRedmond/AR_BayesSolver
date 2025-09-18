package com.artools.method.network;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class BayesianNetworkTest {
  BayesianNetwork test = BayesianNetwork.newNetwork();

  @Test
  void testSolves() {
    test.addNode("RAIN", List.of("RAIN:TRUE", "RAIN:FALSE"))
        .addNode("SPRINKLER", List.of("SPRINKLER:TRUE", "SPRINKLER:FALSE"))
        .addNode("WET_GRASS", List.of("WET_GRASS:TRUE", "WET_GRASS:FALSE"))
        .addParent("SPRINKLER", "RAIN")
        .addParents("WET_GRASS", List.of("SPRINKLER", "RAIN"))
        .addEvidence("RAIN:TRUE", 0.3)
        .addEvidence("SPRINKLER:TRUE", List.of("RAIN:TRUE"), 0.01)
        .addEvidence("SPRINKLER:TRUE", List.of("RAIN:FALSE"), 0.3)
        .addEvidence("WET_GRASS:TRUE", List.of("RAIN:TRUE", "SPRINKLER:TRUE"), 0.95)
        .addEvidence("WET_GRASS:TRUE", List.of("RAIN:FALSE", "SPRINKLER:TRUE"), 0.9)
        .addEvidence("WET_GRASS:TRUE", List.of("RAIN:FALSE", "SPRINKLER:FALSE"), 0.25)
        .addEvidence("WET_GRASS:TRUE", List.of("RAIN:TRUE", "SPRINKLER:FALSE"), 0.93)
        .solveNetwork()
        .printNetwork();

    assertTrue(true);
  }
}
