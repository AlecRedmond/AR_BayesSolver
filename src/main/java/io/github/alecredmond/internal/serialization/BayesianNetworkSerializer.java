package io.github.alecredmond.internal.serialization;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.export.serialization.network.SerializedBayesianNetwork;
import io.github.alecredmond.internal.method.network.BayesianNetworkImpl;
import io.github.alecredmond.internal.serialization.structure.NetworkDataSerializer;
import io.github.alecredmond.internal.serialization.structure.NodeSerializer;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BayesianNetworkSerializer {

  public SerializedBayesianNetwork serialize(BayesianNetwork network) {
    if (!network.isSolved()) network.solveNetwork();
    return new NetworkDataSerializer().serialize(network.getNetworkData());
  }

  public BayesianNetwork deSerialize(SerializedBayesianNetwork serializedBayesianNetwork) {
    SerializationData serializationData = new SerializationData();
    createNodes(serializedBayesianNetwork, serializationData);
    return new BayesianNetworkImpl(
        new NetworkDataSerializer().deSerialize(serializedBayesianNetwork, serializationData));
  }

  private void createNodes(SerializedBayesianNetwork sbn, SerializationData serializationData) {
    NodeSerializer serializer = new NodeSerializer();
    sbn.getSerializedNodes()
        .forEach(
            serializedNode -> {
              Node node = serializer.createNewBase(serializedNode);
              serializationData.getNodeIdMap().put(node.getId(), node);
              node.getNodeStates()
                  .forEach(
                      state -> serializationData.getNodeStateIdMap().put(state.getId(), state));
            });
  }
}
