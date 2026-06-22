package io.github.alecredmond.internal.method.inference.junctiontree.clique;

import io.github.alecredmond.internal.application.inference.junctiontree.Clique;
import io.github.alecredmond.internal.application.inference.junctiontree.JunctionTreeData;
import io.github.alecredmond.internal.application.inference.junctiontree.Separator;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;
import java.util.*;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CliqueJoiner {

  public void joinCliques(JunctionTreeData jtd) {
    SeparatorFactory separatorFactory = new SeparatorFactory(jtd);
    Clique[] cliques = jtd.getCliques();

    if (cliques.length <= 1) {
      jtd.setSeparators(new Separator[0]);
      return;
    }

    List<CliqueEdge> candidates = orderCandidateEdges(cliques);
    Map<Clique, Clique> branchRoots = new HashMap<>();
    for (Clique clique : cliques) branchRoots.put(clique, clique);

    List<Separator> finalSeparators = new ArrayList<>();
    int edgesAdded = 0;

    for (CliqueEdge edge : candidates) {
      if (edgesAdded == cliques.length - 1) break;

      Clique cliqueA = edge.cliqueA;
      Clique cliqueB = edge.cliqueB;

      Clique rootOfA = findRoots(cliqueA, branchRoots);
      Clique rootOfB = findRoots(cliqueB, branchRoots);

      if (rootOfB.equals(rootOfA)) continue;

      branchRoots.put(cliqueA, cliqueB);
      finalSeparators.add(separatorFactory.buildSeparator(cliqueA, cliqueB));
      edgesAdded++;
    }

    jtd.setSeparators(finalSeparators.toArray(Separator[]::new));
  }

  private Clique findRoots(Clique clique, Map<Clique, Clique> branchRoots) {
    if (branchRoots.get(clique).equals(clique)) return clique;
    Clique root = findRoots(branchRoots.get(clique), branchRoots);
    branchRoots.put(clique, root);
    return branchRoots.get(clique);
  }

  private List<CliqueEdge> orderCandidateEdges(Clique[] cliques) {
    List<CliqueEdge> candidates = new ArrayList<>();
    for (int i = 0; i < cliques.length; i++) {
      for (int j = i + 1; j < cliques.length; j++) {
        int weight = getCommonNodeCount(cliques[i], cliques[j]);
        if (weight == 0) continue;
        candidates.add(new CliqueEdge(cliques[i], cliques[j], weight));
      }
    }
    candidates.sort(Comparator.comparingInt(CliqueEdge::weight).reversed());
    return candidates;
  }

  private int getCommonNodeCount(Clique cliqueA, Clique cliqueB) {
    return TableUtils.getCommonNodes(cliqueA.getTable(), cliqueB.getTable()).size();
  }

  private record CliqueEdge(Clique cliqueA, Clique cliqueB, int weight) {}
}
