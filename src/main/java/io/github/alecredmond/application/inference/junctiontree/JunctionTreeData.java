package io.github.alecredmond.application.inference.junctiontree;

import io.github.alecredmond.application.constraints.ParameterConstraint;
import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.junctiontree.JunctionTreeTable;
import io.github.alecredmond.application.probabilitytables.MarginalTable;
import io.github.alecredmond.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.method.inference.junctiontree.handlers.JTAConstraintHandler;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents the data structure required for the Junction Tree Algorithm (JTA) inference process in
 * a Bayesian Network. <br>
 * This class encapsulates the structural components of the Junction Tree (cliques and separators),
 * the specialized probability tables used in the JTA ({@link JunctionTreeTable}), and the mappings
 * necessary to link these structures back to the core network data and applied constraints.
 */
@Getter
@AllArgsConstructor
public class JunctionTreeData {
  /** The reference to the core data of the Bayesian Network. */
  private final BayesianNetworkData bayesianNetworkData;

  /** The set of all {@link Clique} objects forming the Junction Tree structure. */
  private final Set<Clique> cliqueSet;

  /** The set of all {@link Separator} objects connecting the cliques in the Junction Tree. */
  private final Set<Separator> separators;

  /** A subset of {@link Clique} objects connected to only one separator */
  private final Set<Clique> leafCliques;

  /**
   * A map linking each {@link Clique} to the set of original network {@link ProbabilityTable}s
   * (CPTs or marginals) that were absorbed into it during the junction tree construction.
   */
  private final Map<Clique, Set<ProbabilityTable>> associatedTables;

  /**
   * A list of all {@link JunctionTreeTable} objects created for the cliques and separators, sorted
   * by ascending length of the probability tables.
   */
  private final List<JunctionTreeTable> junctionTreeTables;

  /**
   * A map linking each {@link ParameterConstraint} to the specific {@link Clique} that handles its
   * probability distribution.
   */
  private final Map<ParameterConstraint, Clique> cliqueForConstraint;

  /**
   * A map linking each {@link ParameterConstraint} to its dedicated {@link JTAConstraintHandler} used
   * during the JTA inference.
   */
  private final Map<ParameterConstraint, JTAConstraintHandler> constraintHandlers;

  /** Flag tracking whether the Junction tree has been marginalized */
  @Setter private boolean marginalized;

  /**
   * Retrieves the list of all {@link Node} objects from the underlying Bayesian Network Data.
   *
   * @return A list of {@link Node} objects.
   */
  public List<Node> getNodes() {
    return bayesianNetworkData.getNodes();
  }

  /**
   * Returns the map of observed or calculated marginal probabilities for each {@link Node} from the
   * underlying Bayesian Network Data.*
   *
   * @return A map of {@link Node} to {@link MarginalTable}.
   */
  public Map<Node, MarginalTable> getObservationMap() {
    return bayesianNetworkData.getObservationMap();
  }

  /**
   * Retrieves the list of all constraints applied during the JTA solver cycle
   *
   * @return A list of {@link ParameterConstraint} objects.
   */
  public List<ParameterConstraint> getConstraints() {
    return bayesianNetworkData.getConstraints();
  }

  /**
   * Retrieves the map storing the specific {@link NodeState} that has been observed as evidence for
   * a given {@link Node}.
   *
   * @return the current evidence applied to the network
   */
  public Map<Node, NodeState> getObserved() {
    return this.bayesianNetworkData.getObserved();
  }

  /**
   * Sets the map storing the specific {@link NodeState} that has been observed as evidence for a
   * given {@link Node}.
   *
   * @param observed the current observed evidence in the network
   */
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
