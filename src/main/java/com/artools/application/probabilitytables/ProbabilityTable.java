package com.artools.application.probabilitytables;

import com.artools.application.node.Node;
import com.artools.application.node.NodeState;
import java.util.*;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

/** An abstract class representing a probability table in the Bayesian Network */
@Getter
public abstract class ProbabilityTable {

  /** A map associating each NodeState in the table its ID */
  protected final Map<?, NodeState> nodeStateIDMap;

  /** A map associating each Node in the table with its ID */
  protected final Map<?, Node> nodeIDMap;

  /** A map that links a set of NodeStates with the index of its probability in the main array. */
  protected final Map<Set<NodeState>, Integer> indexMap;

  /**
   * The main array of probabilities. Data is structured in this way to allow indexes to be stored
   * by Solver handlers, allowing better performance than using a HashMap.
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
   * @param nodeStateIDMap a map which can obtain a NodeState from its ID
   * @param nodeIDMap a map which can obtain a Node from its ID
   * @param indexMap a map that links every set of Node States to its associated probability on the
   *     array
   * @param tableID The Identifier for the table, almost always a String used as the table's Name
   * @param probabilities a flat array of probability values
   * @param nodes all nodes associated with the table
   * @param events Event Nodes associated with the table, P(Events|Conditions)
   * @param conditions Condition Nodes associated with the table, P(Events|Conditions)
   */
  protected <T> ProbabilityTable(
      Map<?, NodeState> nodeStateIDMap,
      Map<?, Node> nodeIDMap,
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
   * @return the NodeState object associated with the provided ID
   */
  public <T> NodeState getNodeState(T nodeStateID) {
    return nodeStateIDMap.get(nodeStateID);
  }

  /**
   * @return the Node object associated with the provided ID
   */
  public <T> Node getNode(T nodeID) {
    return nodeIDMap.get(nodeID);
  }

  /**
   * @return the probability associated with the values of the provided state IDs.
   * @param stateIDs the IDs of a FULL request to the table, containing all event and condition
   *     states
   */
  public double getProbability(Collection<?> stateIDs) {
    return getProbability(getStates(stateIDs));
  }

  public double getProbability(Set<NodeState> key) {
    double probability = probabilities[indexMap.get(key)];
    if (Double.isNaN(probability)) throw new IllegalArgumentException("map returned NaN");
    return probability;
  }

  private Set<NodeState> getStates(Collection<?> stateIDs) {
    return stateIDs.stream().map(nodeStateIDMap::get).collect(Collectors.toSet());
  }

  public void setProbability(Set<NodeState> key, double probability) {
    if (!indexMap.containsKey(key)) {
      throw new IllegalArgumentException(String.format("Illegal set request to table %s", tableID));
    }
    if (Double.isNaN(probability)) throw new IllegalArgumentException("tried to add NaN");
    probabilities[indexMap.get(key)] = probability;
  }

  public Set<Set<NodeState>> getKeySet() {
    return indexMap.keySet();
  }

  public int getIndex(Set<NodeState> key) {
    return indexMap.get(key);
  }
}
