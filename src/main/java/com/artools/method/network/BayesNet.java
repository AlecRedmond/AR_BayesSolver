package com.artools.method.network;

import static com.artools.method.constraints.ConstraintBuilder.*;

import com.artools.application.network.BayesNetData;
import com.artools.application.node.Node;
import com.artools.application.node.NodeState;
import com.artools.application.solver.SolverConfigs;
import com.artools.export.BayesianNetwork;
import com.artools.method.probabilitytables.TableBuilder;
import com.artools.method.sampler.JunctionTreeAlgorithm;
import com.artools.method.sampler.NetworkSampler;
import com.artools.method.solver.BayesSolver;
import java.util.*;
import lombok.Getter;

@Getter
public class BayesNet implements BayesianNetwork {
  private final BayesNetData networkData;
  private final SolverConfigs solverConfigs;
  private NetworkSampler sampler;

  public BayesNet() {
    this.networkData = new BayesNetData();
    this.solverConfigs = new SolverConfigs();
    this.sampler = null;
  }

  public <T> BayesNet addNode(T nodeID) {
    networkData.getNodesMap().put(nodeID, new Node(nodeID));
    networkData.setSolved(false);
    return this;
  }

  public <T, E> BayesNet addNode(T nodeID, Collection<E> nodeStateIDs) {
    Node newNode = new Node(nodeID, nodeStateIDs);
    networkData.getNodes().add(newNode);
    networkData.getNodesMap().put(nodeID, newNode);
    addStatesToMap(newNode);
    networkData.setSolved(false);
    return this;
  }

  private void addStatesToMap(Node node) {
    node.getStates().forEach(state -> networkData.getNodeStateMap().put(state.getStateID(), state));
  }

  public <T> BayesNet removeNode(T nodeID) {
    if (!networkData.getNodesMap().containsKey(nodeID)) return this;
    Node toRemove = networkData.getNodesMap().get(nodeID);
    networkData.getNetworkTablesMap().remove(toRemove);
    networkData.getObservationMap().remove(toRemove);
    networkData.getNodesMap().remove(nodeID);
    removeStatesFromMap(toRemove);
    networkData.setSolved(false);
    return this;
  }

  private void removeStatesFromMap(Node toRemove) {
    toRemove.getStates().forEach(state -> networkData.getNodeStateMap().remove(state.getStateID()));
  }

  public BayesNet removeAllNodes() {
    networkData.setNodes(new ArrayList<>());
    networkData.setNodesMap(new HashMap<>());
    networkData.setNodeStateMap(new HashMap<>());
    networkData.setNetworkTablesMap(new HashMap<>());
    networkData.setObservationMap(new HashMap<>());
    networkData.setSolved(false);
    return this;
  }

  public <T, E> BayesNet addNodeStates(T nodeID, Collection<E> nodeStateIDs) {
    nodeStateIDs.forEach(sID -> addNodeState(nodeID, sID));
    return this;
  }

  public <T, E> BayesNet addNodeState(T nodeID, E nodeStateID) {
    Node node = networkData.getNodesMap().get(nodeID);
    NodeState state = node.addState(nodeStateID);
    networkData.getNodeStateMap().put(nodeStateID, state);
    networkData.setSolved(false);
    return this;
  }

  public <T> BayesNet removeNodeStates(T nodeID) {
    networkData.getNodesMap().get(nodeID).getStates().stream()
        .map(NodeState::getStateID)
        .forEach(sID -> removeNodeState(nodeID, sID));
    return this;
  }

  public <T, E> BayesNet removeNodeState(T nodeID, E nodeStateID) {
    networkData.getNodesMap().get(nodeID).removeState(nodeStateID);
    networkData.getNodeStateMap().remove(nodeStateID);
    networkData.setSolved(false);
    return this;
  }

  public <T, E> BayesNet addParents(T childID, Collection<E> parentIDs) {
    parentIDs.forEach(pID -> addParent(childID, pID));
    return this;
  }

  public <T, E> BayesNet addParent(T childID, E parentID) {
    networkData.getNodesMap().get(childID).addParent(networkData.getNodesMap().get(parentID));
    networkData.getNodesMap().get(parentID).addChild(networkData.getNodesMap().get(childID));
    networkData.setSolved(false);
    return this;
  }

  public <T, E> BayesNet removeParent(T childID, E parentID) {
    networkData.getNodesMap().get(childID).removeParent(networkData.getNodesMap().get(parentID));
    networkData.getNodesMap().get(parentID).removeChild(networkData.getNodesMap().get(childID));
    networkData.setSolved(false);
    return this;
  }

  public <T> BayesNet removeParents(T childID) {
    networkData.getNodesMap().get(childID).getParents().stream()
        .map(Node::getNodeID)
        .forEach(parentID -> removeParent(childID, parentID));
    return this;
  }

  public <T, E> BayesNet addConstraint(
      T eventStateID, Collection<E> conditionStateIDs, double probability) {
    networkData.setSolved(false);
    networkData
        .getConstraints()
        .add(buildConstraint(eventStateID, conditionStateIDs, probability, networkData));
    return this;
  }

  public <T> BayesNet addConstraint(T eventStateID, double probability) {
    networkData.setSolved(false);
    networkData.getConstraints().add(buildConstraint(eventStateID, probability, networkData));
    return this;
  }

  public <T, E> BayesNet addConstraint(
      Collection<T> eventStateIDs, Collection<E> conditionStateIDs, double probability) {
    networkData.setSolved(false);
    networkData
        .getConstraints()
        .add(buildConstraint(eventStateIDs, conditionStateIDs, probability, networkData));
    return this;
  }

  public BayesNet solverCyclesLimit(int cyclesLimit) {
    solverConfigs.setCyclesLimit(cyclesLimit);
    return this;
  }

  public BayesNet solverTimeLimit(int timeLimitSeconds) {
    solverConfigs.setTimeLimitSeconds(timeLimitSeconds);
    return this;
  }

  public BayesNet logIntervalSeconds(int seconds) {
    solverConfigs.setLogIntervalSeconds(seconds);
    return this;
  }

  public BayesNet solverConvergeThreshold(double threshold) {
    solverConfigs.setConvergeThreshold(threshold);
    return this;
  }

  public BayesNet solveNetwork() {
    TableBuilder.buildNetworkTables(networkData);
    TableBuilder.buildObservationMap(networkData);
    new BayesSolver(networkData, solverConfigs).solveNetwork();
    networkData.setSolved(true);
    sampler = new JunctionTreeAlgorithm(networkData);
    return this;
  }

  @Override
  public BayesNet printNetwork() {
    networkData
        .getNetworkTablesMap()
        .forEach(
            (node, table) -> {
              System.out.println(table.getTableID());
              table
                  .getIndexMap()
                  .forEach(
                      (keyset, index) -> {
                        StringBuilder sb = new StringBuilder("\t");
                        keyset.stream()
                            .sorted(Comparator.comparing(NodeState::toString))
                            .forEach(state -> sb.append(state).append(", "));
                        sb.append(table.getProbability(keyset));
                        System.out.println(sb);
                      });
            });
    return this;
  }

  @Override
  public <T> BayesNet observeNetwork(Collection<T> observedNodeStateIDs) {
    if (observedNodeStateIDs.isEmpty()) {
      observeMarginals();
      return this;
    }

    if (!networkData.isSolved()) solveNetwork();

    List<NodeState> states =
        observedNodeStateIDs.stream().map(networkData.getNodeStateMap()::get).toList();
    Map<Node, NodeState> observations = sampler.convertToEvidence(states);
    sampler.sampleNetwork(observations);
    return this;
  }

  @Override
  public BayesNet observeMarginals() {
    if (!networkData.isSolved()) solveNetwork();
    sampler.sampleNetwork();
    return this;
  }

  @Override
  public BayesNet printObserved() {
    networkData
        .getObservationMap()
        .forEach(
            (node, table) -> {
              System.out.println(table.getTableID());
              table
                  .getIndexMap()
                  .forEach(
                      (keyset, index) -> {
                        StringBuilder sb = new StringBuilder("\t");
                        keyset.stream()
                            .sorted(Comparator.comparing(NodeState::toString))
                            .forEach(state -> sb.append(state).append(", "));
                        sb.append(table.getProbability(keyset));
                        System.out.println(sb);
                      });
            });
    return this;
  }
}
