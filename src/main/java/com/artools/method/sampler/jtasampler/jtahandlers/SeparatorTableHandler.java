package com.artools.method.sampler.jtasampler.jtahandlers;

import com.artools.application.probabilitytables.JunctionTreeTable;
import com.artools.application.sampler.Clique;
import com.artools.application.sampler.Separator;
import java.util.*;

public class SeparatorTableHandler extends JunctionTableHandler {
  private final Separator separator;
  private final JunctionTreeTable tableCliqueA;
  private final JunctionTableHandler cliqueIndexerA;
  private final List<Set<Integer>> sumFinderA;
  private final JunctionTreeTable tableCliqueB;
  private final JunctionTableHandler cliqueIndexerB;
  private final List<Set<Integer>> sumFinderB;

  public SeparatorTableHandler(Separator separator) {
    super(separator.getTable());
    this.separator = separator;
    this.tableCliqueA = separator.getCliqueA().getTable();
    this.cliqueIndexerA = new JunctionTableHandler(tableCliqueA);
    this.tableCliqueB = separator.getCliqueB().getTable();
    this.cliqueIndexerB = new JunctionTableHandler(tableCliqueB);
    this.sumFinderA = buildSumToIndex(cliqueIndexerA);
    this.sumFinderB = buildSumToIndex(cliqueIndexerB);
  }

  private List<Set<Integer>> buildSumToIndex(JunctionTableHandler cliqueIndexer) {
    List<Set<Integer>> sumFinder = new ArrayList<>();
    table.getIndexMap().entrySet().stream()
        .sorted(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .forEach(key -> sumFinder.add(cliqueIndexer.getIndexes(key)));
    return sumFinder;
  }

  public void passMessageFrom(Clique startingClique) {
    JunctionTreeTable cliqueTable = startingClique.getTable();
    if (cliqueTable.equals(tableCliqueA)) {
      passMessageFrom(cliqueIndexerA, sumFinderA, cliqueIndexerB, sumFinderB);
    } else if (cliqueTable.equals(tableCliqueB)) {
      passMessageFrom(cliqueIndexerB, sumFinderB, cliqueIndexerA, sumFinderA);
    } else
      throw new IllegalArgumentException(
          String.format(
              "unexpected table %s in separator indexer for %s",
              cliqueTable.getTableID(), separator.getTable().getTableID()));
  }

  private void passMessageFrom(
      JunctionTableHandler indexerFrom,
      List<Set<Integer>> sumFinderFrom,
      JunctionTableHandler indexerTo,
      List<Set<Integer>> sumFinderTo) {
    double[] probabilities = getProbabilities();
    for (int i = 0; i < probabilities.length; i++) {
      double sumFrom = indexerFrom.sumFromTableIndexes(sumFinderFrom.get(i));
      probabilities[i] = sumFrom;
      double sumTo = indexerTo.sumFromTableIndexes(sumFinderTo.get(i));

      if (Math.abs(sumFrom - sumTo) <= 1e-9) continue;

      double ratio = getRatio(sumFrom, sumTo);
      indexerTo.adjustByRatio(sumFinderTo.get(i), ratio);
    }
  }
}
