package io.github.alecredmond.internal.method.inference.junctiontree.separators;

import io.github.alecredmond.internal.application.inference.junctiontree.Clique;
import io.github.alecredmond.internal.application.inference.junctiontree.JunctionTreeData;
import io.github.alecredmond.internal.application.inference.junctiontree.Separator;
import io.github.alecredmond.export.application.node.Node;
import java.util.*;

public class CliqueJoiner {

  private CliqueJoiner() {}

  public static void join(JunctionTreeData jtd) {
    Set<Clique> cliques = new HashSet<>();
    Set<Clique> joined = new HashSet<>();
    Clique smallest = initializeAndReturnSmallest(jtd, cliques, joined);

    recursivelyJoinCliques(smallest, cliques, joined, jtd);
  }

  private static void recursivelyJoinCliques(
      Clique current, Set<Clique> available, Set<Clique> joined, JunctionTreeData jtd) {

    List<Clique> orderedCandidates = orderAvailableCandidates(current, available);

    if (orderedCandidates.isEmpty()) {
      available.remove(current);
      return;
    }

    orderedCandidates.forEach(
        nextClique -> {
          if (joined.contains(nextClique)) {
            return;
          }
          Separator separator = new SeparatorFactory().buildSeparator(current, nextClique, jtd);
          current.getSeparatorMap().put(nextClique, separator);
          nextClique.getSeparatorMap().put(current, separator);
          available.remove(nextClique);
          joined.add(nextClique);
          recursivelyJoinCliques(nextClique, available, joined, jtd);
        });
  }

  private static List<Clique> orderAvailableCandidates(Clique current, Set<Clique> available) {
    return available.stream()
        .map(clique -> commonNodes(clique, current))
        .filter(connectionSize -> connectionSize.getValue() > 0)
        .sorted(Map.Entry.<Clique, Integer>comparingByValue().reversed())
        .map(Map.Entry::getKey)
        .toList();
  }

  private static Clique initializeAndReturnSmallest(
      JunctionTreeData jtd, Set<Clique> cliques, Set<Clique> joined) {
    cliques.addAll(Arrays.asList(jtd.getCliques()));
    Clique smallest =
        cliques.stream().min(Comparator.comparing(c -> c.getNodes().size())).orElseThrow();
    cliques.remove(smallest);
    joined.add(smallest);
    return smallest;
  }

  private static Map.Entry<Clique, Integer> commonNodes(Clique clique, Clique current) {
    Set<Node> cliqueNodes = new HashSet<>(clique.getNodes());
    cliqueNodes.retainAll(current.getNodes());
    return Map.entry(clique, cliqueNodes.size());
  }
}
