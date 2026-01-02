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
  private String networkName;
  private List<Node> nodes;
  private Map<Object, Node> nodeIDsMap;
  private Map<Object, NodeState> nodeStateIDsMap;
  private Map<Node, ProbabilityTable> networkTablesMap;
  private Map<Node, MarginalTable> observationMap;
  private Map<Node, NodeState> observed;
  private List<ParameterConstraint> constraints;
  private boolean solved;

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
