package io.github.alecredmond.internal.method.node;

import io.github.alecredmond.exceptions.NodeStateConflictException;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NodeUtils {

  private NodeUtils() {}

  public static Map<Node, NodeState> generateRequest(Collection<NodeState> states) {
    try {
      return states.stream()
          .map((state -> Map.entry(state.getNode(), state)))
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    } catch (IllegalStateException e) {
      throw new NodeStateConflictException(
          "Error generating request : Multiple values of NodeState shared the same Node", e);
    }
  }

  public static Set<NodeState> combineStates(
      Collection<NodeState> eventStates, Collection<NodeState> conditionStates) {
    return Stream.concat(eventStates.stream(), conditionStates.stream())
        .collect(Collectors.toSet());
  }

  public static void addParent(Node child, Node parent) {
    if (!child.getParents().contains(parent)) {
      addToList(child.getParents(), parent, child::setParents);
    }
    if (!parent.getChildren().contains(child)) {
      addToList(parent.getChildren(), child, parent::setChildren);
    }
  }

  private static <E> List<E> addToList(List<E> list, E element, Consumer<List<E>> setter) {
    return addAllToList(list, List.of(element), setter);
  }

  private static <E> List<E> addAllToList(
      List<E> list, Collection<E> elements, Consumer<List<E>> setter) {
    List<E> newList = Stream.concat(list.stream(), elements.stream()).toList();
    setter.accept(newList);
    return newList;
  }

  public static List<Set<NodeState>> splitStatesSharingNodes(Collection<NodeState> states) {
    return states.stream().collect(Collectors.groupingBy(NodeState::getNode)).values().stream()
        .reduce(List.of(new HashSet<>()), NodeUtils::accumulatePerturbations, (x, y) -> y);
  }

  private static List<Set<NodeState>> accumulatePerturbations(
      List<Set<NodeState>> previousAccumulation, List<NodeState> statesWithSharedNode) {
    return previousAccumulation.stream()
        .flatMap(
            currentSet ->
                statesWithSharedNode.stream()
                    .map(
                        state -> {
                          Set<NodeState> newPerturbation = new HashSet<>(currentSet);
                          newPerturbation.add(state);
                          return newPerturbation;
                        }))
        .toList();
  }

  public static Set<Node> getNodes(Collection<NodeState> states) {
    return states.stream().map(NodeState::getNode).collect(Collectors.toSet());
  }

  public static Map<Node, Integer> buildNodeIndexMap(Node[] nodes) {
    return IntStream.range(0, nodes.length)
        .mapToObj(i -> Map.entry(nodes[i], i))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  public static Map<NodeState, Integer> buildStateIndexMap(Node[] nodes) {
    Map<NodeState, Integer> statePositions = new HashMap<>();
    Arrays.stream(nodes)
        .map(Node::getNodeStates)
        .forEach(
            states ->
                IntStream.range(0, states.size())
                    .forEach(i -> statePositions.put(states.get(i), i)));
    return statePositions;
  }

  public static String formatStatesToString(Collection<NodeState> stateCollection) {
    return formatCollectionToString(stateCollection);
  }

  private static <T> String formatCollectionToString(Collection<T> collection) {
    if (collection.isEmpty()) return "";
    StringBuilder sb = new StringBuilder();
    collection.forEach(c -> sb.append(c.toString()).append(", "));
    if (sb.length() >= 2) {
      sb.setLength(sb.length() - 2);
    }
    return sb.toString();
  }

  public static String formatIDsToString(Collection<Serializable> ids) {
    return formatCollectionToString(ids);
  }

  public static String formatNodesToString(Collection<Node> nodeCollection) {
    return formatCollectionToString(nodeCollection);
  }

  public static List<Serializable> getNodeIds(Collection<Node> nodes) {
    return nodes.stream().map(Node::getId).toList();
  }

  public static List<Serializable> getNodeStateIds(Collection<Node> nodes) {
    return nodes.stream().flatMap(n -> n.getNodeStates().stream()).map(NodeState::getId).toList();
  }

  public static <T extends Serializable> NodeState addNodeState(Node node, T stateID) {
    return addToList(node.getNodeStates(), new NodeState(stateID, node), node::setNodeStates)
        .getLast();
  }

  public static <E extends Serializable> void addNodeStates(Node node, Collection<E> stateIDs) {
    List<NodeState> newStates = stateIDs.stream().map(id -> new NodeState(id, node)).toList();
    addAllToList(node.getNodeStates(), newStates, node::setNodeStates);
  }

  public static <E extends Serializable> void removeState(Node node, E stateID) {
    removeFromList(node.getNodeStates(), s -> s.getId().equals(stateID), node::setNodeStates);
  }

  private static <E> void removeFromList(
      List<E> list, Predicate<E> predicate, Consumer<List<E>> setter) {
    setter.accept(list.stream().filter(predicate.negate()).toList());
  }

  public static void removeParent(Node node, Node parent) {
    if (node.getParents().contains(parent)) {
      removeFromList(node.getParents(), parent::equals, node::setParents);
    }
    if (parent.getChildren().contains(node)) {
      removeFromList(parent.getChildren(), node::equals, parent::setChildren);
    }
  }

  public static Set<Node> getOverlap(Set<Node> nodesA, Set<Node> nodesB) {
    return nodesA.stream()
        .filter(nodesB::contains)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }
}
