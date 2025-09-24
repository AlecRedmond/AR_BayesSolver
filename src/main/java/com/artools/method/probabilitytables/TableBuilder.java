package com.artools.method.probabilitytables;

import com.artools.application.network.BayesNetData;
import com.artools.application.node.Node;
import com.artools.application.node.NodeState;
import com.artools.application.probabilitytables.*;
import com.artools.method.node.NodeUtils;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TableBuilder {

  private TableBuilder() {}

  public static MarginalTable buildTable(Node eventNode) {
    return buildMarginalTable(Set.of(eventNode));
  }

  private static MarginalTable buildMarginalTable(Set<Node> events) {
    Node eventNode = events.stream().findAny().orElseThrow();
    String tableName = buildTableName(eventNode);
    Map<Set<NodeState>, Double> probabilityMap = buildProbabilityMap(events);
    return new MarginalTable(tableName, probabilityMap, eventNode);
  }

  private static String buildTableName(Node eventNode) {
    return buildTableName(Set.of(eventNode), new HashSet<>());
  }

  private static Map<Set<NodeState>, Double> buildProbabilityMap(Set<Node> nodes) {
    return NodeUtils.generateStateCombinations(nodes).stream()
        .map(combo -> Map.entry(combo, 1.0))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static String buildTableName(Set<Node> events, Set<Node> conditions) {
    StringBuilder sb = new StringBuilder("P(");
    for (Node event : events) {
      sb.append(event.getNodeID().toString()).append(" ");
    }
    if (conditions.isEmpty()) return sb.append(")").toString();
    sb.append("|");
    for (Node condition : conditions) {
      sb.append(condition.getNodeID().toString()).append(" ");
    }
    return sb.append(")").toString();
  }

    public static LogitTable createLogitTable(ProbabilityTable table) {
    String tableID = table.getTableID().toString() + " LOGIT";
    Map<Set<NodeState>, Double> logitMap = buildLogitTableLogitMap(table);
    return new LogitTable(
        tableID, logitMap, table.getNodes(), table.getEvents(), table.getConditions());
  }

  private static Map<Set<NodeState>, Double> buildLogitTableLogitMap(ProbabilityTable table) {
    return table.getProbabilitiesMap().entrySet().stream()
        .map(TableBuilder::logitTableEntry)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static Map.Entry<Set<NodeState>, Double> logitTableEntry(
      Map.Entry<Set<NodeState>, Double> entry) {
    Set<NodeState> key = entry.getKey();
    double val = (entry.getValue() != 0) ? entry.getValue() : LogitTable.ZERO_REPLACEMENT;
    return Map.entry(key, Math.log(val));
  }

  public static GradientTable buildGradientTable(ProbabilityTable table) {
    String tableID = table.getTableID().toString() + " GRADIENTS";
    Map<Set<NodeState>, Double> gradientMap =
        table.getProbabilitiesMap().keySet().stream()
            .map(key -> Map.entry(key, 1.0))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    Set<Node> nodes = table.getNodes();
    Set<Node> events = table.getEvents();
    Set<Node> conditions = table.getConditions();
    return new GradientTable(tableID, gradientMap, nodes, events, conditions);
  }

  public static void buildObservationMap(BayesNetData networkData) {
    networkData
        .getNodes()
        .forEach(
            node -> networkData.getObservationMap().put(node, buildMarginalTable(Set.of(node))));
  }

  public static void buildNetworkTables(BayesNetData networkData) {
    networkData
        .getNodesMap()
        .values()
        .forEach(
            node -> {
              Set<Node> events = Set.of(node);
              Set<Node> conditions = new HashSet<>(node.getParents());
              ProbabilityTable table = buildNetworkTable(events, conditions);
              TableUtils.marginalizeTable(table);
              networkData.getNetworkTablesMap().put(node, table);
            });
  }

  public static ProbabilityTable buildNetworkTable(Set<Node> events, Set<Node> conditions) {
    if (events.isEmpty())
      throw new IllegalArgumentException("attempted to build a table with no events!");
    if (!conditions.isEmpty()) return buildConditionalTable(events, conditions);
    if (events.size() == 1) return buildMarginalTable(events);
    throw new IllegalArgumentException(
        "Could not build a marginal or conditional table from request!");
  }

  private static ProbabilityTable buildConditionalTable(Set<Node> events, Set<Node> conditions) {
    Set<Node> nodes = joinSets(events, conditions);
    Map<Set<NodeState>, Double> probabilityMap = buildProbabilityMap(nodes);
    Node eventNode = events.size() == 1 ? events.stream().findAny().orElseThrow() : null;
    return new ConditionalTable(
        buildTableName(events, conditions), probabilityMap, nodes, events, conditions, eventNode);
  }

  private static Set<Node> joinSets(Set<Node> events, Set<Node> conditions) {
    return Stream.concat(events.stream(), conditions.stream()).collect(Collectors.toSet());
  }

  public static JunctionTreeTable buildJunctionTreeTable(Set<Node> events) {
    Map<Set<NodeState>, Double> requestMap = buildProbabilityMap(events);
    return new JunctionTreeTable(buildTableName(events, new HashSet<>()), requestMap, events);
  }
}
