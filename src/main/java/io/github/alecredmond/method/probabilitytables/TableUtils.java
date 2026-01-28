package io.github.alecredmond.method.probabilitytables;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.ProbabilityTable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class TableUtils {

  private TableUtils() {}

  public static void marginalizeTable(ProbabilityTable table) {
    Set<Node> conditions = table.getConditions();
    ProbabilityVectorUtils utils = table.getUtils();
    utils.marginalizeVector(conditions);
  }

  public static Set<NodeState> collectStatesPresentInTable(
      Collection<NodeState> currentStates, ProbabilityTable table) {
    Set<Node> tableNodes = table.getNodes();
    return currentStates.stream()
        .filter(ns -> tableNodes.contains(ns.getNode()))
        .collect(Collectors.toCollection(HashSet::new));
  }
}
