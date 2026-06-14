package io.github.alecredmond.export.method.network;

import io.github.alecredmond.export.application.network.NetworkNodeInput;
import io.github.alecredmond.internal.method.network.NetworkInputBuilder;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class NetworkInput {
  private String networkName;
  private List<NetworkNodeInput> nodeInputs = new ArrayList<>();

  public NetworkInput() {
    this.networkName = "UNNAMED NETWORK";
  }

  public NetworkInput(String networkName) {
    this.networkName = networkName;
  }

  public BayesianNetwork createNetwork() {
    return new NetworkInputBuilder().buildNetwork(networkName, nodeInputs);
  }

  public NetworkInput addInput(Serializable id, List<Serializable> stateIds) {
    nodeInputs.add(new NetworkNodeInput(id, stateIds));
    return this;
  }

  public NetworkInput addInput(
      Serializable id, List<Serializable> stateIds, List<Serializable> parentIds) {
    nodeInputs.add(new NetworkNodeInput(id, stateIds, parentIds));
    return this;
  }

  public NetworkInput addInput(
      Serializable id,
      List<Serializable> stateIds,
      List<Serializable> parentIds,
      Serializable[] cptStrideOrderIds,
      double[] cptValues) {
    nodeInputs.add(new NetworkNodeInput(id, stateIds, parentIds, cptStrideOrderIds, cptValues));
    return this;
  }
}
