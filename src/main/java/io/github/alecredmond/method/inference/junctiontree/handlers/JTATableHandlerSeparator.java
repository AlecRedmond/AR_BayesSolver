package io.github.alecredmond.method.inference.junctiontree.handlers;

import io.github.alecredmond.application.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.application.inference.junctiontree.Clique;
import io.github.alecredmond.application.inference.junctiontree.Separator;
import java.util.*;

/**
 * Extends {@link JTATableHandler} to specifically manage the transfer of messages
 * (probabilities) between two {@link Clique}s connected by a {@link Separator} in the Junction Tree
 * Algorithm. It is used in the Collect and Distribute steps of the algorithm by passing messages
 * between the connected cliques and updating their probabilities.
 */
public class JTATableHandlerSeparator extends JTATableHandler {
  private final Separator separator;
  private final JunctionTreeTable tableCliqueA;
  private final JTATableHandler cliqueIndexerA;
  private final List<Set<Integer>> sumFinderA;
  private final JunctionTreeTable tableCliqueB;
  private final JTATableHandler cliqueIndexerB;
  private final List<Set<Integer>> sumFinderB;

  /**
   * Constructs a {@code JTATableHandlerSeparator} for a given {@link Separator}. Initializes handlers
   * for the two connected cliques and prepares the index lists for summing probabilities during
   * message passing.
   *
   * @param separator The {@link Separator} object containing the table and connected cliques.
   */
  public JTATableHandlerSeparator(Separator separator) {
    super(separator.getTable());
    this.separator = separator;
    this.tableCliqueA = separator.getCliqueA().getTable();
    this.cliqueIndexerA = new JTATableHandler(tableCliqueA);
    this.tableCliqueB = separator.getCliqueB().getTable();
    this.cliqueIndexerB = new JTATableHandler(tableCliqueB);
    this.sumFinderA = buildSumToIndex(cliqueIndexerA);
    this.sumFinderB = buildSumToIndex(cliqueIndexerB);
  }

  /**
   * Builds a list of index sets for a clique handler, ordered by the separator table's key set.
   * Each set in the list contains the indexes in the clique's probability array that correspond to
   * one key configuration in the separator's table.
   *
   * @param cliqueIndexer The handler for the clique whose indexes are being built.
   * @return A list of sets of integers for summing probabilities in the clique table.
   */
  private List<Set<Integer>> buildSumToIndex(JTATableHandler cliqueIndexer) {
    List<Set<Integer>> sumFinder = new ArrayList<>();
    table.getIndexMap().entrySet().stream()
        .sorted(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .forEach(key -> sumFinder.add(cliqueIndexer.getIndexes(key)));
    return sumFinder;
  }

  /**
   * Passes a message (updates probabilities) from the {@code startingClique} to the other clique
   * connected by this separator.
   *
   * @param startingClique The {@link Clique} from which the message originates.
   * @throws IllegalArgumentException if the provided clique table is not one of the two connected
   *     cliques.
   */
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

  /**
   * The core logic for message passing: <br>
   * 1. Updates the separator's table probabilities based on the message from the originating
   * clique. <br>
   * 2. Calculates the required probability ratio by comparing the sum from the originating clique with
   * the sum from the receiving clique. <br>
   * 3. Adjusts the probabilities in the receiving clique's table by this ratio.
   *
   * @param indexerFrom The handler for the originating clique.
   * @param sumFinderFrom The list of index sets for the originating clique.
   * @param indexerTo The handler for the receiving clique.
   * @param sumFinderTo The list of index sets for the receiving clique.
   */
  private void passMessageFrom(
      JTATableHandler indexerFrom,
      List<Set<Integer>> sumFinderFrom,
      JTATableHandler indexerTo,
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
