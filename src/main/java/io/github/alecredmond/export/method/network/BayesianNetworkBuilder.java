package io.github.alecredmond.export.method.network;

import io.github.alecredmond.export.application.network.NetworkBuilderNode;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.internal.method.network.NetworkInputBuilder;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * A builder object to streamline the construction of a {@link BayesianNetwork}. {@code
 * BayesianNetworkBuilder} allows information about the network to be input at the {@link Node}
 * level using its id, {@link NodeState} ids, parent {@link Node} ids, and known Conditional
 * Probability Table (CPT) entries. This bypasses the strict input order which is necessary when
 * manually building a {@link BayesianNetwork} from the interface.
 *
 * <p>
 */
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
