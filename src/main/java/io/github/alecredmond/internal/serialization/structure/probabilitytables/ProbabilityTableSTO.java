package io.github.alecredmond.internal.serialization.structure.probabilitytables;

import static io.github.alecredmond.internal.serialization.mapper.SerializerUtils.*;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ConditionalTable;
import io.github.alecredmond.export.application.probabilitytables.MarginalTable;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.serialization.mapper.SerializationData;
import io.github.alecredmond.internal.serialization.mapper.SerializationTransferObject;
import io.github.alecredmond.internal.serialization.structure.probabilitytables.probabilityvector.ProbabilityVectorSTO;

import java.io.Serializable;
import java.util.*;

public abstract class ProbabilityTableSTO<T extends ProbabilityTable>
    implements SerializationTransferObject<T> {
  protected ProbabilityVectorSTO vectorSTO;
  protected List<Serializable> nodeIds;
  protected List<Serializable> eventNodeIds;
  protected List<Serializable> conditionNodeIds;
  protected Serializable tableName;

  public static ProbabilityTableSTO<?> staticSerialize(ProbabilityTable table) {
    return switch (table) {
      case MarginalTable mt -> new MarginalTableSTO().serialize(mt);
      case ConditionalTable ct -> new ConditionalTableSTO().serialize(ct);
      default -> throw new IllegalStateException("Unexpected value: " + table);
    };
  }

  public static ProbabilityTable staticDeSerialize(
      ProbabilityTableSTO<?> sto, SerializationData data) {
    return switch (sto) {
      case MarginalTableSTO marginal -> marginal.deSerialize(data);
      case ConditionalTableSTO conditional -> conditional.deSerialize(data);
      default -> throw new IllegalStateException("Unexpected value: " + sto);
    };
  }

  protected void serializeCommon(ProbabilityTable table) {
    this.vectorSTO = new ProbabilityVectorSTO().serialize(table.getVector());
    this.nodeIds = serializeNodes(table.getNodes());
    this.eventNodeIds = serializeNodes(table.getEvents());
    this.conditionNodeIds = serializeNodes(table.getConditions());
    this.tableName = table.getTableName();
  }

  protected void buildMapData(
      Map<Serializable, Node> nodeMap,
      Map<Serializable, NodeState> stateMap,
      SerializationData data) {
    Map<Serializable, Node> nodeIdMap = data.getNodeIdMap();
    nodeIds.forEach(
        nodeId -> {
          Node node = nodeIdMap.get(nodeId);
          nodeMap.put(nodeId, node);
          node.getNodeStates().forEach(state -> stateMap.put(state.getId(), state));
        });
  }

  protected Set<Node> nodeDeSerialize(List<Serializable> ids, SerializationData data) {
    return deSerializeNodes(ids, LinkedHashSet::new, data);
  }
}
