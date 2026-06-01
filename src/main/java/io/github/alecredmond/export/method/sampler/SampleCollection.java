package io.github.alecredmond.export.method.sampler;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface SampleCollection {
  Map<Node, NodeState> getObservations();

  Node[] getNodes();

  boolean isEmpty();

  int countSamples();

  int countSamplesIncludingStates(Collection<NodeState> states);

  int countSamplesIncludingStates(NodeState state);

  <T extends Serializable> int countSamplesIncludingStateIds(Collection<T> stateIds);

  <T extends Serializable> int countSamplesIncludingStateIds(T stateId);

  List<Sample> getSamples();

  List<Sample> getSamplesIncludingStates(Collection<NodeState> states);

  List<Sample> getSamplesIncludingStates(NodeState state);

  <T extends Serializable> List<Sample> getSamplesIncludingStatesById(Collection<T> stateIds);

  <T extends Serializable> List<Sample> getSamplesIncludingStatesById(T stateId);

  void displayAllNodes();

  void setDisplayedNodes(Collection<Node> nodes);

  void setDisplayedNodes(Node node);

  <T extends Serializable> void setDisplayedNodesById(Collection<T> nodeIds);

  <T extends Serializable> void setDisplayedNodesById(T nodeId);

  String toString();
}
