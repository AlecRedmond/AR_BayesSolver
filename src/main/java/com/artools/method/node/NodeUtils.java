package com.artools.method.node;

import com.artools.application.node.Node;
import com.artools.application.node.NodeState;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NodeUtils {

  private NodeUtils() {}

    public static List<Set<NodeState>> generateStateCombinations(Set<Node> nodes) {
    List<Set<NodeState>> stateCombos = new ArrayList<>();
    List<Node> nodesList = new ArrayList<>(nodes);
    recursiveCombinationBuilder(stateCombos, nodesList, 0, new ArrayList<>());
    return stateCombos;
  }

  public static Set<NodeState> combineStates(
      Collection<NodeState> eventStates, Collection<NodeState> conditionStates) {
    return Stream.concat(eventStates.stream(), conditionStates.stream())
        .collect(Collectors.toSet());
  }

  public static Set<Node> getNodes(Collection<NodeState> states) {
    return states.stream().map(NodeState::getParentNode).collect(Collectors.toSet());
  }

  private static void recursiveCombinationBuilder(
      List<Set<NodeState>> stateCombos,
      List<Node> nodesList,
      int depth,
      List<NodeState> currentCombo) {
    if (depth == nodesList.size()) {
      stateCombos.add(new HashSet<>(currentCombo));
      return;
    }

    Node currentNode = nodesList.get(depth);
    List<NodeState> currentNodeStates = currentNode.getStates();

    for (NodeState state : currentNodeStates) {
      currentCombo.add(state);
      recursiveCombinationBuilder(stateCombos, nodesList, depth + 1, currentCombo);
      currentCombo.removeLast();
    }
  }
}
