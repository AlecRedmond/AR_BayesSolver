package io.github.alecredmond.internal.serialization.mapper;

import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.internal.serialization.structure.network.BayesianNetworkDataSTO;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SerializationMapper {

  public BayesianNetworkDataSTO serialize(BayesianNetworkData networkData) {
    return new BayesianNetworkDataSTO().serialize(networkData);
  }

  public BayesianNetworkData deSerialize(BayesianNetworkDataSTO networkDataSTO) {
    SerializationData data = new SerializationData();
    createNodes(networkDataSTO, data);
    return networkDataSTO.deSerialize(data);
  }

  private void createNodes(BayesianNetworkDataSTO networkDataSTO, SerializationData data) {
    networkDataSTO
        .getNodeSTOs()
        .forEach(
            nodeSTO -> {
              Node node = nodeSTO.createNewBase();
              data.getNodeIdMap().put(node.getId(), node);
              node.getNodeStates()
                  .forEach(state -> data.getNodeStateIdMap().put(state.getId(), state));
            });
  }
}
