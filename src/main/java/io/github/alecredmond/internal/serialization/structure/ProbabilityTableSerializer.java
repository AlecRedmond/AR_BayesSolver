package io.github.alecredmond.internal.serialization.structure;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ConditionalTable;
import io.github.alecredmond.export.application.probabilitytables.MarginalTable;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.export.serialization.probabilitytable.SerializedConditionalTable;
import io.github.alecredmond.export.serialization.probabilitytable.SerializedMarginalTable;
import io.github.alecredmond.export.serialization.probabilitytable.SerializedProbabilityTable;
import io.github.alecredmond.internal.serialization.SerializerUtils;
import io.github.alecredmond.internal.serialization.SerializationData;

import java.io.Serializable;
import java.util.*;

public class ProbabilityTableSerializer {

  public SerializedProbabilityTable serialize(ProbabilityTable table) {
    return switch (table) {
      case MarginalTable mt -> serializeMarginalTable(mt);
      case ConditionalTable ct -> serializeConditionalTable(ct);
      default -> throw new IllegalStateException("Unexpected value: " + table);
    };
  }

  public SerializedMarginalTable serializeMarginalTable(MarginalTable mt) {
    SerializedMarginalTable sto = new SerializedMarginalTable();
    serializeCommon(sto, mt);
    sto.setNetworkNodeId(mt.getNetworkNode().getId());
    return sto;
  }

  private SerializedConditionalTable serializeConditionalTable(ConditionalTable ct) {
    SerializedConditionalTable sto = new SerializedConditionalTable();
    serializeCommon(sto, ct);
    sto.setNetworkNodeId(ct.getNetworkNode().getId());
    return sto;
  }

  private void serializeCommon(SerializedProbabilityTable sto, ProbabilityTable table) {
    sto.setVectorSTO(new ProbabilityVectorSerializer().serialize(table.getVector()));
    sto.setNodeIds(SerializerUtils.serializeNodes(table.getNodes()));
    sto.setEventNodeIds(SerializerUtils.serializeNodes(table.getEvents()));
    sto.setConditionNodeIds(SerializerUtils.serializeNodes(table.getConditions()));
    sto.setTableName(table.getTableName());
  }

  public ProbabilityTable deSerialize(SerializedProbabilityTable sto, SerializationData data) {
    return switch (sto) {
      case SerializedMarginalTable marginal -> deSerializeMarginal(marginal, data);
      case SerializedConditionalTable conditional -> deSerializeConditional(conditional, data);
      default -> throw new IllegalStateException("Unexpected value: " + sto);
    };
  }

  public MarginalTable deSerializeMarginal(SerializedMarginalTable marginal, SerializationData data) {
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

  private ProbabilityTable deSerializeConditional(
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
      SerializedProbabilityTable sto,
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

  private ProbabilityVector deserializeVector(SerializedProbabilityTable sto, SerializationData data) {
    return new ProbabilityVectorSerializer().deSerialize(sto.getVectorSTO(), data);
  }

  private Set<Node> nodeDeSerialize(List<Serializable> ids, SerializationData data) {
    return SerializerUtils.deSerializeNodes(ids, LinkedHashSet::new, data);
  }
}
