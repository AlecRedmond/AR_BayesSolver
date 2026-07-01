package io.github.alecredmond.internal.serialization.structure;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.serialization.constraint.SerializedProbabilityConstraint;
import io.github.alecredmond.export.serialization.network.SerializedBayesianNetwork;
import io.github.alecredmond.export.serialization.node.SerializedNode;
import io.github.alecredmond.export.serialization.probabilitytable.SerializedNetworkTable;
import io.github.alecredmond.internal.serialization.SerializationData;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NetworkDataSerializer {

    public SerializedBayesianNetwork serialize(BayesianNetworkData data) {
    return new SerializedBayesianNetwork(
        data.getNetworkName(),
        buildSerializedNodes(data),
        buildSerializedNetworkTables(data),
        buildSerializedProbabilityConstraints(data),
        data.isSolved());
  }

  private List<SerializedNode> buildSerializedNodes(BayesianNetworkData data) {
    NodeSerializer serializer = new NodeSerializer();
    return data.getNodes().stream().map(serializer::serialize).toList();
  }

  private Map<Serializable, SerializedNetworkTable> buildSerializedNetworkTables(
      BayesianNetworkData data) {
    NetworkTableSerializer serializer = new NetworkTableSerializer();
    return convertMap(data.getNetworkTablesMap(), Node::getId, serializer::serialize);
  }

  private List<SerializedProbabilityConstraint<ProbabilityConstraint>>
      buildSerializedProbabilityConstraints(BayesianNetworkData data) {
    return new ProbabilityConstraintSerializer().serializeAll(data);
  }

  private <R, S, T, U> Map<R, S> convertMap(
      Map<T, U> input, Function<T, R> keyConverter, Function<U, S> valConverter) {
    Map<R, S> converted = new HashMap<>();
    input.forEach((t, u) -> converted.put(keyConverter.apply(t), valConverter.apply(u)));
    return converted;
  }

  public BayesianNetworkData deSerialize(
      SerializedBayesianNetwork serialized, SerializationData serializationData) {
    BayesianNetworkData networkData = serializationData.getNetworkData();
    networkData.setNetworkName(serialized.getNetworkName());
    deSerializeNodeSTOs(serialized.getSerializedNodes(), serializationData);
    deSerializeNetworkTables(serialized, serializationData);
    deSerializeConstraints(serialized.getSerializedConstraints(), serializationData);
    networkData.setSolved(serialized.isSolved());
    return networkData;
  }

  private void deSerializeNodeSTOs(List<SerializedNode> serializedNodes, SerializationData data) {
    NodeSerializer serializer = new NodeSerializer();
    List<Node> nodes = data.getNetworkData().getNodes();
    serializedNodes.stream()
        .map(serializedNode -> serializer.deSerialize(serializedNode, data))
        .forEach(nodes::add);
  }

  private void deSerializeNetworkTables(
      SerializedBayesianNetwork serialized, SerializationData data) {
    NetworkTableSerializer serializer = new NetworkTableSerializer();
    Map<Serializable, Node> nodeIDsMap = data.getNodeIdMap();
    data.getNetworkData()
        .getNetworkTablesMap()
        .putAll(
            convertMap(
                serialized.getSerializedCptMap(),
                nodeIDsMap::get,
                serializedTable -> serializer.deSerialize(serializedTable, data)));
  }

  private <T extends ProbabilityConstraint> void deSerializeConstraints(
      List<SerializedProbabilityConstraint<T>> serializedConstraints, SerializationData data) {
    new ProbabilityConstraintSerializer().deserialize(serializedConstraints, data);
  }

}
