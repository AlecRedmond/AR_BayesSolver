package io.github.alecredmond.application.probabilitytables;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

/**
 * An abstract class representing a probability table in the solver. This class serves as the base
 * for various table types like {@link ConditionalTable}, {@link MarginalTable}, and {@link
 * JunctionTreeTable}, managing the core data structure for probabilities, node/state mappings, and
 * indexing.
 */
@Getter
public abstract class ProbabilityTable {

  /** A map associating each NodeState in the table its ID */
  protected final Map<Object, NodeState> nodeStateIDMap;

  /** A map associating each Node in the table with its ID */
  protected final Map<Object, Node> nodeIDMap;

  /**
   * A map that links a unique set of NodeStates (representing a full joint assignment of all
   * associated Nodes) to the index of its probability in the main array.
   */
  protected final Map<Set<NodeState>, Integer> indexMap;

  /**
   * The main array of probability values. Data is structured in this way to allow indexes to be
   * stored by Solver handlers, allowing better performance than using a HashMap.
   */
  protected final double[] probabilities;

  /** All nodes associated with the table */
  protected final Set<Node> nodes;

  /** Event Nodes associated with the table, P(Events|Conditions) */
  protected final Set<Node> events;

  /** Condition Nodes associated with the table, P(Events|Conditions) */
  protected final Set<Node> conditions;

  /** The Identifier for the table, almost always a String used as the table's Name */
  @Setter protected Object tableID;

  /**
   * Constructs the base {@link ProbabilityTable} with all necessary data structures.
   *
   * @param nodeStateIDMap a map which can obtain a NodeState from its ID
   * @param nodeIDMap a map which can obtain a Node from its ID
   * @param indexMap a map that links every set of Node States to its associated probability on the
   *     array
   * @param tableID The Identifier for the table, almost always a String used as the table's Name
   * @param probabilities a flat array of probability values
   * @param nodes all nodes associated with the table
   * @param events Event Nodes associated with the table, P(Events|Conditions)
   * @param conditions Condition Nodes associated with the table, P(Events|Conditions)
   * @param <T> The class of the table ID
   */
  protected <T> ProbabilityTable(
      Map<Object, NodeState> nodeStateIDMap,
      Map<Object, Node> nodeIDMap,
      Map<Set<NodeState>, Integer> indexMap,
      double[] probabilities,
      T tableID,
      Set<Node> nodes,
      Set<Node> events,
      Set<Node> conditions) {
    this.nodeStateIDMap = nodeStateIDMap;
    this.nodeIDMap = nodeIDMap;
    this.indexMap = indexMap;
    this.probabilities = probabilities;
    this.tableID = tableID;
    this.nodes = nodes;
    this.events = events;
    this.conditions = conditions;
  }

  /**
   * Returns the NodeState associated with its ID
   *
   * @param <T> class of the NodeState's ID
   * @param nodeStateID the NodeState's ID
   * @return the NodeState object associated with the provided ID
   */
  public <T> NodeState getNodeState(T nodeStateID) {
    return nodeStateIDMap.get(nodeStateID);
  }

  /**
   * Gets the Network Node associated with the provided ID
   *
   * @param nodeID the Node's ID
   * @param <T> the Class of the Node's ID
   * @return the Node object associated with the provided ID
   */
  public <T> Node getNode(T nodeID) {
    return nodeIDMap.get(nodeID);
  }

  /**
   * Finds the probability for a given collection of Event and Condition NodeState IDs.
   *
   * @param stateIDs the IDs of a FULL request to the table, containing exactly one state from each
   *     node
   * @param <T> The Class of the NodeState IDs
   * @return a probability value between 0 and 1.
   */
  public <T> double getProbability(Collection<T> stateIDs) {
    return getProbability(getStates(stateIDs));
  }

  /**
   * Finds the probability for a given collection of Event and Condition NodeStates.
   *
   * @param key a set of NodeStates, one state for each Node associated with the table
   * @return a probability value between 0 and 1.
   * @throws IllegalArgumentException if the map lookup returns {@code NaN}.
   */
  public double getProbability(Set<NodeState> key) {
    double probability = probabilities[indexMap.get(key)];
    if (Double.isNaN(probability)) throw new IllegalArgumentException("map returned NaN");
    return probability;
  }

  /**
   * Private helper function, converts a collection of NodeState IDs into a set of {@code NodeState}
   * objects by resolving them using the internal ID map. If the stateIDs are instances of
   * NodeStates, those nodeStates will instead be returned.
   *
   * @param stateIDs a collection of NodeState IDs.
   * @return a Set of resolved {@code NodeState} objects.
   */
  private <T> Set<NodeState> getStates(Collection<T> stateIDs) {
    Set<NodeState> set = new HashSet<>();
    for (Object stateID : stateIDs) {
      if (stateID instanceof NodeState state) {
        set.add(state);
        continue;
      }
      NodeState state = nodeStateIDMap.get(stateID);
      set.add(state);
    }
    return set;
  }

  /**
   * Returns a set of all possible unique combinations of node states (keys) that the table holds
   * probabilities for.
   *
   * @return a Set of Sets of NodeStates.
   */
  public Set<Set<NodeState>> getKeySet() {
    return indexMap.keySet();
  }

  /**
   * Sets the probability value for a specific combination of node states (a key) in the table's
   * internal array.
   *
   * @param key a set of NodeStates defining the entry to be updated.
   * @param probability the new probability value.
   * @throws IllegalArgumentException if the key is not found in the index map or the probability is
   *     NaN.
   */
  public void setProbability(Set<NodeState> key, double probability) {
    if (!indexMap.containsKey(key)) {
      throw new IllegalArgumentException(String.format("Illegal set request to table %s", tableID));
    }
    if (Double.isNaN(probability)) throw new IllegalArgumentException("tried to add NaN");
    probabilities[indexMap.get(key)] = probability;
  }

  /**
   * Retrieves the array index associated with a specific combination of node states (key). This
   * index points to the probability value in the {@code probabilities} array.
   *
   * @param key a set of NodeStates, one for each Node associated with the table.
   * @return the index in the {@code probabilities} array.
   */
  public int getIndex(Set<NodeState> key) {
    return indexMap.get(key);
  }
}
