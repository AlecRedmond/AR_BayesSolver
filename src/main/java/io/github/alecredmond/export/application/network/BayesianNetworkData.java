package io.github.alecredmond.export.application.network;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ConditionalTable;
import io.github.alecredmond.export.application.probabilitytables.MarginalTable;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import java.io.Serializable;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Object containing the data which constitutes a {@link BayesianNetwork}.
 *
 * <p><b>WARNING: DO NOT MODIFY THIS DATA!</b> Use the methods given in {@link BayesianNetwork} to
 * modify the network's structure, add constraints, etc.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BayesianNetworkData {
  private String networkName = "UNNAMED NETWORK";
  private List<Node> nodes = new ArrayList<>();
  private Map<Serializable, Node> nodeIDsMap = new HashMap<>();
  private Map<Serializable, NodeState> nodeStateIDsMap = new HashMap<>();
  private Map<Node, ProbabilityTable> networkTablesMap = new LinkedHashMap<>();
  private List<ProbabilityConstraint> constraints = new ArrayList<>();
  private boolean solved = false;

  /**
   * Returns the conditional probability table (CPT) associated with the given Node's ID. This be a
   * {@link MarginalTable} if referencing a root node, or a {@link ConditionalTable} otherwise.
   *
   * @param <T> the class of the Node's ID
   * @param nodeID an ID associated with a node in the network
   * @return the CPT associated with the Node
   */
  public <T extends Serializable> ProbabilityTable getNetworkTableById(T nodeID) {
    return networkTablesMap.get(nodeIDsMap.get(nodeID));
  }
}
