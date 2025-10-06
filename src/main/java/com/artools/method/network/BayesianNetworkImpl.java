package com.artools.method.network;

import static com.artools.method.constraints.ConstraintBuilder.*;

import com.artools.application.network.BayesianNetworkData;
import com.artools.application.solver.SolverConfigs;
import com.artools.method.printer.NetworkPrinter;
import com.artools.method.sampler.NetworkSampler;
import java.util.*;
import lombok.Getter;

@Getter
public class BayesianNetworkImpl implements BayesianNetwork {
  private final BayesianNetworkData networkData;
  private final SolverConfigs solverConfigs;
  private final NetworkDataUtils utils;
  private final NetworkSampler sampler;

  public BayesianNetworkImpl(String networkName) {
    this.networkData = new BayesianNetworkData();
    networkData.setNetworkName(networkName);
    this.utils = new NetworkDataUtils(networkData);
    this.solverConfigs = new SolverConfigs();
    this.sampler = new NetworkSampler(networkData, solverConfigs);
  }

  public BayesianNetworkImpl() {
    this.networkData = new BayesianNetworkData();
    networkData.setNetworkName("UNNAMED_NETWORK");
    this.utils = new NetworkDataUtils(networkData);
    this.solverConfigs = new SolverConfigs();
    this.sampler = new NetworkSampler(networkData, solverConfigs);
  }

  public <T> BayesianNetworkImpl addNode(T nodeID) {
    utils.addNode(nodeID);
    return this;
  }

  public <T, E> BayesianNetworkImpl addNode(T nodeID, Collection<E> nodeStateIDs) {
    utils.addNode(nodeID, nodeStateIDs);
    return this;
  }

  public <T> BayesianNetworkImpl removeNode(T nodeID) {
    utils.removeNode(nodeID);
    return this;
  }

  public BayesianNetworkImpl removeAllNodes() {
    utils.resetAllNodeData();
    return this;
  }

  public <T, E> BayesianNetworkImpl addNodeStates(T nodeID, Collection<E> nodeStateIDs) {
    utils.addNodeStates(nodeID, nodeStateIDs);
    return this;
  }

  public <T, E> BayesianNetworkImpl addNodeState(T nodeID, E nodeStateID) {
    utils.addNodeState(nodeID, nodeStateID);
    return this;
  }

  public <T> BayesianNetworkImpl removeNodeStates(T nodeID) {
    utils.removeNodeStates(nodeID);
    return this;
  }

  public <T, E> BayesianNetworkImpl removeNodeState(T nodeID, E nodeStateID) {
    utils.removeNodeState(nodeID, nodeStateID);
    return this;
  }

  public <T, E> BayesianNetworkImpl addParents(T childID, Collection<E> parentIDs) {
    utils.addParents(childID, parentIDs);
    return this;
  }

  public <T, E> BayesianNetworkImpl addParent(T childID, E parentID) {
    utils.addParent(childID, parentID);
    return this;
  }

  public <T, E> BayesianNetworkImpl removeParent(T childID, E parentID) {
    utils.removeParent(childID, parentID);
    return this;
  }

  public <T> BayesianNetworkImpl removeParents(T childID) {
    utils.removeParents(childID);
    return this;
  }

  public <T, E> BayesianNetworkImpl addConstraint(
      T eventStateID, Collection<E> conditionStateIDs, double probability) {
    networkData.setSolved(false);
    networkData
        .getConstraints()
        .add(buildConstraint(eventStateID, conditionStateIDs, probability, networkData));
    return this;
  }

  public <T> BayesianNetworkImpl addConstraint(T eventStateID, double probability) {
    networkData.setSolved(false);
    networkData.getConstraints().add(buildConstraint(eventStateID, probability, networkData));
    return this;
  }

  public <T, E> BayesianNetworkImpl addConstraint(
      Collection<T> eventStateIDs, Collection<E> conditionStateIDs, double probability) {
    networkData.setSolved(false);
    networkData
        .getConstraints()
        .add(buildConstraint(eventStateIDs, conditionStateIDs, probability, networkData));
    return this;
  }

  public BayesianNetworkImpl solverCyclesLimit(int cyclesLimit) {
    solverConfigs.setCyclesLimit(cyclesLimit);
    return this;
  }

  public BayesianNetworkImpl solverTimeLimit(int timeLimitSeconds) {
    solverConfigs.setTimeLimitSeconds(timeLimitSeconds);
    return this;
  }

  public BayesianNetworkImpl logIntervalSeconds(int seconds) {
    solverConfigs.setLogIntervalSeconds(seconds);
    return this;
  }

  public BayesianNetworkImpl solverConvergeThreshold(double threshold) {
    solverConfigs.setConvergeThreshold(threshold);
    return this;
  }

  public BayesianNetworkImpl solveNetwork() {
    utils.buildNetworkData();
    sampler.runSolver();
    return this;
  }

  public BayesianNetworkImpl printNetwork() {
    new NetworkPrinter(networkData).printNetwork(true);
    return this;
  }

  public BayesianNetworkImpl printObserved() {
    new NetworkPrinter(networkData).printObserved(true);
    return this;
  }

  public BayesianNetworkImpl printObserved(boolean toFile) {
    new NetworkPrinter(networkData).printObserved(toFile);
    return this;
  }

  public BayesianNetworkImpl printNetwork(boolean toFile) {
    new NetworkPrinter(networkData).printNetwork(toFile);
    return this;
  }

  public BayesianNetworkImpl printNetwork(String directory) {
    new NetworkPrinter(networkData, directory).printNetwork(true);
    return this;
  }

  public BayesianNetworkImpl printObserved(String directory) {
    new NetworkPrinter(networkData, directory).printNetwork(true);
    return null;
  }

  public <T> BayesianNetworkImpl observeNetwork(Collection<T> observedNodeStateIDs) {
    sampler.observeNetwork(utils.getStatesByID(observedNodeStateIDs));
    return this;
  }

  public BayesianNetworkImpl observeMarginals() {
    sampler.observeNetwork(List.of());
    return this;
  }

  public <T, E> List<List<T>> generateSamples(
      int numberOfSamples,
      Collection<E> excludeNodeIDs,
      Collection<E> includeNodeIDs,
      Class<T> tClass) {
    return sampler.generateSamples(
        numberOfSamples,
        utils.getNodesByID(excludeNodeIDs),
        utils.getNodesByID(includeNodeIDs),
        tClass);
  }

  public <T> double observeProbability(Collection<T> eventStates) {
    return sampler.observeProbability(
        utils.getStatesByID(eventStates));
  }
}
