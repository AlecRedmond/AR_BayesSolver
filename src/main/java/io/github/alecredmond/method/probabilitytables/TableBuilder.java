package io.github.alecredmond.method.probabilitytables;

import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.ConditionalTable;
import io.github.alecredmond.application.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.application.probabilitytables.MarginalTable;
import io.github.alecredmond.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.exceptions.TableBuilderException;
import io.github.alecredmond.method.probabilitytables.probabilityvector.ProbabilityVectorFactory;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TableBuilder {

  private TableBuilder() {}

  public static ProbabilityTable buildNetworkTable(List<Node> events, List<Node> conditions) {
    if (events.isEmpty())
      throw new TableBuilderException(
          "attempted to buildRatioWriter a table with no events!"); // TODO - Hit Branch In Test Suite
    if (!conditions.isEmpty()) return buildConditionalTable(events, conditions);
    if (events.size() == 1) return buildMarginalTable(new HashSet<>(events));
    throw new TableBuilderException(
        "Could not buildRatioWriter a marginal or conditional table from request!"); // TODO - Hit Branch In
    // Test Suite
  }

  private static ProbabilityTable buildConditionalTable(List<Node> events, List<Node> conditions) {
    List<Node> nodesList = joinEventsAndConditions(events, conditions);
    Set<Node> nodes = new LinkedHashSet<>(nodesList);

    ProbabilityVector vector = new ProbabilityVectorFactory().build(nodesList);
    Node eventNode = events.size() == 1 ? events.stream().findAny().orElseThrow() : null;
    Map<Object, Node> nodeIDMap = buildNodeIDMap(nodes);
    Map<Object, NodeState> nodeStateIDMap = buildNodeStateIDMap(nodes);
    return new ConditionalTable(
        buildTableName(events, conditions),
        vector,
        nodes,
        new LinkedHashSet<>(events),
        new LinkedHashSet<>(conditions),
        eventNode,
        nodeIDMap,
        nodeStateIDMap);
  }

  public static MarginalTable buildMarginalTable(Set<Node> events) {
    Node eventNode = events.stream().findAny().orElseThrow();
    String tableName = buildTableName(Set.of(eventNode), new HashSet<>());
    ProbabilityVector vector = new ProbabilityVectorFactory().build(new ArrayList<>(events));
    Map<Object, Node> nodeIDMap = buildNodeIDMap(List.of(eventNode));
    Map<Object, NodeState> nodeStateIDMap = buildNodeStateIDMap(List.of(eventNode));
    return new MarginalTable(vector, tableName, eventNode, nodeStateIDMap, nodeIDMap);
  }

  private static List<Node> joinEventsAndConditions(List<Node> events, List<Node> conditions) {
    return Stream.concat(conditions.stream(), events.stream()).toList();
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

  private static String buildTableName(Collection<Node> events, Collection<Node> conditions) {
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

  public static JunctionTreeTable buildJunctionTreeTable(
      Set<Node> events, BayesianNetworkData bnd) {
    List<Node> orderedEvents = bnd.getNodes().stream().filter(events::contains).toList();
    ProbabilityVector vector = new ProbabilityVectorFactory().build(orderedEvents);
    ProbabilityVector observedVector = new ProbabilityVectorFactory().build(orderedEvents);
    Map<ProbabilityTable, Integer[]> equivalentIndexes = new HashMap<>();
    Map<Object, NodeState> nodeStateIDMap = buildNodeStateIDMap(events);
    Map<Object, Node> nodeIDMap = buildNodeIDMap(events);
    return new JunctionTreeTable(
        buildTableName(orderedEvents, new HashSet<>()),
        vector,
        new LinkedHashSet<>(orderedEvents),
        observedVector,
        equivalentIndexes,
        nodeStateIDMap,
        nodeIDMap);
  }
}
