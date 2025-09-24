package com.artools.method.solver.netsampler;

import com.artools.application.constraints.ParameterConstraint;
import com.artools.application.junctiontree.Clique;
import com.artools.application.junctiontree.JunctionTreeData;
import com.artools.application.junctiontree.Separator;
import com.artools.application.network.BayesNetData;
import com.artools.application.node.Node;
import com.artools.application.probabilitytables.JunctionTreeTable;
import com.artools.application.probabilitytables.ProbabilityTable;
import com.artools.method.probabilitytables.TableBuilder;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JunctionTreeDataBuilder {

  private JunctionTreeDataBuilder() {}

  public static JunctionTreeData build(BayesNetData data) {
    Set<Clique> cliques = buildCliques(data);
    Set<Separator> separators = buildSeparators(cliques);
    buildTables(cliques, separators);
    Set<Clique> leafCliques = buildLeafCliques(cliques);
    Map<Clique, Set<ProbabilityTable>> associatedTables = buildAssociatedTablesMap(cliques, data);
    List<JunctionTreeTable> allTables = buildAllTablesList(cliques, separators);
    Map<ParameterConstraint, Clique> cliqueForConstraint = findCliquesForConstraints(cliques, data);
    return new JunctionTreeData(
        data, cliques, separators, leafCliques, associatedTables, allTables, cliqueForConstraint);
  }

  private static Map<ParameterConstraint, Clique> findCliquesForConstraints(
      Set<Clique> cliques, BayesNetData data) {
    Map<ParameterConstraint, Clique> cfc = new HashMap<>();
    for (ParameterConstraint constraint : data.getConstraints()) {
      Clique bestClique =
          cliques.stream()
              .filter(clique -> clique.getNodes().containsAll(constraint.getAllNodes()))
              .min(Comparator.comparingInt(clique -> clique.getNodes().size()))
              .orElseThrow();

      cfc.put(constraint, bestClique);
    }
    return cfc;
  }

  private static List<JunctionTreeTable> buildAllTablesList(
      Set<Clique> cliques, Set<Separator> separators) {
    List<JunctionTreeTable> allTables = new ArrayList<>();
    cliques.stream().map(Clique::getTable).forEach(allTables::add);
    separators.stream().map(Separator::getTable).forEach(allTables::add);
    return allTables;
  }

  private static Map<Clique, Set<ProbabilityTable>> buildAssociatedTablesMap(
      Set<Clique> cliques, BayesNetData data) {
    Map<Clique, Set<ProbabilityTable>> associated =
        cliques.stream()
            .map(c -> Map.entry(c, new HashSet<ProbabilityTable>()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    for (ProbabilityTable table : data.getNetworkTablesMap().values()) {
      Set<Node> tableNodes = table.getNodes();
      Clique containsScope =
          cliques.stream()
              .filter(c -> c.getNodes().containsAll(tableNodes))
              .findAny()
              .orElseThrow();
      associated.get(containsScope).add(table);
    }

    return associated;
  }

  private static Set<Clique> buildCliques(BayesNetData data) {
    Map<Node, Set<Node>> graph = initializeGraph(data);
    moralizeGraph(graph, data);
    triangulateGraph(graph, data);
    Set<Set<Node>> maximalCliques = findMaximalCliques(graph);
    return maximalCliques.stream().map(Clique::new).collect(Collectors.toSet());
  }

  private static Map<Node, Set<Node>> initializeGraph(BayesNetData data) {
    Map<Node, Set<Node>> graph = new HashMap<>();
    for (Node node : data.getNodes()) {
      Set<Node> connected = new HashSet<>(node.getParents());
      connected.addAll(node.getChildren());

      data.getConstraints().stream()
          .filter(c -> c.getEventNodes().contains(node))
          .map(ParameterConstraint::getConditionNodes)
          .forEach(connected::addAll);

      graph.put(node, connected);
    }
    return graph;
  }

  private static void buildTables(Set<Clique> cliques, Set<Separator> separators) {
    cliques.forEach(
        clique -> {
          JunctionTreeTable table = TableBuilder.buildJunctionTreeTable(clique.getNodes());
          clique.setTable(table);
        });

    separators.forEach(
        separator -> {
          JunctionTreeTable table =
              TableBuilder.buildJunctionTreeTable(separator.getConnectingNodes());
          separator.setTable(table);
        });
  }

  private static void moralizeGraph(Map<Node, Set<Node>> graph, BayesNetData data) {
    for (Node node : data.getNodes()) {
      List<Node> parents = node.getParents();
      for (int i = 0; i < parents.size(); i++) {
        for (int j = i + 1; j < parents.size(); j++) {
          Node parent1 = parents.get(i);
          Node parent2 = parents.get(j);
          graph.get(parent1).add(parent2);
          graph.get(parent2).add(parent1);
        }
      }
    }
  }

  private static void triangulateGraph(Map<Node, Set<Node>> moralGraph, BayesNetData data) {

    Map<Node, Set<Node>> graph = new HashMap<>();
    moralGraph.keySet().forEach(node -> graph.put(node, new HashSet<>(moralGraph.get(node))));

    for (Node toEliminate : data.getNodes()) {
      List<Node> neighbours = graph.get(toEliminate).stream().toList();
      for (int i = 0; i < neighbours.size(); i++) {
        for (int j = i + 1; j < neighbours.size(); j++) {
          Node n1 = neighbours.get(i);
          Node n2 = neighbours.get(j);
          moralGraph.get(n1).add(n2);
          moralGraph.get(n2).add(n1);
        }
      }
      neighbours.forEach(neighbour -> graph.get(neighbour).remove(toEliminate));
    }
  }

  private static Set<Set<Node>> findMaximalCliques(Map<Node, Set<Node>> graph) {
    Set<Set<Node>> maximalCliques = new HashSet<>();

    List<Node> degeneracyOrdering =
        graph.entrySet().stream()
            .sorted(Comparator.comparingInt(entry -> entry.getValue().size()))
            .map(Map.Entry::getKey)
            .toList();

    Set<Node> pBase = new HashSet<>(degeneracyOrdering);
    Set<Node> xBase = new HashSet<>();

    for (Node node : degeneracyOrdering) {
      Set<Node> neighbours = graph.get(node);
      Set<Node> newR = new HashSet<>(Set.of(node));
      Set<Node> newP = intersectionOf(pBase, neighbours);
      Set<Node> newX = intersectionOf(xBase, neighbours);
      bronKerbosch(newR, newP, newX, graph, maximalCliques);
      pBase.remove(node);
      xBase.add(node);
    }
    return maximalCliques;
  }

  /**
   * The Bron-Kerbosch algorithm with pivoting for finding all maximal cliques in a graph.
   *
   * @param currentNodes The set of nodes in the clique currently being built.
   * @param candidateNodes The set of candidate nodes that can be added to extend the clique.
   * @param processedNodes The set of nodes already processed and cannot be used to extend the
   *     clique.
   * @param edges The graph's adjacency list.
   * @param maximalCliques The collection to store the found maximal cliques.
   */
  private static void bronKerbosch(
      Set<Node> currentNodes,
      Set<Node> candidateNodes,
      Set<Node> processedNodes,
      Map<Node, Set<Node>> edges,
      Set<Set<Node>> maximalCliques) {

    if (candidateNodes.isEmpty() && processedNodes.isEmpty()) {
      maximalCliques.add(new HashSet<>(currentNodes));
      return;
    }

    for (Node node : new HashSet<>(candidateNodes)) {
      Set<Node> neighbours = edges.get(node);
      Set<Node> newR = unionOf(currentNodes, Set.of(node));
      Set<Node> newP = intersectionOf(candidateNodes, neighbours);
      Set<Node> newX = intersectionOf(processedNodes, neighbours);
      bronKerbosch(newR, newP, newX, edges, maximalCliques);
      candidateNodes.remove(node);
      processedNodes.add(node);
    }
  }

  private static Set<Separator> buildSeparators(Set<Clique> cliquesSet) {
    List<Clique> cliqueList = new ArrayList<>(cliquesSet);

    List<List<Clique>> sortedPairs = getSortedPairs(cliqueList);

    int separatorsNeeded = cliqueList.size() - 1;
    List<Separator> separators = new ArrayList<>();

    sortedPairs.forEach(pair -> joinBestPairs(pair, separators, separatorsNeeded));

    return new HashSet<>(separators);
  }

  private static void joinBestPairs(
      List<Clique> pair, List<Separator> separators, int separatorsNeeded) {
    if (separators.size() == separatorsNeeded) return;
    Clique cliqueA = pair.getFirst();
    Clique cliqueB = pair.getLast();

    boolean containsLoop = false;
    for (Clique clique : cliqueA.getSeparatorMap().keySet()) {
      containsLoop = checkForLoops(clique, Set.of(), new HashSet<>());
      if (containsLoop) break;
    }
    if (containsLoop) return;
    Separator s = addSeparatorForNodes(cliqueA, cliqueB);
    separators.add(s);
  }

  private static List<List<Clique>> getSortedPairs(List<Clique> cliques) {
    return getSharedNodesPerPair(cliques).entrySet().stream()
        .sorted(Map.Entry.<List<Clique>, Integer>comparingByValue().reversed())
        .map(Map.Entry::getKey)
        .toList();
  }

  private static Map<List<Clique>, Integer> getSharedNodesPerPair(List<Clique> cliques) {
    Map<List<Clique>, Integer> sharedNodesPerPair = new HashMap<>();
    for (int i = 0; i < cliques.size(); i++) {
      for (int j = i + 1; j < cliques.size(); j++) {
        Clique cliqueA = cliques.get(i);
        Clique cliqueB = cliques.get(j);
        int commonNodes =
            cliqueA.getNodes().stream()
                .filter(cliqueB.getNodes()::contains)
                .collect(Collectors.toSet())
                .size();
        sharedNodesPerPair.put(List.of(cliqueA, cliqueB), commonNodes);
      }
    }
    return sharedNodesPerPair;
  }

  private static Separator addSeparatorForNodes(Clique cliqueA, Clique cliqueB) {
    Set<Node> common = intersectionOf(cliqueA.getNodes(), cliqueB.getNodes());
    Separator separator =
        new Separator(cliqueA, cliqueB, common, TableBuilder.buildJunctionTreeTable(common));

    cliqueA.getSeparatorMap().put(cliqueB, separator);
    cliqueB.getSeparatorMap().put(cliqueA, separator);
    return separator;
  }

  private static boolean checkForLoops(
      Clique clique, Set<Clique> lastClique, Set<Clique> currentChain) {
    currentChain.add(clique);

    Set<Clique> connectedCliques =
        clique.getSeparatorMap().keySet().stream()
            .filter(c -> !lastClique.contains(c))
            .collect(Collectors.toSet());

    if (connectedCliques.isEmpty()) {
      currentChain.remove(clique);
      return false;
    }

    boolean loopFound = connectedCliques.stream().anyMatch(currentChain::contains);

    if (loopFound) {
      currentChain.remove(clique);
      return true;
    }

    for (Clique connectedClique : connectedCliques) {
      loopFound = checkForLoops(connectedClique, Set.of(clique), currentChain);
      if (loopFound) break;
    }

    currentChain.remove(clique);
    return loopFound;
  }

  private static Set<Clique> buildLeafCliques(Set<Clique> cliques) {
    return cliques.stream()
        .filter(clique -> clique.getSeparators().size() <= 1)
        .collect(Collectors.toSet());
  }

  private static Set<Node> unionOf(Set<Node> setA, Set<Node> setB) {
    return Stream.concat(setA.stream(), setB.stream())
        .collect(Collectors.toCollection(HashSet::new));
  }

  private static Set<Node> intersectionOf(Set<Node> setA, Set<Node> setB) {
    return setA.stream().filter(setB::contains).collect(Collectors.toCollection(HashSet::new));
  }
}
