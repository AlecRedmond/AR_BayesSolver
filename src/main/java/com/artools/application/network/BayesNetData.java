package com.artools.application.network;

import com.artools.application.constraints.ParameterConstraint;
import com.artools.application.node.Node;
import com.artools.application.node.NodeState;
import com.artools.application.probabilitytables.MarginalTable;
import com.artools.application.probabilitytables.ProbabilityTable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class BayesNetData {
  protected List<Node> nodes;
  protected Map<Object, Node> nodesMap;
  protected Map<Object, NodeState> nodeStateMap;
  protected Map<Node, ProbabilityTable> networkTablesMap;
  protected Map<Node, MarginalTable> observationMap;
  protected List<ParameterConstraint> constraints;
  protected boolean solved;

  public BayesNetData() {
    this.nodes = new ArrayList<>();
    this.nodesMap = new HashMap<>();
    this.nodeStateMap = new HashMap<>();
    this.networkTablesMap = new HashMap<>();
    this.observationMap = new HashMap<>();
    this.constraints = new ArrayList<>();
    this.solved = false;
  }
}
