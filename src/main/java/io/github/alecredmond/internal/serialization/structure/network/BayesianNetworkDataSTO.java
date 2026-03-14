package io.github.alecredmond.internal.serialization.structure.network;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.MarginalTable;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.serialization.mapper.SerializationData;
import io.github.alecredmond.internal.serialization.mapper.SerializationTransferObject;
import io.github.alecredmond.internal.serialization.structure.constraints.ProbabilityConstraintSTO;
import io.github.alecredmond.internal.serialization.structure.node.NodeSTO;
import io.github.alecredmond.internal.serialization.structure.probabilitytables.MarginalTableSTO;
import io.github.alecredmond.internal.serialization.structure.probabilitytables.ProbabilityTableSTO;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.Getter;
import lombok.ToString;

@ToString
public class BayesianNetworkDataSTO implements SerializationTransferObject<BayesianNetworkData> {
  private String networkName;
  @Getter private List<NodeSTO> nodeSTOs;
  private Map<Serializable, ProbabilityTableSTO<?>> networkTableStoMap;
  private Map<Serializable, MarginalTableSTO> observedTableStoMap;
  private Map<Serializable, Serializable> observedEvidenceIdMap;
  private List<? extends ProbabilityConstraintSTO<?>> constraintStos;
  private boolean solved;

  @Override
  public BayesianNetworkDataSTO serialize(BayesianNetworkData data) {
    networkName = data.getNetworkName();
    nodeSTOs = buildNodeSTOs(data);
    networkTableStoMap = buildNetworkTableStoMap(data);
    observedTableStoMap = buildObservedTableStoMap(data);
    observedEvidenceIdMap = buildObservedEvidenceIdMap(data);
    constraintStos = buildConstraintStos(data);
    solved = data.isSolved();
    return this;
  }

  private List<NodeSTO> buildNodeSTOs(BayesianNetworkData data) {
    return data.getNodes().stream().map(node -> new NodeSTO().serialize(node)).toList();
  }

  private Map<Serializable, ProbabilityTableSTO<?>> buildNetworkTableStoMap(
      BayesianNetworkData data) {
    return buildMap(data.getNetworkTablesMap(), Node::getId, ProbabilityTableSTO::staticSerialize);
  }

  private Map<Serializable, MarginalTableSTO> buildObservedTableStoMap(BayesianNetworkData data) {
    return buildMap(
        data.getObservedTablesMap(), Node::getId, mt -> new MarginalTableSTO().serialize(mt));
  }

  private Map<Serializable, Serializable> buildObservedEvidenceIdMap(BayesianNetworkData data) {
    return buildMap(data.getObservedEvidence(), Node::getId, NodeState::getId);
  }

  private List<? extends ProbabilityConstraintSTO<?>> buildConstraintStos(
      BayesianNetworkData data) {
    return data.getConstraints().stream().map(ProbabilityConstraintSTO::staticSerialize).toList();
  }

  private <R, S, T, U> Map<R, S> buildMap(
      Map<T, U> input, Function<T, R> keyFunc, Function<U, S> valFunc) {
    Map<R, S> map = new HashMap<>();
    input.forEach((t, u) -> map.put(keyFunc.apply(t), valFunc.apply(u)));
    return map;
  }

  @Override
  public BayesianNetworkData deSerialize(SerializationData data) {
    Map<Serializable, Node> nodeIDsMap = data.getNodeIdMap();
    Map<Serializable, NodeState> nodeStateIDsMap = data.getNodeStateIdMap();
    return new BayesianNetworkData(
        this.networkName,
        deSerializeNodeSTOs(nodeSTOs, data),
        nodeIDsMap,
        nodeStateIDsMap,
        deSerializeNetworkTables(data, nodeIDsMap),
        deSerializeObservedTables(data, nodeIDsMap),
        deSerializeObservedEvidence(nodeIDsMap, nodeStateIDsMap),
        deSerializeConstraints(constraintStos, data),
        this.solved);
  }

  private List<Node> deSerializeNodeSTOs(List<NodeSTO> nodeSTOs, SerializationData data) {
    return nodeSTOs.stream().map(sto -> sto.deSerialize(data)).toList();
  }

  private Map<Node, ProbabilityTable> deSerializeNetworkTables(
      SerializationData data, Map<Serializable, Node> nodeIDsMap) {
    return buildMap(
        networkTableStoMap,
        nodeIDsMap::get,
        sto -> ProbabilityTableSTO.staticDeSerialize(sto, data));
  }

  private Map<Node, MarginalTable> deSerializeObservedTables(
      SerializationData data, Map<Serializable, Node> nodeIDsMap) {
    return buildMap(observedTableStoMap, nodeIDsMap::get, sto -> sto.deSerialize(data));
  }

  private Map<Node, NodeState> deSerializeObservedEvidence(
      Map<Serializable, Node> nodeIDsMap, Map<Serializable, NodeState> nodeStateIDsMap) {
    return buildMap(observedEvidenceIdMap, nodeIDsMap::get, nodeStateIDsMap::get);
  }

  private List<ProbabilityConstraint> deSerializeConstraints(
      List<? extends ProbabilityConstraintSTO<?>> constraintStos, SerializationData data) {
    return constraintStos.stream()
        .map(sto -> ProbabilityConstraintSTO.staticDeSerialize(sto, data))
        .toList();
  }
}
