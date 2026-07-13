package io.github.alecredmond.internal.serialization.structure;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.export.probabilitytables.serialized.SerializedNetworkTable;
import io.github.alecredmond.internal.method.probabilitytables.tablebuilders.ConditionalTableBuilder;
import io.github.alecredmond.internal.method.probabilitytables.tablebuilders.RootNodeTableBuilder;
import io.github.alecredmond.internal.method.probabilitytables.tablebuilders.TableBuilder;
import io.github.alecredmond.internal.serialization.SerializationData;
import io.github.alecredmond.internal.serialization.SerializerUtils;
import java.io.Serializable;
import java.util.*;
import java.util.stream.IntStream;

public class NetworkTableSerializer {

  public SerializedNetworkTable serialize(NetworkTable table) {
    return new SerializedNetworkTable(
        table.getNetworkNode().getId(),
        SerializerUtils.serializeNodes(table.getConditions()),
        table.getTableName(),
        serializeProbabilities(table));
  }

  private List<Double> serializeProbabilities(NetworkTable table) {
    return Arrays.stream(table.getProbabilities()).boxed().toList();
  }

  public NetworkTable deSerialize(SerializedNetworkTable serialized, SerializationData data) {
    Node event = data.getNodeIdMap().get(serialized.networkNodeId());
    List<Node> conditions = nodeDeSerialize(serialized.conditionNodeIds(), data);
    NetworkTable table = selectBuilder(conditions).buildTable(List.of(event), conditions);
    deSerializeProbabilities(table, serialized);
    return table;
  }

  private List<Node> nodeDeSerialize(List<Serializable> ids, SerializationData data) {
    return SerializerUtils.deSerializeNodes(ids, ArrayList::new, data);
  }

  private static TableBuilder<? extends NetworkTable> selectBuilder(List<Node> conditionNodes) {
    return conditionNodes.isEmpty() ? new RootNodeTableBuilder() : new ConditionalTableBuilder();
  }

  private void deSerializeProbabilities(NetworkTable table, SerializedNetworkTable serialized) {
    List<Double> list = serialized.probabilities();
    double[] array = table.getProbabilities();
    IntStream.range(0, array.length).forEach(i -> array[i] = list.get(i));
  }
}
