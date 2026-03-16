package io.github.alecredmond.internal.serialization;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.internal.method.network.BayesianNetworkImpl;
import io.github.alecredmond.export.serialization.network.SerializedBayesNetData;
import io.github.alecredmond.internal.serialization.structure.NetworkDataSerializer;
import io.github.alecredmond.internal.serialization.structure.NodeSerializer;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BayesianNetworkSerializer {

  public SerializedBayesNetData serialize(BayesianNetwork network) {
    return new NetworkDataSerializer().serialize(network.getNetworkData());
  }

  public BayesianNetwork deSerialize(SerializedBayesNetData networkDataSTO) {
    SerializationData data = new SerializationData();
    createNodes(networkDataSTO, data);
    return new BayesianNetworkImpl(new NetworkDataSerializer().deSerialize(networkDataSTO, data));
  }

  private void createNodes(SerializedBayesNetData networkDataSTO, SerializationData data) {
    NodeSerializer serializer = new NodeSerializer();
    networkDataSTO
        .getSerializedNodes()
        .forEach(
            nodeSTO -> {
              Node node = serializer.createNewBase(nodeSTO);
              data.getNodeIdMap().put(node.getId(), node);
              node.getNodeStates()
                  .forEach(state -> data.getNodeStateIdMap().put(state.getId(), state));
            });
  }
}
