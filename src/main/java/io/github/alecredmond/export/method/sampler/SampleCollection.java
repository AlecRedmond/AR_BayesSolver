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
   * @return the sample frequency in this {@code SampleCollection}
   */
  <T extends Serializable> int countSamplesIncludingStateIds(Collection<T> stateIds);

  <T extends Serializable> int countSamplesIncludingStateIds(T stateId);

  List<Sample> getSamples();

  List<Sample> getSamplesIncludingStates(Collection<NodeState> states);

  List<Sample> getSamplesIncludingStates(NodeState state);

  <T extends Serializable> List<Sample> getSamplesIncludingStatesById(Collection<T> stateIds);

  <T extends Serializable> List<Sample> getSamplesIncludingStatesById(T stateId);

  void displayAllNodes();

  void setDisplayedNodes(Collection<Node> nodes);

  void setDisplayedNodes(Node node);

  <T extends Serializable> void setDisplayedNodesById(Collection<T> nodeIds);

  <T extends Serializable> void setDisplayedNodesById(T nodeId);

  String toString();
}
