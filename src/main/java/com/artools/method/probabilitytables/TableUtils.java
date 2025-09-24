package com.artools.method.probabilitytables;

import com.artools.application.node.Node;
import com.artools.application.node.NodeState;
import com.artools.application.probabilitytables.JunctionTreeTable;
import com.artools.application.probabilitytables.MarginalTable;
import com.artools.application.probabilitytables.ProbabilityTable;
import com.artools.method.node.NodeUtils;
import java.util.Map;
import java.util.Set;

public class TableUtils {

  private TableUtils() {}

  public static void fillObservedTable(MarginalTable observedTable, ProbabilityTable bestTable) {
    observedTable
        .getKeySet()
        .forEach(
            key -> {
              double sum = sumOfJointKey(bestTable.getProbabilitiesMap(), key);
              observedTable.setProbability(key, sum);
            });
  }

  private static double sumOfJointKey(
      Map<Set<NodeState>, Double> tableMap, Set<NodeState> keySet) {
    return tableMap.entrySet().stream()
        .filter(entry -> entry.getKey().containsAll(keySet))
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
    double observedMapSum = sumOfJointKey(table.getObservedProbMap(), table.getObservedStates());
    double probMapSum = sumOfJointKey(table.getProbabilitiesMap(), table.getObservedStates());
    double normalizationFactor = observedMapSum == 0 ? 0 : probMapSum / observedMapSum;

    table.getKeySet().stream()
        .filter(key -> key.containsAll(table.getObservedStates()))
        .forEach(
            key -> {
              double newEntry = normalizationFactor * table.getObservedProb(key);
              table.setProbability(key, newEntry);
            });

    table.clearObservations();
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
    Set<Set<NodeState>> keys = table.getProbabilitiesMap().keySet();
    keys.forEach(key -> multiplyEntry(table, key, ratio));
  }

  private static void marginalizeWithConditions(ProbabilityTable table, Set<Node> conditions) {
    NodeUtils.generateStateCombinations(conditions)
        .forEach(
            conditionCombo -> {
              double probOfCondition = sumOfJointKey(table, conditionCombo);
              if (probOfCondition == 0) return;
              double normalizationFactor = 1 / probOfCondition;
              table.getKeySet().stream()
                  .filter(key -> key.containsAll(conditionCombo))
                  .forEach(key -> multiplyEntry(table, key, normalizationFactor));
            });
  }

  private static void multiplyEntry(ProbabilityTable table, Set<NodeState> states, double ratio) {
    table.setProbability(states, table.getProbability(states) * ratio);
  }

  public static double sumOfJointKey(ProbabilityTable table, Set<NodeState> key) {
    return sumOfJointKey(table.getProbabilitiesMap(), key);
  }

  public static void updateNetworkTableFromJunctionTable(
      JunctionTreeTable junctionTable, ProbabilityTable networkTable) {
    networkTable
        .getKeySet()
        .forEach(
            key -> {
              double sumFromJunction = sumOfJointKey(junctionTable, key);
              networkTable.setProbability(key, sumFromJunction);
            });

    if (!networkTable.getConditions().isEmpty()) {
      marginalizeWithConditions(networkTable, networkTable.getConditions());
    }
  }
}
