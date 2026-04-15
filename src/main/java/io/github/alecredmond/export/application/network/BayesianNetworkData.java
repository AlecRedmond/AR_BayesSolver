package io.github.alecredmond.export.application.network;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import java.io.Serializable;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

  public <T extends Serializable> ProbabilityTable getNetworkTableById(T nodeID) {
    return networkTablesMap.get(nodeIDsMap.get(nodeID));
  }
}
