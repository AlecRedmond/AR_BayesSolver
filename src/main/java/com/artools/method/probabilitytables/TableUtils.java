package com.artools.method.probabilitytables;

import com.artools.application.node.Node;
import com.artools.application.node.NodeState;
import com.artools.application.probabilitytables.ProbabilityTable;
import com.artools.method.node.NodeUtils;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class TableUtils {

  private TableUtils() {}

  public static void marginalizeTable(ProbabilityTable table) {
    Set<Node> conditions = table.getConditions();
    if (conditions.isEmpty()) marginalizeWithoutConditions(table);
    else marginalizeWithConditions(table, conditions);
  }

  private static void marginalizeWithoutConditions(ProbabilityTable table) {
    double tableSum = Arrays.stream(table.getProbabilities()).sum();
    if (tableSum == 0) return;
    double ratio = 1 / tableSum;
    Set<Set<NodeState>> keys = table.getIndexMap().keySet();
    keys.forEach(key -> multiplyEntry(table, key, ratio));
  }

  private static void marginalizeWithConditions(ProbabilityTable table, Set<Node> conditions) {
    NodeUtils.generateStateCombinations(conditions)
        .forEach(
            conditionKey -> {
              double probOfCondition = jointProbOfKey(table, conditionKey);
              if (probOfCondition == 0) return;
              double normalizationFactor = 1 / probOfCondition;
              table.getKeySet().stream()
                  .filter(key -> key.containsAll(conditionKey))
                  .forEach(key -> multiplyEntry(table, key, normalizationFactor));
            });
  }

  private static void multiplyEntry(ProbabilityTable table, Set<NodeState> states, double ratio) {
    table.setProbability(states, table.getProbability(states) * ratio);
  }

  public static double jointProbOfKey(ProbabilityTable table, Set<NodeState> key) {
    return table.getIndexMap().entrySet().stream()
        .filter(entry -> entry.getKey().containsAll(key))
        .mapToDouble(entry -> table.getProbabilities()[entry.getValue()])
        .sum();
  }

  public static void writeProbabilityMap(ProbabilityTable table) {
    Map<Set<NodeState>, Double> probMap = table.getProbabilityMap();
    if (!probMap.isEmpty()) return;
    double[] probs = table.getProbabilities();
    table.getIndexMap().forEach((key, index) -> probMap.put(key, probs[index]));
  }
}
