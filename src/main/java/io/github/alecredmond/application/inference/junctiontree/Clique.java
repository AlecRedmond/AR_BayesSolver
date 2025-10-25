package io.github.alecredmond.application.inference.junctiontree;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.probabilitytables.JunctionTreeTable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.github.alecredmond.method.inference.junctiontree.handlers.JTATableHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents a <b>Clique</b> in the Junction Tree structure used for Bayesian Network inference.
 * <br>
 * A Clique is a subgraph of the moralized and triangulated Bayesian Network that holds a joint
 * probability distribution over its associated nodes, represented by a {@link JunctionTreeTable}.
 */
@Data
@EqualsAndHashCode(exclude = {"separatorMap"})
public class Clique {
  /** The set of all {@link Node} objects contained within this Clique. */
  private Set<Node> nodes;

  /**
   * The {@link JunctionTreeTable} storing the joint probability distribution for the Clique's
   * nodes.
   */
  private JunctionTreeTable table;

  /** The handler responsible for JTA operations specific to the current Clique's table. */
  private JTATableHandler handler;

  /**
   * A map linking neighboring {@link Clique} objects to the {@link Separator} that connects them.
   */
  private Map<Clique, Separator> separatorMap;

  /**
   * Constructs a new Clique with its associated nodes and probability table.
   *
   * @param nodes The set of {@link Node} objects that form the clique.
   * @param table The {@link JunctionTreeTable} that will hold the clique's joint probability.
   */
  public Clique(Set<Node> nodes, JunctionTreeTable table) {
    this.nodes = nodes;
    this.table = table;
    this.handler = new JTATableHandler(table);
    this.separatorMap = new HashMap<>();
  }

  /**
   * Retrieves a set of all {@link Separator} objects connected to this Clique.
   *
   * @return A set of all adjacent Separators.
   */
  public Set<Separator> getSeparators() {
    return new HashSet<>(separatorMap.values());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Clique : ");
    nodes.forEach(node -> sb.append(node.toString()).append(" "));
    return sb.toString();
  }
}
