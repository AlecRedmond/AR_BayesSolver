package io.github.alecredmond.internal.application.inference.junctiontree;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.MarginalTable;
import io.github.alecredmond.internal.method.inference.junctiontree.handlers.JTAConstraintHandler;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JunctionTreeData {
  private BayesianNetworkData bayesianNetworkData;
  private Clique[] cliques;
  private Clique[] rootCliques;
  private Separator[] separators;
  private Map<Clique, List<JTAConstraintHandler>> constraintHandlersMap;
  private boolean solverConfig;
  private double jointProbability;

  public List<Node> getNodes() {
    return bayesianNetworkData.getNodes();
  }

  public Map<Node, MarginalTable> getObservationMap() {
    return bayesianNetworkData.getObservationMap();
  }

  public List<ProbabilityConstraint> getConstraints() {
    return bayesianNetworkData.getConstraints();
  }

  public Map<Node, NodeState> getObserved() {
    return this.bayesianNetworkData.getObserved();
  }

  public void setObserved(Map<Node, NodeState> observed) {
    this.bayesianNetworkData.setObserved(observed);
  }
}
