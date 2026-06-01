package io.github.alecredmond.internal.serialization.structure;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ConditionalTable;
import io.github.alecredmond.export.application.probabilitytables.MarginalTable;
import io.github.alecredmond.export.application.probabilitytables.NetworkTable;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.export.serialization.probabilitytable.SerializedConditionalTable;
import io.github.alecredmond.export.serialization.probabilitytable.SerializedMarginalTable;
import io.github.alecredmond.export.serialization.probabilitytable.SerializedNetworkTable;
import io.github.alecredmond.internal.serialization.SerializationData;
import io.github.alecredmond.internal.serialization.SerializerUtils;
import java.io.Serializable;
import java.util.*;

public class NetworkTableSerializer {

  public SerializedNetworkTable serialize(NetworkTable table) {
    return switch (table) {
      case MarginalTable mt -> serializeMarginalTable(mt);
      case ConditionalTable ct -> serializeConditionalTable(ct);
      default -> throw new IllegalStateException("Unexpected value: " + table);
    };
  }

  public SerializedMarginalTable serializeMarginalTable(MarginalTable mt) {
    SerializedMarginalTable serialized = new SerializedMarginalTable();
    serializeCommon(serialized, mt);
    serialized.setNetworkNodeId(mt.getNetworkNode().getId());
    return serialized;
  }

  private SerializedConditionalTable serializeConditionalTable(ConditionalTable ct) {
    SerializedConditionalTable serialized = new SerializedConditionalTable();
    serializeCommon(serialized, ct);
    serialized.setNetworkNodeId(ct.getNetworkNode().getId());
    return serialized;
  }

  private void serializeCommon(SerializedNetworkTable serialized, NetworkTable table) {
    serialized.setVectorSTO(new ProbabilityVectorSerializer().serialize(table.getVector()));
    serialized.setNodeIds(SerializerUtils.serializeNodes(table.getNodes()));
    serialized.setEventNodeIds(SerializerUtils.serializeNodes(table.getEvents()));
    serialized.setConditionNodeIds(SerializerUtils.serializeNodes(table.getConditions()));
    serialized.setTableName(table.getTableName());
  }

  public NetworkTable deSerialize(SerializedNetworkTable serialized, SerializationData data) {
    return switch (serialized) {
      case SerializedMarginalTable marginal -> deSerializeMarginal(marginal, data);
      case SerializedConditionalTable conditional -> deSerializeConditional(conditional, data);
      default -> throw new IllegalStateException("Unexpected value: " + serialized);
    };
  }

  public MarginalTable deSerializeMarginal(
      SerializedMarginalTable marginal, SerializationData data) {
    Map<Serializable, Node> nodeIdMap = new HashMap<>();
    Map<Serializable, NodeState> nodeStateIdMap = new HashMap<>();
    buildMapData(marginal, nodeIdMap, nodeStateIdMap, data);
    return new MarginalTable(
        deserializeVector(marginal, data),
        marginal.getTableName(),
        data.getNodeIdMap().get(marginal.getNetworkNodeId()),
        nodeStateIdMap,
        nodeIdMap);
  }

  private NetworkTable deSerializeConditional(
      SerializedConditionalTable conditional, SerializationData data) {
    Map<Serializable, Node> nodeIdMap = new HashMap<>();
    Map<Serializable, NodeState> nodeStateIdMap = new HashMap<>();
    buildMapData(conditional, nodeIdMap, nodeStateIdMap, data);
    return new ConditionalTable(
        conditional.getTableName(),
        deserializeVector(conditional, data),
        nodeDeSerialize(conditional.getNodeIds(), data),
        nodeDeSerialize(conditional.getEventNodeIds(), data),
        nodeDeSerialize(conditional.getConditionNodeIds(), data),
        data.getNodeIdMap().get(conditional.getNetworkNodeId()),
        nodeIdMap,
        nodeStateIdMap);
  }

  private void buildMapData(
      SerializedNetworkTable sto,
      Map<Serializable, Node> nodeMap,
      Map<Serializable, NodeState> stateMap,
      SerializationData data) {
    Map<Serializable, Node> nodeIdMap = data.getNodeIdMap();
    sto.getNodeIds()
        .forEach(
            nodeId -> {
              Node node = nodeIdMap.get(nodeId);
              nodeMap.put(nodeId, node);
              node.getNodeStates().forEach(state -> stateMap.put(state.getId(), state));
            });
  }

  private ProbabilityVector deserializeVector(SerializedNetworkTable sto, SerializationData data) {
    return new ProbabilityVectorSerializer().deSerialize(sto.getVectorSTO(), data);
  }

  private Set<Node> nodeDeSerialize(List<Serializable> ids, SerializationData data) {
    return SerializerUtils.deSerializeNodes(ids, LinkedHashSet::new, data);
  }
}
