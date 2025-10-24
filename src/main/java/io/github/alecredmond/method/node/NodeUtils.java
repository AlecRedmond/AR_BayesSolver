package io.github.alecredmond.method.node;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NodeUtils {

  private NodeUtils() {}

  public static List<Set<NodeState>> generateStateCombinations(Set<Node> nodes) {
    List<Set<NodeState>> stateCombos = new ArrayList<>();
    List<Node> nodesList = new ArrayList<>(nodes);
    recursiveStateCombinationBuilder(stateCombos, nodesList, 0, new ArrayList<>());
    return stateCombos;
  }

  public static Set<NodeState> combineStates(
      Collection<NodeState> eventStates, Collection<NodeState> conditionStates) {
    return Stream.concat(eventStates.stream(), conditionStates.stream())
        .collect(Collectors.toSet());
  }

  public static Set<Node> getNodes(Collection<NodeState> states) {
    return states.stream().map(NodeState::getNode).collect(Collectors.toSet());
  }

  public static List<Set<Node>> generateNodeCombinations(List<Node> toCombine, int blockSize) {
    if (toCombine.size() < blockSize) {
      log.error(
          "Requested block size {} for Node collection of size {}. Reducing to collection size...",
          blockSize,
          toCombine.size());
      blockSize = toCombine.size();
    }
    List<Set<Node>> combos = new ArrayList<>();
    recursiveNodeCombinationBuilder(toCombine, blockSize, combos, new ArrayList<>(), 0);
    return combos;
  }

  private static void recursiveNodeCombinationBuilder(
      List<Node> toCombine,
      int blockSize,
      List<Set<Node>> nodeSets,
      List<Node> currentNodes,
      int index) {
    if (blockSize == 0) {
      nodeSets.add(new HashSet<>(currentNodes));
      return;
    }
    for (int i = index; i < toCombine.size(); i++) {
      currentNodes.add(toCombine.get(i));
      recursiveNodeCombinationBuilder(toCombine, blockSize - 1, nodeSets, currentNodes, i + 1);
      currentNodes.removeLast();
    }
  }

  private static void recursiveStateCombinationBuilder(
      List<Set<NodeState>> stateCombos,
      List<Node> nodesList,
      int depth,
      List<NodeState> currentCombo) {
    if (depth == nodesList.size()) {
      stateCombos.add(new HashSet<>(currentCombo));
      return;
    }

    Node currentNode = nodesList.get(depth);
    List<NodeState> currentNodeStates = currentNode.getNodeStates();

    for (NodeState state : currentNodeStates) {
      currentCombo.add(state);
      recursiveStateCombinationBuilder(stateCombos, nodesList, depth + 1, currentCombo);
      currentCombo.removeLast();
    }
  }
}
