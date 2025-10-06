package com.artools.method.probabilitytables;

import com.artools.application.node.Node;
import com.artools.application.node.NodeState;
import com.artools.application.probabilitytables.*;
import com.artools.method.node.NodeUtils;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TableBuilder {

  private TableBuilder() {}

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
    Map<Set<NodeState>, Integer> indexMap = buildIndexMap(nodes);
    double[] probabilities = buildProbTable(indexMap.size());
    Node eventNode = events.size() == 1 ? events.stream().findAny().orElseThrow() : null;
    return new ConditionalTable(
        buildTableName(events, conditions),
        indexMap,
        probabilities,
        nodes,
        events,
        conditions,
        eventNode);
  }

  public static MarginalTable buildMarginalTable(Set<Node> events) {
    Node eventNode = events.stream().findAny().orElseThrow();
    String tableName = buildTableName(Set.of(eventNode), new HashSet<>());
    Map<Set<NodeState>, Integer> indexMap = buildIndexMap(events);
    double[] probabilities = buildProbTable(indexMap.size());
    return new MarginalTable(indexMap, probabilities, tableName, eventNode);
  }

  private static Set<Node> joinSets(Set<Node> events, Set<Node> conditions) {
    return Stream.concat(events.stream(), conditions.stream()).collect(Collectors.toSet());
  }

  private static Map<Set<NodeState>, Integer> buildIndexMap(Set<Node> nodes) {
    List<Set<NodeState>> keys = NodeUtils.generateStateCombinations(nodes);
    return IntStream.range(0, keys.size())
        .mapToObj(i -> Map.entry(keys.get(i), i))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static double[] buildProbTable(int size) {
    double[] probabilities = new double[size];
    Arrays.fill(probabilities, 1.0);
    return probabilities;
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
    Map<Set<NodeState>, Integer> indexMap = buildIndexMap(events);
    double[] probabilities = buildProbTable(indexMap.size());
    double[] observedProbabilities = buildProbTable(indexMap.size());
    Map<ProbabilityTable, Integer[]> equivalentIndexes = new HashMap<>();
    return new JunctionTreeTable(
        buildTableName(events, new HashSet<>()),
        indexMap,
        probabilities,
        events,
        observedProbabilities,
        equivalentIndexes);
  }
}
