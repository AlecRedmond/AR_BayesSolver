package io.github.alecredmond.application.inference.junctiontree;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.probabilitytables.junctiontree.JunctionTreeTable;
import io.github.alecredmond.method.inference.junctiontree.handlers.JTATableHandlerSeparator;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents a <b>Separator</b> in the Junction Tree structure, acting as a link between two
 * adjacent {@link Clique} objects. <br>
 * A Separator contains the set of nodes common to both connected cliques and is responsible for
 * passing messages (probability distributions) between them during the Junction Tree Algorithm.
 */
@Data
@AllArgsConstructor
public class Separator {
  /** The first {@link Clique} connected by this Separator. */
  private Clique cliqueA;

  /** The second {@link Clique} connected by this Separator. */
  private Clique cliqueB;

  /** The set of {@link Node} objects common to both {@code cliqueA} and {@code cliqueB}. */
  private Set<Node> connectingNodes;

  /**
   * The {@link JunctionTreeTable} storing the joint probability distribution over the common nodes.
   */
  private JunctionTreeTable table;

  /** The handler responsible for JTA operations specific to the Separator's probability table. */
  private JTATableHandlerSeparator handler;

  /**
   * Constructs a new Separator, initializing its table and handler. <br>
   *
   * @param cliqueA The first clique in the pair.
   * @param cliqueB The second clique in the pair.
   * @param connectingNodes The set of nodes common to both cliques.
   * @param table The {@link JunctionTreeTable} representing the separator's probability.
   */
  public Separator(
      Clique cliqueA, Clique cliqueB, Set<Node> connectingNodes, JunctionTreeTable table) {
    this.cliqueA = cliqueA;
    this.cliqueB = cliqueB;
    this.connectingNodes = connectingNodes;
    this.table = table;
    this.handler = new JTATableHandlerSeparator(this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("SEPARATOR : ");
    connectingNodes.forEach(node -> sb.append(node.toString()).append(" "));
    return sb.toString();
  }
}
