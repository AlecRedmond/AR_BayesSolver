package io.github.alecredmond.internal.serialization.structure;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.NetworkTable;
import io.github.alecredmond.export.serialization.constraint.SerializedProbabilityConstraint;
import io.github.alecredmond.export.serialization.network.SerializedBayesianNetwork;
import io.github.alecredmond.export.serialization.node.SerializedNode;
import io.github.alecredmond.export.serialization.probabilitytable.SerializedNetworkTable;
import io.github.alecredmond.internal.method.constraints.ConstraintRegistry;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintSerializer;
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

  private Map<Serializable, SerializedNetworkTable> buildSerializedNetworkTables(
      BayesianNetworkData data) {
    NetworkTableSerializer serializer = new NetworkTableSerializer();
    return convertMap(data.getNetworkTablesMap(), Node::getId, serializer::serialize);
  }

  private List<SerializedProbabilityConstraint<ProbabilityConstraint>>
      buildSerializedProbabilityConstraints(BayesianNetworkData data) {
    return data.getConstraints().stream().map(this::serializeConstraint).toList();
  }

  private <R, S, T, U> Map<R, S> convertMap(
      Map<T, U> input, Function<T, R> keyConverter, Function<U, S> valConverter) {
    Map<R, S> converted = new HashMap<>();
    input.forEach((t, u) -> converted.put(keyConverter.apply(t), valConverter.apply(u)));
    return converted;
  }

  @SuppressWarnings("unchecked")
  private <T extends ProbabilityConstraint> SerializedProbabilityConstraint<T> serializeConstraint(
      T constraint) {
    return ((ConstraintSerializer<T>)
            ConstraintRegistry.getStrategy(constraint.getClass()).buildConstraintSerializer())
        .serialize(constraint);
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
        deSerializeConstraints(serialized.getSerializedConstraints(), data),
        serialized.isSolved());
  }

  private List<Node> deSerializeNodeSTOs(
      List<SerializedNode> serializedNodes, SerializationData data) {
    NodeSerializer serializer = new NodeSerializer();
    return serializedNodes.stream()
        .map(serializedNode -> serializer.deSerialize(serializedNode, data))
        .toList();
  }

  private Map<Node, NetworkTable> deSerializeNetworkTables(
      SerializedBayesianNetwork serialized,
      SerializationData data,
      Map<Serializable, Node> nodeIDsMap) {
    NetworkTableSerializer serializer = new NetworkTableSerializer();
    return convertMap(
        serialized.getSerializedCptMap(),
        nodeIDsMap::get,
        serializedTable -> serializer.deSerialize(serializedTable, data));
  }

  private <T extends ProbabilityConstraint> List<ProbabilityConstraint> deSerializeConstraints(
      List<SerializedProbabilityConstraint<T>> serializedConstraints, SerializationData data) {
    return serializedConstraints.stream()
        .map(serialized -> deSerializeConstraint(serialized, data))
        .toList();
  }

  private <T extends ProbabilityConstraint> ProbabilityConstraint deSerializeConstraint(
      SerializedProbabilityConstraint<T> serialized, SerializationData data) {
    return ConstraintRegistry.getStrategy(serialized.getConstraintClass())
        .buildConstraintSerializer()
        .deSerialize(serialized, data);
  }
}
