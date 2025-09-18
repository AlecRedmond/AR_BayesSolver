package com.artools.method.probabilitytables;

import com.artools.application.node.Node;
import com.artools.application.node.NodeState;
import com.artools.application.probabilitytables.JunctionTreeTable;
import com.artools.application.probabilitytables.MarginalTable;
import com.artools.application.probabilitytables.ProbabilityTable;
import com.artools.method.node.NodeUtils;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;

public class TableUtils {

    private TableUtils(){}

  public static void fillObservedTable(MarginalTable observedTable, ProbabilityTable bestTable) {
    observedTable
        .getKeySet()
        .forEach(
            key -> {
              double sum = getMapSum(bestTable.getProbabilitiesMap(), key);
              observedTable.setProbability(key, sum);
            });
  }

  private static double getMapSum(Map<Set<NodeState>, Double> map, Set<NodeState> observedStates) {
    return map.entrySet().stream()
        .filter(entry -> entry.getKey().containsAll(observedStates))
        .mapToDouble(Map.Entry::getValue)
        .sum();
  }

  public static void recalculateObservedProbabilityMap(JunctionTreeTable table) {
    if (table.getObservedStates().isEmpty()) return;
    table
        .getProbabilitiesMap()
        .forEach(
            (keySet, value) -> {
              double newVal = keySet.containsAll(table.getObservedStates()) ? value : 0.0;
              table.setObservedProb(keySet, newVal);
            });
  }

  public static void adjustProbabilityFromObserved(JunctionTreeTable table) {
    if (table.getObservedStates().isEmpty()) return;
    double observedMapSum = getMapSum(table.getObservedProbMap(), table.getObservedStates());
    double probMapSum = getMapSum(table.getProbabilitiesMap(), table.getObservedStates());
    double probMapRatio = observedMapSum == 0 ? 0 : probMapSum / observedMapSum;

    table.getKeySet().stream()
        .filter(keySet -> keySet.containsAll(table.getObservedStates()))
        .forEach(
            keySet -> {
              double expected = table.getObservedProb(keySet) * probMapRatio;
              table.setProbability(keySet, expected);
            });

    marginalizeTable(table);
  }

  public static void marginalizeTable(ProbabilityTable table) {
    Set<Node> conditions = table.getConditions();
    if (conditions.isEmpty()) marginalizeWithoutConditions(table);
    else marginalizeWithConditions(table, conditions);
  }

  private static void marginalizeWithoutConditions(ProbabilityTable table) {
    double tableSum = table.getProbabilitiesMap().values().stream().mapToDouble(d -> d).sum();
    if (tableSum == 0) return;
    double ratio = 1 / tableSum;
    Set<Set<NodeState>> requests = table.getProbabilitiesMap().keySet();
    requests.forEach(r -> table.setProbability(r, table.getProbability(r) * ratio));
  }

  private static void marginalizeWithConditions(ProbabilityTable table, Set<Node> conditions) {
    NodeUtils.generateStateCombinations(conditions)
        .forEach(
            conditionCombo -> {
              Set<Set<NodeState>> conditionKeys =
                  table.getKeySet().stream()
                      .filter(aDouble -> aDouble.containsAll(conditionCombo))
                      .collect(Collectors.toSet());
              double conditionSum = conditionKeys.stream().mapToDouble(table::getProbability).sum();
              if (conditionSum == 0) return;
              double ratio = 1 / conditionSum;
              conditionKeys.forEach(k -> table.setProbability(k, table.getProbability(k) * ratio));
            });
  }
}
