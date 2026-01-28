package io.github.alecredmond.method.probabilitytables;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.ConditionalTable;
import io.github.alecredmond.application.probabilitytables.MarginalTable;
import io.github.alecredmond.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.application.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.exceptions.TableBuilderException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TableBuilder {

    private TableBuilder() {}

  public static ProbabilityTable buildNetworkTable(Set<Node> events, Set<Node> conditions) {
    if (events.isEmpty())
      throw new TableBuilderException(
          "attempted to build a table with no events!"); // TODO - Hit Branch In Test Suite
    if (!conditions.isEmpty()) return buildConditionalTable(events, conditions);
    if (events.size() == 1) return buildMarginalTable(events);
    throw new TableBuilderException(
        "Could not build a marginal or conditional table from request!"); // TODO - Hit Branch In
    // Test Suite
  }

  private static ProbabilityTable buildConditionalTable(Set<Node> events, Set<Node> conditions) {
    Set<Node> nodes = joinSets(events, conditions);
    ProbabilityVector vector = new ProbabilityVectorBuilder().build(nodes);
    ProbabilityVectorUtils utils = new ProbabilityVectorUtils(vector);
    Node eventNode = events.size() == 1 ? events.stream().findAny().orElseThrow() : null;
    Map<Object, Node> nodeIDMap = buildNodeIDMap(nodes);
    Map<Object, NodeState> nodeStateIDMap = buildNodeStateIDMap(nodes);
    return new ConditionalTable(
        buildTableName(events, conditions),
        vector,
        utils,
        nodes,
        events,
        conditions,
        eventNode,
        nodeIDMap,
        nodeStateIDMap);
  }

  public static MarginalTable buildMarginalTable(Set<Node> events) {
    Node eventNode = events.stream().findAny().orElseThrow();
    String tableName = buildTableName(Set.of(eventNode), new HashSet<>());
    ProbabilityVector vector = new ProbabilityVectorBuilder().build(events);
    ProbabilityVectorUtils utils = new ProbabilityVectorUtils(vector);
    Map<Object, Node> nodeIDMap = buildNodeIDMap(List.of(eventNode));
    Map<Object, NodeState> nodeStateIDMap = buildNodeStateIDMap(List.of(eventNode));
    return new MarginalTable(vector, utils, tableName, eventNode, nodeStateIDMap, nodeIDMap);
  }

  private static Set<Node> joinSets(Set<Node> events, Set<Node> conditions) {
    return Stream.concat(conditions.stream(), events.stream())
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  private static Map<Object, Node> buildNodeIDMap(Collection<Node> nodes) {
    return nodes.stream()
        .map(n -> Map.entry(n.getNodeID(), n))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static Map<Object, NodeState> buildNodeStateIDMap(Collection<Node> nodes) {
    return nodes.stream()
        .map(Node::getNodeStates)
        .flatMap(Collection::stream)
        .map(ns -> Map.entry(ns.getStateID(), ns))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static String buildTableName(Set<Node> events, Set<Node> conditions) {
    StringBuilder sb = new StringBuilder("P(");
    for (Node event : events) {
      sb.append(event.getNodeID().toString()).append(",");
    }
    sb.deleteCharAt(sb.length() - 1);
    if (conditions.isEmpty()) return sb.append(")").toString();
    sb.append("|");
    for (Node condition : conditions) {
      sb.append(condition.getNodeID().toString()).append(",");
    }
    sb.deleteCharAt(sb.length() - 1);
    return sb.append(")").toString();
  }

  public static JunctionTreeTable buildJunctionTreeTable(Set<Node> events) {
    ProbabilityVector vector = new ProbabilityVectorBuilder().build(events);
    ProbabilityVectorUtils utils = new ProbabilityVectorUtils(vector);
    ProbabilityVector observedVector = new ProbabilityVectorBuilder().build(events);
    Map<ProbabilityTable, Integer[]> equivalentIndexes = new HashMap<>();
    Map<Object, NodeState> nodeStateIDMap = buildNodeStateIDMap(events);
    Map<Object, Node> nodeIDMap = buildNodeIDMap(events);
    return new JunctionTreeTable(
        buildTableName(events, new HashSet<>()),
        vector,
        utils,
        events,
        observedVector,
        equivalentIndexes,
        nodeStateIDMap,
        nodeIDMap);
  }

}
