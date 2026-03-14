package io.github.alecredmond.internal.serialization.structure.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.MarginalTable;
import io.github.alecredmond.internal.serialization.mapper.SerializationData;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MarginalTableSTO extends ProbabilityTableSTO<MarginalTable> {
  private Serializable networkNodeId;

  @Override
  public MarginalTableSTO serialize(MarginalTable marginalTable) {
    serializeCommon(marginalTable);
    this.networkNodeId = marginalTable.getNetworkNode().getId();
    return this;
  }

  @Override
  public MarginalTable deSerialize(SerializationData data) {
    Map<Serializable, Node> nodeIdMap = new HashMap<>();
    Map<Serializable, NodeState> nodeStateIdMap = new HashMap<>();
    buildMapData(nodeIdMap, nodeStateIdMap, data);
    return new MarginalTable(
        this.vectorSTO.deSerialize(data),
        this.tableName,
        data.getNodeIdMap().get(networkNodeId),
        nodeStateIdMap,
        nodeIdMap);
  }
}
