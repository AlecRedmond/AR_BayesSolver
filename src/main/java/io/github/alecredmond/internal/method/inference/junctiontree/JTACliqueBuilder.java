package io.github.alecredmond.internal.method.inference.junctiontree;

import static io.github.alecredmond.internal.method.utils.AppProperty.INFERENCE_USE_JTA_INFERENCE;
import static io.github.alecredmond.internal.method.utils.AppProperty.INFERENCE_USE_JTA_SOLVER;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.internal.application.inference.junctiontree.Clique;
import io.github.alecredmond.internal.application.inference.junctiontree.JunctionTreeData;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.probabilitytables.TableBuilder;
import io.github.alecredmond.internal.method.utils.AppProperty;
import io.github.alecredmond.internal.method.utils.PropertiesLoader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class JTACliqueBuilder {

  private JTACliqueBuilder() {}

  static void buildCliques(JunctionTreeData jtd) {
    AppProperty property =
        jtd.isSolverConfig() ? INFERENCE_USE_JTA_SOLVER : INFERENCE_USE_JTA_INFERENCE;
    boolean useJta = new PropertiesLoader().loadBoolean(property);
    if (useJta) {
      buildJtaCliques(jtd);
    } else {
      buildIPFPClique(jtd);
    }
  }

  static void buildIPFPClique(JunctionTreeData jtd) {
    BayesianNetworkData bnd = jtd.getBayesianNetworkData();
    Clique[] cliques = new Clique[1];
    Set<Node> linkedNodes = new LinkedHashSet<>(bnd.getNodes());
    cliques[0] = new Clique(linkedNodes, TableBuilder.buildJunctionTreeTable(linkedNodes, bnd));
    jtd.setCliques(cliques);
  }

  private static void buildJtaCliques(JunctionTreeData jtd) {
    BayesianNetworkData bnd = jtd.getBayesianNetworkData();
    Map<Node, Set<Node>> edgeGraph = initializeGraph(bnd);
    moralizeGraph(edgeGraph, bnd);
    triangulateGraph(edgeGraph, bnd);
    jtd.setCliques(buildCliqueArray(findMaximalCliques(edgeGraph), bnd));
  }

  private static Clique[] buildCliqueArray(Set<Set<Node>> maximalCliques, BayesianNetworkData bnd) {
    return maximalCliques.stream()
        .map(nodes -> new Clique(nodes, TableBuilder.buildJunctionTreeTable(nodes, bnd)))
        .toArray(Clique[]::new);
  }

  static Set<Node> intersectionOf(Set<Node> setA, Set<Node> setB) {
    return NodeUtils.getOverlap(setA, setB);
  }

  private static Map<Node, Set<Node>> initializeGraph(BayesianNetworkData data) {
    Map<Node, Set<Node>> edges = new HashMap<>();
    for (Node node : data.getNodes()) {
      Set<Node> connected = new HashSet<>(node.getParents());
      connected.addAll(node.getChildren());
      addConstraintsIfUnsolved(data, node, connected);
      edges.put(node, connected);
    }
    return edges;
  }

  private static void addConstraintsIfUnsolved(
      BayesianNetworkData data, Node node, Set<Node> connected) {
    if (data.isSolved()) return;
    data.getConstraints().stream()
        .map(ProbabilityConstraint::getAllNodes)
        .filter(allNodes -> allNodes.contains(node))
        .flatMap(Collection::stream)
        .distinct()
        .filter(n -> !node.equals(n))
        .forEach(connected::add);
  }

  private static void moralizeGraph(Map<Node, Set<Node>> edges, BayesianNetworkData data) {
    for (Node node : data.getNodes()) {
      List<Node> parents = node.getParents();
      for (int i = 0; i < parents.size(); i++) {
        for (int j = i + 1; j < parents.size(); j++) {
          Node parent1 = parents.get(i);
          Node parent2 = parents.get(j);
          edges.get(parent1).add(parent2);
          edges.get(parent2).add(parent1);
        }
      }
    }
  }

  private static void triangulateGraph(Map<Node, Set<Node>> edges, BayesianNetworkData data) {
    Map<Node, Set<Node>> graph = new HashMap<>();
    edges.keySet().forEach(node -> graph.put(node, new HashSet<>(edges.get(node))));

    for (Node toEliminate : data.getNodes()) {
      List<Node> neighbours = graph.get(toEliminate).stream().toList();
      if (neighbours.size() < 2) continue;
      for (int i = 0; i < neighbours.size(); i++) {
        for (int j = i + 1; j < neighbours.size(); j++) {
          Node n1 = neighbours.get(i);
          Node n2 = neighbours.get(j);
          edges.get(n1).add(n2);
          edges.get(n2).add(n1);
        }
      }
      neighbours.forEach(neighbour -> graph.get(neighbour).remove(toEliminate));
    }
  }

  private static Set<Set<Node>> findMaximalCliques(Map<Node, Set<Node>> edges) {
    Set<Set<Node>> maximalCliques = new HashSet<>();

    List<Node> degeneracyOrdering =
        edges.entrySet().stream()
            .sorted(Comparator.comparingInt(entry -> entry.getValue().size()))
            .map(Map.Entry::getKey)
            .toList();

    Set<Node> pBase = new HashSet<>(degeneracyOrdering);
    Set<Node> xBase = new HashSet<>();

    for (Node node : degeneracyOrdering) {
      Set<Node> neighbours = edges.get(node);
      Set<Node> newR = new HashSet<>(Set.of(node));
      Set<Node> newP = intersectionOf(pBase, neighbours);
      Set<Node> newX = intersectionOf(xBase, neighbours);
      bronKerbosch(newR, newP, newX, edges, maximalCliques);
      pBase.remove(node);
      xBase.add(node);
    }
    return maximalCliques;
  }

  /**
   * I have used the Bron-Kerbosch algorithm with pivoting for finding all maximal cliques in a
   * graph. Further explanation of this algorithm can be found <a
   * href="https://en.wikipedia.org/wiki/Bron%E2%80%93Kerbosch_algorithm#With_pivoting">here.</a>
   *
   * @param currentNodes (R) The set of nodes in the clique currently being built.
   * @param candidateNodes (P) The set of candidate nodes that can be added to extend the clique.
   * @param processedNodes (X) The set of nodes already processed and cannot be used to extend the
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

    Node pivot = unionOf(candidateNodes, processedNodes).stream().findAny().orElseThrow();
    Set<Node> vertexSet = new HashSet<>(candidateNodes);
    vertexSet.removeAll(edges.get(pivot));

    for (Node node : vertexSet) {
      Set<Node> neighbours = edges.get(node);
      Set<Node> newR = unionOf(currentNodes, Set.of(node));
      Set<Node> newP = intersectionOf(candidateNodes, neighbours);
      Set<Node> newX = intersectionOf(processedNodes, neighbours);
      bronKerbosch(newR, newP, newX, edges, maximalCliques);
      candidateNodes.remove(node);
      processedNodes.add(node);
    }
  }

  private static Set<Node> unionOf(Set<Node> setA, Set<Node> setB) {
    return Stream.concat(setA.stream(), setB.stream())
        .collect(Collectors.toCollection(HashSet::new));
  }
}
