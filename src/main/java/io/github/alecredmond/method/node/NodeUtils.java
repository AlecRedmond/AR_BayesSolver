package io.github.alecredmond.method.node;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.exceptions.NodeStateConflictException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
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
      throw new NodeStateConflictException(e);
    }
  }

  public static Set<NodeState> combineStates(
      Collection<NodeState> eventStates, Collection<NodeState> conditionStates) {
    return Stream.concat(eventStates.stream(), conditionStates.stream())
        .collect(Collectors.toSet());
  }

  public static Set<Node> getNodes(Collection<NodeState> states) {
    return states.stream().map(NodeState::getNode).collect(Collectors.toSet());
  }

  public static void addParent(Node node, Node parent) {
    addToList(node.getParents(), parent, node::setParents);
    addToList(parent.getChildren(), node, parent::setChildren);
  }

  private static <E> void addToList(List<E> list, E element, Consumer<List<E>> setter) {
    actOnList(
        list,
        l -> l.add(element),
        l -> setter.accept(Stream.concat(l.stream(), Stream.of(element)).distinct().toList()));
  }

  private static <E> void actOnList(
      List<E> list, Consumer<List<E>> arrayListConsumer, Consumer<List<E>> listConsumer) {
    if (list instanceof ArrayList<E> arrayList) {
      arrayListConsumer.accept(arrayList);
      return;
    }
    listConsumer.accept(list);
  }

  public static String formatToString(Collection<NodeState> stateCollection) {
    StringBuilder sb = new StringBuilder();
    stateCollection.forEach(state -> sb.append(state.toString()).append(", "));
    sb.setLength(sb.length() - 2);
    return sb.toString();
  }

  public static <T> NodeState addNodeState(Node node, T stateID) {
    NodeState state = new NodeState(stateID, node);
    addToList(node.getNodeStates(), state, node::setNodeStates);
    return state;
  }

  public static <E> void removeState(Node node, E stateID) {
    removeFromList(node.getNodeStates(), s -> s.getId().equals(stateID), node::setNodeStates);
  }

  private static <E> void removeFromList(
      List<E> list, Predicate<E> predicate, Consumer<List<E>> setter) {
    actOnList(
        list,
        l -> l.removeIf(predicate),
        l -> setter.accept(l.stream().filter(predicate).toList()));
  }

  public static void removeParent(Node node, Node parent) {
    removeFromList(node.getParents(), parent::equals, node::setParents);
    removeFromList(parent.getChildren(), node::equals, parent::setChildren);
  }
}
