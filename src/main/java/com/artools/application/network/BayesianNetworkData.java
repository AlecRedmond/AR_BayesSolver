package com.artools.application.network;

import com.artools.application.constraints.ParameterConstraint;
import com.artools.application.node.Node;
import com.artools.application.node.NodeState;
import com.artools.application.probabilitytables.MarginalTable;
import com.artools.application.probabilitytables.ProbabilityTable;
import com.artools.application.sampler.JunctionTreeData;

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
  protected Map<Node, NodeState> observedStatesMap;
  protected List<ParameterConstraint> constraints;
  protected boolean solved;
  protected JunctionTreeData junctionTreeData;

  public BayesianNetworkData() {
    this.networkName = "";
    this.nodes = new ArrayList<>();
    this.nodeIDsMap = new HashMap<>();
    this.nodeStateIDsMap = new HashMap<>();
    this.networkTablesMap = new LinkedHashMap<>();
    this.observationMap = new LinkedHashMap<>();
    this.constraints = new ArrayList<>();
    this.solved = false;
    this.junctionTreeData = null;
  }

  public <T> ProbabilityTable getNetworkTable(T nodeID) {
    return networkTablesMap.get(nodeIDsMap.get(nodeID));
  }

  public <T> MarginalTable getObservedTable(T nodeID) {
    return observationMap.get(nodeIDsMap.get(nodeID));
  }
}
