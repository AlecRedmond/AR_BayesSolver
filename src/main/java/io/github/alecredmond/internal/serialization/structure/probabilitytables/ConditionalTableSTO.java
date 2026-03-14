package io.github.alecredmond.internal.serialization.structure.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ConditionalTable;
import io.github.alecredmond.internal.serialization.mapper.SerializationData;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ConditionalTableSTO extends ProbabilityTableSTO<ConditionalTable> {
  private Serializable networkNodeId;

  @Override
  public ConditionalTableSTO serialize(ConditionalTable conditionalTable) {
    serializeCommon(conditionalTable);
    this.networkNodeId = conditionalTable.getNetworkNode().getId();
    return this;
  }

  @Override
  public ConditionalTable deSerialize(SerializationData data) {
    Map<Serializable, Node> nodeIdMap = new HashMap<>();
    Map<Serializable, NodeState> nodeStateIdMap = new HashMap<>();
    buildMapData(nodeIdMap, nodeStateIdMap, data);
    return new ConditionalTable(
        this.tableName,
        this.vectorSTO.deSerialize(data),
        nodeDeSerialize(this.nodeIds, data),
        nodeDeSerialize(this.eventNodeIds, data),
        nodeDeSerialize(this.conditionNodeIds, data),
        data.getNodeIdMap().get(networkNodeId),
        nodeIdMap,
        nodeStateIdMap);
  }
}
