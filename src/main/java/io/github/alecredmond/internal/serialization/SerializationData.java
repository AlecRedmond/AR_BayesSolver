package io.github.alecredmond.internal.serialization;

import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import java.io.Serializable;
import java.util.Map;
import lombok.Data;

@Data
public class SerializationData {
  private final BayesianNetworkData networkData;
  private final Map<Serializable, Node> nodeIdMap;
  private final Map<Serializable, NodeState> nodeStateIdMap;

  public SerializationData() {
    this.networkData = new BayesianNetworkData();
    this.nodeIdMap = networkData.getNodeIDsMap();
    this.nodeStateIdMap = networkData.getNodeStateIDsMap();
  }
}
