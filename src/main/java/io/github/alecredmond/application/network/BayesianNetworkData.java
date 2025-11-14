package io.github.alecredmond.application.network;

import io.github.alecredmond.application.constraints.ParameterConstraint;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.MarginalTable;
import io.github.alecredmond.application.probabilitytables.ProbabilityTable;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BayesianNetworkData {
  protected String networkName;
  protected List<Node> nodes;
  protected Map<Object, Node> nodeIDsMap;
  protected Map<Object, NodeState> nodeStateIDsMap;
  protected Map<Node, ProbabilityTable> networkTablesMap;
  protected Map<Node, MarginalTable> observationMap;
  protected Map<Node, NodeState> observed;
  protected List<ParameterConstraint> constraints;
  protected boolean solved;

  public BayesianNetworkData() {
    this.networkName = "";
    this.nodes = new ArrayList<>();
    this.nodeIDsMap = new HashMap<>();
    this.nodeStateIDsMap = new HashMap<>();
    this.networkTablesMap = new LinkedHashMap<>();
    this.observationMap = new LinkedHashMap<>();
    this.observed = new HashMap<>();
    this.constraints = new ArrayList<>();
    this.solved = false;
  }

  public <T> ProbabilityTable getNetworkTable(T nodeID) {
    return networkTablesMap.get(nodeIDsMap.get(nodeID));
  }

  public <T> MarginalTable getObservedTable(T nodeID) {
    return observationMap.get(nodeIDsMap.get(nodeID));
  }
}
