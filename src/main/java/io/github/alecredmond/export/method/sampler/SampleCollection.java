package io.github.alecredmond.export.method.sampler;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A container for {@link Sample} objects generated during a Monte Carlo {@link Sampler} run.
 *
 * @see Sampler
 * @see Sample
 * @author Alec Redmond
 */
public interface SampleCollection {
  /**
   * Returns a map of each {@link Node} set as observed during the sampling run, and the specific
   * {@link NodeState} it was locked to.
   *
   * @return an unmodifiable map of the observations in this {@code SampleCollection}.
   */
  Map<Node, NodeState> getObservations();

  /**
   * Returns an array of all {@link Node}s in the {@link BayesianNetwork} this {@code
   * SampleCollection} was sampled from. The nodes are ordered in network layer order, with root
   * nodes at the start and childless nodes at the end.
   *
   * @return a the {@link Node} array from this {@code SampleCollection}.
   */
  Node[] getNodes();

  /**
   * Checks if there are no samples present in this {@code SampleCollection}. This will be {@code
   * true} if the joint probability of the observed {@link NodeState} values {@code P(Obs) = 0}.
   *
   * @return {@code true} if there are no samples, otherwise {@code false}
   */
  boolean isEmpty();

  /**
   * Returns a sum of all {@link Sample} frequencies in this {@code SampleCollection}. This will
   * equal the number of samples specified during the {@link Sampler} run.
   *
   * @return the total sample frequency in this {@code SampleCollection}
   */
  int countSamples();

  /**
   * Returns a sum of all {@link Sample} frequencies in this {@code SampleCollection}, where each
   * {@link Sample} contains the union of all given {@link NodeState} values. This will give the
   * equivalent of {@code P(X|obs) * n}, where {@code X} is the input {@link NodeState} values,
   * {@code obs} is the observations present during the {@link Sampler} run, and {@code n} is the
   * number of samples generated in the run.
   *
   * @param states the {@link NodeState} values to query.
   * @return the sample frequency in this {@code SampleCollection}
   */
  int countSamplesIncludingStates(Collection<NodeState> states);

  /**
   * Returns a sum of all {@link Sample} frequencies in this {@code SampleCollection}, where each
   * {@link Sample} contains the given {@link NodeState} value. This will give the equivalent of
   * {@code P(x|obs) * n}, where {@code x} is the input {@link NodeState} value, {@code obs} is the
   * observations present during the {@link Sampler} run, and {@code n} is the number of samples
   * generated in the run.
   *
   * @param state the {@link NodeState} value to query.
   * @return the sample frequency in this {@code SampleCollection}
   */
  int countSamplesIncludingStates(NodeState state);

  /**
   * Returns a sum of all {@link Sample} frequencies in this {@code SampleCollection}, where each
   * {@link Sample} contains the union of all given {@link NodeState} values. This will give the
   * equivalent of {@code P(X|obs) * n}, where {@code X} is the input {@link NodeState} values,
   * {@code obs} is the observations present during the {@link Sampler} run, and {@code n} is the
   * number of samples generated in the run.
   *
   * @param stateIds the ids of the {@link NodeState} values to query.
   * @return the sample frequency in this {@code SampleCollection}.
   */
  <T extends Serializable> int countSamplesIncludingStateIds(Collection<T> stateIds);

  /**
   * Returns a sum of all {@link Sample} frequencies in this {@code SampleCollection}, where each
   * {@link Sample} contains the given {@link NodeState} value. This will give the equivalent of
   * {@code P(x|obs) * n}, where {@code x} is the input {@link NodeState} value, {@code obs} is the
   * observations present during the {@link Sampler} run, and {@code n} is the number of samples
   * generated in the run.
   *
   * @param stateId the id of the {@link NodeState} value to query.
   * @return the sample frequency in this {@code SampleCollection}.
   * @throws NullPointerException if the id was not mapped to any valid {@link NodeState}.
   */
  <T extends Serializable> int countSamplesIncludingStateIds(T stateId);

  /**
   * Returns a list of all distinct {@link Sample}s in this {@code SampleCollection}. A {@link
   * Sample} contains a unique combination of {@link NodeState} values, with the frequency of their
   * occurrence in the {@link Sampler} run.
   *
   * @return an unmodifiable {@link List} of {@link Sample} objects.
   */
  List<Sample> getSamples();

  /**
   * Returns a list of all distinct {@link Sample}s in this {@code SampleCollection} which include
   * the union of all given {@link NodeState} values. A {@link Sample} contains a unique combination
   * of {@link NodeState} values, with the frequency of their occurrence in the {@link Sampler} run.
   *
   * @param states the {@link NodeState} values to query.
   * @return an unmodifiable {@link List} of {@link Sample} objects.
   */
  List<Sample> getSamplesIncludingStates(Collection<NodeState> states);

  /**
   * Returns a list of all distinct {@link Sample}s in this {@code SampleCollection} which include
   * the given {@link NodeState} value. A {@link Sample} contains a unique combination of {@link
   * NodeState} values, with the frequency of their occurrence in the {@link Sampler} run.
   *
   * @param state the {@link NodeState} value to query.
   * @return an unmodifiable {@link List} of {@link Sample} objects.
   */
  List<Sample> getSamplesIncludingStates(NodeState state);

  /**
   * Returns a list of all distinct {@link Sample}s in this {@code SampleCollection} which include
   * the union of all given {@link NodeState} values. A {@link Sample} contains a unique combination
   * of {@link NodeState} values, with the frequency of their occurrence in the {@link Sampler} run.
   *
   * @param stateIds the ids of all {@link NodeState} values to query.
   * @return an unmodifiable {@link List} of {@link Sample} objects.
   */
  <T extends Serializable> List<Sample> getSamplesIncludingStatesById(Collection<T> stateIds);

  /**
   * Returns a list of all distinct {@link Sample}s in this {@code SampleCollection} which include
   * the given {@link NodeState} value. A {@link Sample} contains a unique combination of {@link
   * NodeState} values, with the frequency of their occurrence in the {@link Sampler} run.
   *
   * @param stateId the id of the {@link NodeState} value to query.
   * @return an unmodifiable {@link List} of {@link Sample} objects.
   * @throws NullPointerException if the id was not mapped to any valid {@link NodeState}.
   */
  <T extends Serializable> List<Sample> getSamplesIncludingStatesById(T stateId);

  /**
   * Sets all {@link Node}s as displayed in each {@link Sample}. This resets the displayed nodes to
   * their initial state, and will show every {@link Node} in the sampled {@link BayesianNetwork}.
   */
  void displayAllNodes();

  /**
   * Restricts the displayed {@link Node} values in each {@link Sample} to a given collection. This
   * means {@link Sample} methods such as {@link Sample#getDisplayedStates()} will only export the
   * {@link NodeState}s contained within the displayed {@link Node}s. This can be reset either by
   * calling {@link #displayAllNodes()}, or per {@link Sample} object by calling {@link
   * Sample#displayAllNodes()}. This only affects exported states on a per-sample basis, and does
   * not affect methods such as {@link #countSamplesIncludingStates(Collection)},
   *
   * @param nodes the {@link Node} objects each {@link Sample} will be restricted to.
   */
  void setDisplayedNodes(Collection<Node> nodes);

  /**
   * Restricts the displayed {@link Node} value in each {@link Sample} to a single instance. This
   * means {@link Sample} methods such as {@link Sample#getDisplayedStates()} will only export the
   * {@link NodeState} contained within the displayed {@link Node}. This can be reset either by
   * calling {@link #displayAllNodes()}, or per {@link Sample} object by calling {@link
   * Sample#displayAllNodes()}. This only affects exported states on a per-sample basis, and does
   * not affect methods such as {@link #countSamplesIncludingStates(Collection)},
   *
   * @param node the {@link Node} each {@link Sample} will be restricted to.
   */
  void setDisplayedNodes(Node node);

  /**
   * Restricts the displayed {@link Node} values in each {@link Sample} to a given collection. This
   * means {@link Sample} methods such as {@link Sample#getDisplayedStates()} will only export the
   * {@link NodeState}s contained within the displayed {@link Node}s. This can be reset either by
   * calling {@link #displayAllNodes()}, or per {@link Sample} object by calling {@link
   * Sample#displayAllNodes()}. This only affects exported states on a per-sample basis, and does
   * not affect methods such as {@link #countSamplesIncludingStates(Collection)},
   *
   * @param nodeIds the ids of all {@link Node} objects each {@link Sample} will be restricted to.
   */
  <T extends Serializable> void setDisplayedNodesById(Collection<T> nodeIds);

  /**
   * Restricts the displayed {@link Node} value in each {@link Sample} to a single instance. This
   * means {@link Sample} methods such as {@link Sample#getDisplayedStates()} will only export the
   * {@link NodeState} contained within the displayed {@link Node}. This can be reset either by
   * calling {@link #displayAllNodes()}, or per {@link Sample} object by calling {@link
   * Sample#displayAllNodes()}. This only affects exported states on a per-sample basis, and does
   * not affect methods such as {@link #countSamplesIncludingStates(Collection)},
   *
   * @param nodeId the id of the {@link Node} each {@link Sample} will be restricted to.
   * @throws NullPointerException if the id was not mapped to any valid {@link Node}.
   */
  <T extends Serializable> void setDisplayedNodesById(T nodeId);
}
