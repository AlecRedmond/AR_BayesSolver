package com.artools.method.indexer;

import com.artools.application.junctiontree.Clique;
import com.artools.application.junctiontree.Separator;
import com.artools.application.node.NodeState;
import com.artools.application.probabilitytables.JunctionTreeTable;
import java.util.*;

public class SeparatorTableIndexer extends TableIndexer {
  private final Separator separator;
  private final JunctionTreeTable tableCliqueA;
  private final TableIndexer cliqueIndexerA;
  private final List<Set<Integer>> sumFinderA;
  private final JunctionTreeTable tableCliqueB;
  private final TableIndexer cliqueIndexerB;
  private final List<Set<Integer>> sumFinderB;

  public SeparatorTableIndexer(Separator separator) {
    super(separator.getTable());
    this.separator = separator;
    this.tableCliqueA = separator.getCliqueA().getTable();
    this.cliqueIndexerA = new TableIndexer(tableCliqueA);
    this.tableCliqueB = separator.getCliqueB().getTable();
    this.cliqueIndexerB = new TableIndexer(tableCliqueB);
    this.sumFinderA = buildSumToIndex(cliqueIndexerA);
    this.sumFinderB = buildSumToIndex(cliqueIndexerB);
  }

  private List<Set<Integer>> buildSumToIndex(TableIndexer cliqueIndexer) {
    List<Set<Integer>> intSetList = new ArrayList<>(table.getProbabilities().length);
    for (Set<NodeState> key : table.getKeySet()) {
      int index = table.getIndexMap().get(key);
      Set<Integer> cliqueIndexerSet = cliqueIndexer.getIndexes(key);
      intSetList.add(index, cliqueIndexerSet);
    }
    return intSetList;
  }

  public void passMessage(Clique startingClique) {
    JunctionTreeTable cliqueTable = startingClique.getTable();
    if (cliqueTable.equals(tableCliqueA))
      passMessage(cliqueIndexerA, sumFinderA, cliqueIndexerB, sumFinderB);
    if (cliqueTable.equals(tableCliqueB))
      passMessage(cliqueIndexerB, sumFinderB, cliqueIndexerA, sumFinderA);
    else
      throw new IllegalArgumentException(
          String.format(
              "unexpected table %s in separator indexer for %s",
              cliqueTable.getTableID(), separator.getTable().getTableID()));
  }

  private void passMessage(
      TableIndexer indexerFrom,
      List<Set<Integer>> sumFinderFrom,
      TableIndexer indexerTo,
      List<Set<Integer>> sumFinderTo) {
    double[] probabilities = getCorrectProbabilities();
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
