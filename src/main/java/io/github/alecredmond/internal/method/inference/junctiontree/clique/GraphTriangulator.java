package io.github.alecredmond.internal.method.inference.junctiontree.clique;

import io.github.alecredmond.export.application.node.Node;
import java.util.*;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class GraphTriangulator {

  /**
   * This uses the general minimal triangulation algorithm, described by D.L.P Minh & T.T.T Dong in
   * 'Bayesian networks: The minimal triangulations of a graph.' (2019)
   */
  public void triangulate(Map<Node, Set<Node>> edges) {
    Map<Node, Set<Node>> graph = new HashMap<>();
    edges.keySet().forEach(n -> graph.put(n, new HashSet<>(edges.get(n))));

    Map<Node, Integer> labels = new HashMap<>();
    graph.keySet().forEach(n -> labels.put(n, 0));

    for (int i = 0; i < graph.size(); i++) {
      Node mu = selectConfluenceVertex(graph, labels);
      if (labels.get(mu) <= 0) {
        addChordsToSubNeighbourhood(edges, mu, graph, labels);
      }
      Set<Node> muNeighbours = new HashSet<>(graph.get(mu));
      muNeighbours.forEach(n -> labels.merge(n, 1, Integer::sum));
      muNeighbours.forEach(n -> graph.get(n).remove(mu));
      graph.remove(mu);
      labels.remove(mu);
    }
  }

  private Node selectConfluenceVertex(Map<Node, Set<Node>> graph, Map<Node, Integer> labels) {
    return labels.entrySet().stream()
        .filter(e -> graph.containsKey(e.getKey()))
        .max(Comparator.comparingInt(Map.Entry::getValue))
        .map(Map.Entry::getKey)
        .orElseThrow();
  }

  private void addChordsToSubNeighbourhood(
      Map<Node, Set<Node>> edges, Node mu, Map<Node, Set<Node>> graph, Map<Node, Integer> labels) {
    findSubNeighbourhood(mu, graph, labels).stream()
        .filter(theta -> !theta.equals(mu))
        .filter(theta -> !graph.get(theta).contains(mu))
        .filter(theta -> requiresFillIn(theta, mu, graph, labels))
        .forEach(
            theta -> {
              edges.get(theta).add(mu);
              edges.get(mu).add(theta);
              graph.get(mu).add(theta);
              graph.get(theta).add(mu);
            });
  }

  private Set<Node> findSubNeighbourhood(
      Node mu, Map<Node, Set<Node>> graph, Map<Node, Integer> labels) {
    Set<Node> subNeighbourhood = new HashSet<>();
    Queue<Node> queue = new LinkedList<>();
    Set<Node> visited = new HashSet<>();

    queue.add(mu);
    visited.add(mu);
    if (labels.get(mu) > 0) subNeighbourhood.add(mu);

    while (!queue.isEmpty()) {
      Node current = queue.poll();
      graph.get(current).stream()
          .filter(visited::add)
          .forEach(
              neighbour -> {
                queue.add(neighbour);
                if (labels.get(neighbour) > 0) subNeighbourhood.add(neighbour);
              });
    }
    return subNeighbourhood;
  }

  private boolean requiresFillIn(
      Node theta, Node mu, Map<Node, Set<Node>> graph, Map<Node, Integer> labels) {
    int thetaLabel = labels.get(theta);
    Set<Node> muNeighbours = graph.get(mu);

    Queue<Node> queue = new LinkedList<>();
    Set<Node> visited = new HashSet<>();
    queue.add(theta);
    visited.add(theta);

    while (!queue.isEmpty()) {
      Node current = queue.poll();
      Set<Node> lowerLabelledNeighbours =
          graph.get(current).stream()
              .filter(n -> !n.equals(mu))
              .filter(visited::add)
              .filter(n -> labels.getOrDefault(n, 0) < thetaLabel)
              .collect(Collectors.toSet());
      for (Node neighbour : lowerLabelledNeighbours) {
        if (muNeighbours.contains(neighbour)) return true;
        queue.add(neighbour);
      }
    }
    return false;
  }
}
