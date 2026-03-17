package io.github.alecredmond.internal.serialization.structure;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.MarginalTable;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.serialization.constraint.SerializedProbabilityConstraint;
import io.github.alecredmond.export.serialization.network.SerializedBayesNetData;
import io.github.alecredmond.export.serialization.node.SerializedNode;
import io.github.alecredmond.export.serialization.probabilitytable.SerializedMarginalTable;
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

  public SerializedBayesNetData serialize(BayesianNetworkData data) {
    return new SerializedBayesNetData(
        data.getNetworkName(),
        buildSerializedNodes(data),
        buildSerializedNetworkTables(data),
        buildSerializedObservedTables(data),
        buildSerializedObservedEvidence(data),
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
    return buildMap(data.getNetworkTablesMap(), Node::getId, serializer::serialize);
  }

  private Map<Serializable, SerializedMarginalTable> buildSerializedObservedTables(
      BayesianNetworkData data) {
    ProbabilityTableSerializer serializer = new ProbabilityTableSerializer();
    return buildMap(data.getMarginalTableMap(), Node::getId, serializer::serializeMarginalTable);
  }

  private Map<Serializable, Serializable> buildSerializedObservedEvidence(
      BayesianNetworkData data) {
    return buildMap(data.getObservedEvidence(), Node::getId, NodeState::getId);
  }

  private List<SerializedProbabilityConstraint> buildSerializedProbabilityConstraints(
      BayesianNetworkData data) {
    ConstraintSerializer serializer = new ConstraintSerializer();
    return data.getConstraints().stream().map(serializer::serialize).toList();
  }

  private <R, S, T, U> Map<R, S> buildMap(
      Map<T, U> input, Function<T, R> keyFunc, Function<U, S> valFunc) {
    Map<R, S> map = new HashMap<>();
    input.forEach((t, u) -> map.put(keyFunc.apply(t), valFunc.apply(u)));
    return map;
  }

  public BayesianNetworkData deSerialize(
      SerializedBayesNetData serialized, SerializationData data) {
    Map<Serializable, Node> nodeIDsMap = data.getNodeIdMap();
    Map<Serializable, NodeState> nodeStateIDsMap = data.getNodeStateIdMap();
    return new BayesianNetworkData(
        serialized.getNetworkName(),
        deSerializeNodeSTOs(serialized.getSerializedNodes(), data),
        nodeIDsMap,
        nodeStateIDsMap,
        deSerializeNetworkTables(serialized, data, nodeIDsMap),
        deSerializeObservedTables(serialized, data, nodeIDsMap),
        deSerializeObservedEvidence(serialized, nodeIDsMap, nodeStateIDsMap),
        deSerializeConstraints(serialized.getConstraintStos(), data),
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
      SerializedBayesNetData serialized,
      SerializationData data,
      Map<Serializable, Node> nodeIDsMap) {
    ProbabilityTableSerializer serializer = new ProbabilityTableSerializer();
    return buildMap(
        serialized.getNetworkTableStoMap(),
        nodeIDsMap::get,
        tableSTO -> serializer.deSerialize(tableSTO, data));
  }

  private Map<Node, MarginalTable> deSerializeObservedTables(
      SerializedBayesNetData serialized,
      SerializationData data,
      Map<Serializable, Node> nodeIDsMap) {
    ProbabilityTableSerializer serializer = new ProbabilityTableSerializer();
    return buildMap(
        serialized.getObservedTableStoMap(),
        nodeIDsMap::get,
        tableSTO -> serializer.deSerializeMarginal(tableSTO, data));
  }

  private Map<Node, NodeState> deSerializeObservedEvidence(
      SerializedBayesNetData serialized,
      Map<Serializable, Node> nodeIDsMap,
      Map<Serializable, NodeState> nodeStateIDsMap) {
    return buildMap(serialized.getObservedEvidenceIdMap(), nodeIDsMap::get, nodeStateIDsMap::get);
  }

  private List<ProbabilityConstraint> deSerializeConstraints(
      List<SerializedProbabilityConstraint> serializedConstraints, SerializationData data) {
    ConstraintSerializer serializer = new ConstraintSerializer();
    return serializedConstraints.stream()
        .map(constraintSTO -> serializer.deSerialize(constraintSTO, data))
        .toList();
  }
}
