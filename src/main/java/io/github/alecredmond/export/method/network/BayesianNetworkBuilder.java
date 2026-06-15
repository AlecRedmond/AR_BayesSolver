package io.github.alecredmond.export.method.network;

import io.github.alecredmond.export.application.network.NetworkBuilderNode;
import io.github.alecredmond.internal.method.network.NetworkInputBuilder;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class BayesianNetworkBuilder {
  private String networkName;
  private List<NetworkBuilderNode> nodeInputs = new ArrayList<>();

  public BayesianNetworkBuilder() {
    this.networkName = "UNNAMED NETWORK";
  }

  public BayesianNetworkBuilder(String networkName) {
    this.networkName = networkName;
  }

  public BayesianNetwork build() {
    return new NetworkInputBuilder().buildNetwork(networkName, nodeInputs);
  }

  public <T extends Serializable> BayesianNetworkBuilder addNode(T id, List<T> stateIds) {
    nodeInputs.add(new NetworkBuilderNode(id, stateIds));
    return this;
  }

  public <T extends Serializable> BayesianNetworkBuilder addNode(
      T id, List<T> stateIds, List<T> parentIds) {
    nodeInputs.add(new NetworkBuilderNode(id, stateIds, parentIds));
    return this;
  }

  public <T extends Serializable> BayesianNetworkBuilder addNode(
      T id, List<T> stateIds, List<T> cptStrideOrderDesc, double[] cptValues) {
    nodeInputs.add(new NetworkBuilderNode(id, stateIds, cptStrideOrderDesc, cptValues));
    return this;
  }

  public <T extends Serializable> BayesianNetworkBuilder addNode(
      T id, List<T> stateIds, double[] cptValues) {
    nodeInputs.add(new NetworkBuilderNode(id, stateIds, cptValues));
    return this;
  }
}
