package io.github.alecredmond.internal.serialization.structure;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.serialization.constraint.SerializedProbabilityConstraint;
import io.github.alecredmond.export.serialization.network.SerializedBayesianNetwork;
import io.github.alecredmond.export.serialization.node.SerializedNode;
import io.github.alecredmond.export.serialization.probabilitytable.SerializedProbabilityTable;
import io.github.alecredmond.internal.serialization.SerializationData;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  private Map<Serializable, SerializedProbabilityTable> buildSerializedNetworkTables(
      BayesianNetworkData data) {
    ProbabilityTableSerializer serializer = new ProbabilityTableSerializer();
    return convertMap(data.getNetworkTablesMap(), Node::getId, serializer::serialize);
  }

  private List<SerializedProbabilityConstraint> buildSerializedProbabilityConstraints(
      BayesianNetworkData data) {
    ConstraintSerializer serializer = new ConstraintSerializer();
    return data.getConstraints().stream().map(serializer::serialize).toList();
  }

  private <R, S, T, U> Map<R, S> convertMap(
      Map<T, U> input, Function<T, R> keyConverter, Function<U, S> valConverter) {
    Map<R, S> converted = new HashMap<>();
    input.forEach((t, u) -> converted.put(keyConverter.apply(t), valConverter.apply(u)));
    return converted;
  }

  public BayesianNetworkData deSerialize(
      SerializedBayesianNetwork serialized, SerializationData data) {
    Map<Serializable, Node> nodeIDsMap = data.getNodeIdMap();
    Map<Serializable, NodeState> nodeStateIDsMap = data.getNodeStateIdMap();
    return new BayesianNetworkData(
        serialized.getNetworkName(),
        deSerializeNodeSTOs(serialized.getSerializedNodes(), data),
        nodeIDsMap,
        nodeStateIDsMap,
        deSerializeNetworkTables(serialized, data, nodeIDsMap),
        deSerializeConstraints(serialized.getSerializedProbabilityConstraints(), data),
        serialized.isSolved());
  }

  private List<Node> deSerializeNodeSTOs(
      List<SerializedNode> serializedNodes, SerializationData data) {
    NodeSerializer serializer = new NodeSerializer();
    return serializedNodes.stream()
        .map(serializedNode -> serializer.deSerialize(serializedNode, data))
        .toList();
  }

  private Map<Node, ProbabilityTable> deSerializeNetworkTables(
      SerializedBayesianNetwork serialized,
      SerializationData data,
      Map<Serializable, Node> nodeIDsMap) {
    ProbabilityTableSerializer serializer = new ProbabilityTableSerializer();
    return convertMap(
        serialized.getSerializedCptMap(),
        nodeIDsMap::get,
        serializedTable -> serializer.deSerialize(serializedTable, data));
  }

  private List<ProbabilityConstraint> deSerializeConstraints(
      List<SerializedProbabilityConstraint> serializedConstraints, SerializationData data) {
    ConstraintSerializer serializer = new ConstraintSerializer();
    return serializedConstraints.stream()
        .map(constraintSTO -> serializer.deSerialize(constraintSTO, data))
        .toList();
  }
}
