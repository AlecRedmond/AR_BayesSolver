package io.github.alecredmond.method.inference;

import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import java.util.*;

abstract class Sampler<T> {

  public abstract List<List<T>> generateSamples(
          BayesianNetworkData data,
      Map<Node, NodeState> observations,
      Set<Node> excludedNodes,
      Set<Node> includedNodes,
      int numberOfSamples);

  protected List<T> convertToIDs(
      Set<NodeState> states, Set<Node> excludedNodes, Set<Node> includedNodes, Class<T> tClass) {
    return states.stream()
        .filter(ns -> !excludedNodes.contains(ns.getNode()))
        .filter(ns -> includedNodes.contains(ns.getNode()))
        .map(NodeState::getStateID)
        .map(tClass::cast)
        .toList();
  }
}
