package io.github.alecredmond.export.method.sampler;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * A container for a unique sample generated during a {@link Sampler} run. This represents a
 * particular combination of {@link NodeState} values - one per {@link Node} in a {@link
 * BayesianNetwork} - and the frequency that combination occurred during sampling. Each {@code
 * Sample} belongs to a specific {@link SampleCollection}, which contains the results of a sampling
 * run.
 *
 * @see SampleCollection
 * @see Sampler
 * @author Alec Redmond
 */
public interface Sample {
  /**
   * A static helper method which sums the counts of all distinct {@link Sample}s in the collection.
   *
   * @param samples the samples to count
   * @return the summed counts of all distinct {@code Sample} objects
   */
  static int countAll(Collection<Sample> samples) {
    return samples.stream().distinct().mapToInt(Sample::count).sum();
  }

  /**
   * Returns the number of times this {@code Sample} was picked during the sampling process.
   *
   * @return the number of occurrences of this {@code Sample}
   */
  int count();

  /**
   * Returns an array of all {@link NodeState} values that this {@code Sample} represents. This will
   * constitute a {@link NodeState} from every {@link Node} in its associated {@link
   * BayesianNetwork}, and is ordered by its network layer; Root nodes will be at the start,
   * childless nodes at the end.
   *
   * @return an array of all {@link NodeState} values in this {@code Sample}
   */
  NodeState[] getAllStates();

  /**
   * Returns an array of the {@link NodeState} values that this {@code Sample} represents,
   * restricted to the current displayed {@link Node}s.
   *
   * @return an array of those {@link NodeState} values associated to a visible {@link Node} in this
   *     {@code Sample}
   */
  NodeState[] getDisplayedStates();

  /**
   * Returns a collection of the {@link NodeState} values that this {@code Sample} represents,
   * restricted to the current displayed {@link Node}s.
   *
   * @param supplier the supplier for the required collection.
   * @param <S> the supplier class extending {@link Collection}, for example {@link ArrayList}.
   * @param <T> the super type of the supplier class, for example {@link List}.
   * @return collection of {@link NodeState} values where each {@link Node} in this {@code Sample}
   *     is marked displayed.
   */
  <T extends Collection<NodeState>, S extends T> T getDisplayedStates(Supplier<S> supplier);

  /**
   * Resets the display restrictions, returning to the default state where every {@link NodeState}
   * associated with a given {@link NodeState} in the {@link BayesianNetwork} is displayed.
   */
  void displayAllNodes();

  /**
   * Restricts the displayed {@link NodeState} values in {@link #getDisplayedStates(Supplier)} and
   * {@link #getDisplayedStates()} to those associated with the given {@link Node} values.
   *
   * @param nodes the {@link Node}s in the {@link BayesianNetwork} to be displayed in this {@code
   *     Sample}.
   */
  void setDisplayedNodes(Collection<Node> nodes);
}
