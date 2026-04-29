package io.github.alecredmond.export.method.network;

import static io.github.alecredmond.export.method.network.NetworkScenarioBuilder.*;

import java.util.function.Supplier;
import lombok.Getter;

@Getter
public enum NetworkScenario {
  RAIN_NETWORK(buildRainNetwork()),
  AH_NETWORK(buildAhNetwork()),
  FANTASY_GRAPH(buildFantasyGraph()),
  SIMPLE_LINEAR(buildSimpleLinearGraph()),
  DIAMOND_NET(buildDiamondNetwork());

  private final Supplier<BayesianNetwork> supplier;

  NetworkScenario(Supplier<BayesianNetwork> supplier) {
    this.supplier = supplier;
  }

  public BayesianNetwork get() {
    return supplier.get();
  }
}
