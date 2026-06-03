package io.github.alecredmond.internal.method.probabilitytables;

import io.github.alecredmond.exceptions.TableBuilderException;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ConditionalTable;
import io.github.alecredmond.export.application.probabilitytables.MarginalTable;
import io.github.alecredmond.export.application.probabilitytables.NetworkTable;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.application.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.ProbabilityVectorFactory;
import io.github.alecredmond.internal.method.utils.MapUtils;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class TableBuilder {

  private TableBuilder() {}

  public static NetworkTable buildNetworkTable(List<Node> events, List<Node> conditions) {
    if (events.isEmpty())
      throw new TableBuilderException("attempted to build a table with no events!");
    if (!conditions.isEmpty()) return buildConditionalTable(events, conditions);
    if (events.size() == 1) return buildMarginalTable(new HashSet<>(events));
    throw new TableBuilderException(
        "Could not build a marginal or conditional table from request!");
  }

  private static NetworkTable buildConditionalTable(List<Node> events, List<Node> conditions) {
    List<Node> nodesList = joinEventsAndConditions(events, conditions);
    Set<Node> nodes = new LinkedHashSet<>(nodesList);
    ProbabilityVector vector = new ProbabilityVectorFactory().build(nodesList);
    Node eventNode = events.size() == 1 ? events.stream().findAny().orElseThrow() : null;
    Map<Serializable, Node> nodeIDMap = buildNodeIDMap(nodes);
    Map<Serializable, NodeState> nodeStateIDMap = buildNodeStateIDMap(nodes);
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
    Map<Serializable, Node> nodeIDMap = buildNodeIDMap(List.of(eventNode));
    Map<Serializable, NodeState> nodeStateIDMap = buildNodeStateIDMap(List.of(eventNode));
    return new MarginalTable(vector, tableName, eventNode, nodeStateIDMap, nodeIDMap);
  }

  private static List<Node> joinEventsAndConditions(List<Node> events, List<Node> conditions) {
    return Stream.concat(conditions.stream(), events.stream()).toList();
  }

  private static Map<Serializable, Node> buildNodeIDMap(Collection<Node> nodes) {
    return MapUtils.mapFromInput(nodes, Node::getId, Function.identity());
  }

  private static Map<Serializable, NodeState> buildNodeStateIDMap(Collection<Node> nodes) {
    return nodes.stream()
        .map(Node::getNodeStates)
        .flatMap(Collection::stream)
        .map(ns -> Map.entry(ns.getId(), ns))
        .collect(MapUtils.collectMap());
  }

  private static String buildTableName(Collection<Node> events, Collection<Node> conditions) {
    StringBuilder sb = new StringBuilder("P(");
    sb.append(NodeUtils.formatNodesToString(events));
    if (!conditions.isEmpty()) {
      sb.append("|");
      sb.append(NodeUtils.formatNodesToString(conditions));
    }
    return sb.append(")").toString();
  }

  public static JunctionTreeTable buildJunctionTreeTable(
      Set<Node> events, BayesianNetworkData bnd) {
    List<Node> orderedEvents = bnd.getNodes().stream().filter(events::contains).toList();
    ProbabilityVector vector = new ProbabilityVectorFactory().build(orderedEvents);
    ProbabilityVector backupVector = new ProbabilityVectorFactory().build(orderedEvents);
    Map<Serializable, NodeState> nodeStateIDMap = buildNodeStateIDMap(events);
    Map<Serializable, Node> nodeIDMap = buildNodeIDMap(events);
    return new JunctionTreeTable(
        buildTableName(orderedEvents, new HashSet<>()),
        vector,
        new LinkedHashSet<>(orderedEvents),
        backupVector,
        nodeStateIDMap,
        nodeIDMap);
  }
}
