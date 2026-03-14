package io.github.alecredmond.export.application.network;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.MarginalTable;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import java.io.Serializable;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BayesianNetworkData {
  private String networkName;
  private List<Node> nodes;
  private Map<Serializable, Node> nodeIDsMap;
  private Map<Serializable, NodeState> nodeStateIDsMap;
  private Map<Node, ProbabilityTable> networkTablesMap;
  private Map<Node, MarginalTable> observedTablesMap;
  private Map<Node, NodeState> observedEvidence;
  private List<ProbabilityConstraint> constraints;
  private boolean solved;

  public BayesianNetworkData() {
    this.networkName = "UNNAMED NETWORK";
    this.nodes = new ArrayList<>();
    this.nodeIDsMap = new HashMap<>();
    this.nodeStateIDsMap = new HashMap<>();
    this.networkTablesMap = new LinkedHashMap<>();
    this.observedTablesMap = new LinkedHashMap<>();
    this.observedEvidence = new HashMap<>();
    this.constraints = new ArrayList<>();
    this.solved = false;
  }

  public <T extends Serializable> ProbabilityTable getNetworkTable(T nodeID) {
    return networkTablesMap.get(nodeIDsMap.get(nodeID));
  }

  public <T extends Serializable> MarginalTable getObservedTable(T nodeID) {
    return observedTablesMap.get(nodeIDsMap.get(nodeID));
  }
}
