package io.github.alecredmond.internal.method.inference.junctiontree.clique;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.internal.application.inference.junctiontree.Clique;
import io.github.alecredmond.internal.application.inference.junctiontree.JunctionTreeData;
import io.github.alecredmond.internal.application.inference.junctiontree.Separator;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;
import java.util.*;

public class CliqueJoiner {
  private final JunctionTreeData jtd;
  private final SeparatorFactory separatorFactory;
  private final Set<Separator> separators;

  public CliqueJoiner(JunctionTreeData jtd) {
    this.jtd = jtd;
    this.separatorFactory = new SeparatorFactory(jtd);
    this.separators = new HashSet<>();
  }

  public void joinCliques() {
    if (jtd.getCliques().length == 0) {
      return;
    }
    Set<Clique> cliques = new HashSet<>();
    Set<Clique> joined = new HashSet<>();
    Clique smallest = initializeAndPopSmallest(cliques, joined);
    recursivelyJoinCliques(smallest, cliques, joined);
    jtd.setSeparators(getSeparatorArray());
  }

  private Separator[] getSeparatorArray() {
    return Arrays.stream(jtd.getCliques())
        .flatMap(c -> c.getSeparatorMap().values().stream())
        .distinct()
        .toArray(Separator[]::new);
  }

  private void recursivelyJoinCliques(Clique current, Set<Clique> available, Set<Clique> joined) {
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
          Separator separator = separatorFactory.buildSeparator(current, nextClique);
          separators.add(separator);
          current.getSeparatorMap().put(nextClique, separator);
          nextClique.getSeparatorMap().put(current, separator);
          available.remove(nextClique);
          joined.add(nextClique);
          recursivelyJoinCliques(nextClique, available, joined);
        });
  }

  private List<Clique> orderAvailableCandidates(Clique current, Set<Clique> available) {
    return available.stream()
        .map(clique -> commonNodeCount(clique, current))
        .filter(connectionSize -> connectionSize.getValue() > 0)
        .sorted(Map.Entry.<Clique, Integer>comparingByValue().reversed())
        .map(Map.Entry::getKey)
        .toList();
  }

  private Clique initializeAndPopSmallest(Set<Clique> cliques, Set<Clique> joined) {
    cliques.addAll(Arrays.asList(jtd.getCliques()));
    Clique smallest =
        cliques.stream().min(Comparator.comparing(c -> c.getNodes().size())).orElseThrow();
    cliques.remove(smallest);
    joined.add(smallest);
    return smallest;
  }

  private Map.Entry<Clique, Integer> commonNodeCount(Clique clique, Clique current) {
    Set<Node> cliqueNodes = TableUtils.getCommonNodes(clique.getTable(), current.getTable());
    return Map.entry(clique, cliqueNodes.size());
  }
}
