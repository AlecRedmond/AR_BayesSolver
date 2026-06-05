package io.github.alecredmond.export.application.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.method.probabilitytables.NetworkTableHelper;

public interface NetworkTable extends ProbabilityTable {
  Node getNetworkNode();

  @SuppressWarnings("rawtypes")
  NetworkTableHelper getHelper();
}
