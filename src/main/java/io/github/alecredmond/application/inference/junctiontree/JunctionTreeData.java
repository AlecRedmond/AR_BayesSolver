package io.github.alecredmond.application.inference.junctiontree;

import io.github.alecredmond.application.constraints.ParameterConstraint;
import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.MarginalTable;
import io.github.alecredmond.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.application.probabilitytables.junctiontree.JunctionTreeTable;
import io.github.alecredmond.method.inference.junctiontree.handlers.JTAConstraintHandler;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JunctionTreeData {

  private BayesianNetworkData bayesianNetworkData;
  private Set<Clique> cliqueSet;
  private Set<Separator> separators;
  private Set<Clique> leafCliques;
  private Map<Clique, Set<ProbabilityTable>> associatedTables;
  private List<JunctionTreeTable> junctionTreeTables;
  private Map<ParameterConstraint, Clique> constraintCliqueMap;
  private Map<ParameterConstraint, JTAConstraintHandler> constraintHandlers;
  private boolean marginalized;

  public List<Node> getNodes() {
    return bayesianNetworkData.getNodes();
  }

  public Map<Node, MarginalTable> getObservationMap() {
    return bayesianNetworkData.getObservationMap();
  }

  public List<ParameterConstraint> getConstraints() {
    return bayesianNetworkData.getConstraints();
  }

  public Map<Node, NodeState> getObserved() {
    return this.bayesianNetworkData.getObserved();
  }

  public void setObserved(Map<Node, NodeState> observed) {
    this.bayesianNetworkData.setObserved(observed);
  }

  public static JunctionTreeDataBuilder builder() {
    return new JunctionTreeDataBuilder();
  }

  public static class JunctionTreeDataBuilder {
    private BayesianNetworkData bayesianNetworkData;
    private Set<Clique> cliqueSet;
    private Set<Separator> separators;
    private Set<Clique> leafCliques;
    private Map<Clique, Set<ProbabilityTable>> associatedTables;
    private List<JunctionTreeTable> junctionTreeTables;
    private Map<ParameterConstraint, Clique> cliqueForConstraint;
    private Map<ParameterConstraint, JTAConstraintHandler> constraintHandlers;

    JunctionTreeDataBuilder() {}

    public JunctionTreeDataBuilder bayesianNetworkData(BayesianNetworkData bayesianNetworkData) {
      this.bayesianNetworkData = bayesianNetworkData;
      return this;
    }

    public JunctionTreeDataBuilder cliqueSet(Set<Clique> cliqueSet) {
      this.cliqueSet = cliqueSet;
      return this;
    }

    public JunctionTreeDataBuilder separators(Set<Separator> separators) {
      this.separators = separators;
      return this;
    }

    public JunctionTreeDataBuilder leafCliques(Set<Clique> leafCliques) {
      this.leafCliques = leafCliques;
      return this;
    }

    public JunctionTreeDataBuilder associatedTables(
        Map<Clique, Set<ProbabilityTable>> associatedTables) {
      this.associatedTables = associatedTables;
      return this;
    }

    public JunctionTreeDataBuilder junctionTreeTables(List<JunctionTreeTable> junctionTreeTables) {
      this.junctionTreeTables = junctionTreeTables;
      return this;
    }

    public JunctionTreeDataBuilder cliqueForConstraint(
        Map<ParameterConstraint, Clique> cliqueForConstraint) {
      this.cliqueForConstraint = cliqueForConstraint;
      return this;
    }

    public JunctionTreeDataBuilder constraintHandlers(
        Map<ParameterConstraint, JTAConstraintHandler> constraintHandlers) {
      this.constraintHandlers = constraintHandlers;
      return this;
    }

    public JunctionTreeData build() {
      return new JunctionTreeData(
          this.bayesianNetworkData,
          this.cliqueSet,
          this.separators,
          this.leafCliques,
          this.associatedTables,
          this.junctionTreeTables,
          this.cliqueForConstraint,
          this.constraintHandlers,
          false);
    }

    public String toString() {
      return "JunctionTreeData.JunctionTreeDataBuilder(bayesianNetworkData="
          + this.bayesianNetworkData
          + ", cliqueSet="
          + this.cliqueSet
          + ", separators="
          + this.separators
          + ", leafCliques="
          + this.leafCliques
          + ", associatedTables="
          + this.associatedTables
          + ", junctionTreeTables="
          + this.junctionTreeTables
          + ", cliqueForConstraint="
          + this.cliqueForConstraint
          + ", constraintHandlers="
          + this.constraintHandlers
          + ")";
    }
  }
}
