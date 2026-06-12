package io.github.alecredmond.export.method.sampler;

import io.github.alecredmond.exceptions.BayesNetIDException;
import io.github.alecredmond.exceptions.NodeStateConflictException;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.inference.InferenceEngine;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.internal.method.sampler.LikelihoodWeightingSampler;
import java.io.Serializable;
import java.util.Collection;
import java.util.Random;

/**
 * A Monte Carlo sampler for a {@link BayesianNetwork} which generates random samples from the
 * network's CPTs. A {@code Sampler} run returns a {@link SampleCollection} of {@link Sample}
 * objects. Each {@link Sample} contains a unique combination of {@link NodeState} values and the
 * frequency of their occurrence.
 *
 * <p>The only sampling method currently available is <i>Likelihood Weighting Sampling (LWS)</i>.
 * LWS performs a random walk down each CPT in the {@link BayesianNetwork}, selecting each new
 * {@link NodeState} according to its weighted probability. When the algorithm reaches a {@link
 * Node} constrained to a specific {@link NodeState}, the weight of the sample is multiplied by the
 * probability of that state occurring conditional on the sample's current configuration. At the end
 * of the run, the weighted samples are normalized and the proportionally correct frequencies
 * assigned.
 *
 * <p>Monte Carlo sampling is a form of indirect inference. It relies on Java's built-in {@link
 * Random} functionality and does not give exact or deterministic outcomes. The margin of error will
 * be proportional to {@code 1 / sqrt(n)}, where {@code n} is the number of samples generated.
 *
 * @see InferenceEngine
 * @see BayesianNetwork
 * @author Alec Redmond
 */
public interface Sampler {

  /**
   * Creates a new {@code Sampler} from the given {@link BayesianNetwork}.
   *
   * @param network the {@link BayesianNetwork} to sample.
   * @return a new {@code Sampler}.
   */
  static Sampler create(BayesianNetwork network) {
    return new LikelihoodWeightingSampler(network);
  }

  /**
   * Runs the sampler for the given number of cycles and returns a {@link SampleCollection}
   * containing the results of the run. This samples the network without any observations, and will
   * produce a sample set matching the prior distribution of the {@link BayesianNetwork} as the
   * number of cycles trends towards infinity.
   *
   * @param numberOfSamples the number of cycles in the solver run, and the number of samples in the
   *     final {@link SampleCollection}.
   * @return a new {@link SampleCollection}.
   */
  SampleCollection generateSamples(int numberOfSamples);

  /**
   * Runs the sampler for the given number of cycles and returns a {@link SampleCollection}
   * containing the results of the run. This samples the network using the current observations in
   * the {@link InferenceEngine}, and will produce a sample set matching the posterior distribution
   * of the {@link BayesianNetwork} conditional on these observed states as the number of cycles
   * trends towards infinity.
   *
   * <p>This is the equivalent of running {@link #generateSamples(Collection, int)} with <br>
   * {@code inferenceEngine.getCurrentObservations().values()} as the {@link NodeState} collection.
   *
   * @param engine an {@link InferenceEngine} whose observations are to be used as conditions.
   * @param numberOfSamples the number of cycles in the solver run, and the number of samples in the
   *     final {@link SampleCollection}.
   * @return a new {@link SampleCollection}.
   */
  SampleCollection generateSamples(InferenceEngine engine, int numberOfSamples);

  /**
   * Runs the sampler for the given number of cycles and returns a {@link SampleCollection}
   * containing the results of the run. This samples the network with a collection of observed
   * {@link NodeState} values always present. This will produce a sample set matching the posterior
   * distribution of the {@link BayesianNetwork} conditional on these observed states as the number
   * of cycles trends towards infinity.
   *
   * @param observedStates a collection of {@link NodeState} values which will always be present in
   *     every {@link Sample}.
   * @param numberOfSamples the number of cycles in the solver run, and the number of samples in the
   *     final {@link SampleCollection}.
   * @return a new {@link SampleCollection}.
   * @throws NodeStateConflictException if multiple {@link NodeState}s in the input share the same
   *     {@link Node}.
   */
  SampleCollection generateSamples(Collection<NodeState> observedStates, int numberOfSamples);

  /**
   * Runs the sampler for the given number of cycles and returns a {@link SampleCollection}
   * containing the results of the run. This samples the network with a collection of observed
   * {@link NodeState} values always present. This will produce a sample set matching the posterior
   * distribution of the {@link BayesianNetwork} conditional on these observed states as the number
   * of cycles trends towards infinity.
   *
   * @param observedStateIds a collection of ids, each associated with a {@link NodeState} which
   *     will always be present in every {@link Sample}.
   * @param numberOfSamples the number of cycles in the solver run, and the number of samples in the
   *     final {@link SampleCollection}.
   * @return a new {@link SampleCollection}.
   * @throws NodeStateConflictException if multiple {@link NodeState}s in the input share the same
   *     {@link Node}.
   * @throws BayesNetIDException if any id could not be linked to a {@link NodeState} within the
   *     {@link BayesianNetwork}.
   */
  <T extends Serializable> SampleCollection generateSamplesById(
      Collection<T> observedStateIds, int numberOfSamples);

  /**
   * Returns the {@link BayesianNetwork} which is sampled by this {@code Sampler}.
   *
   * @return the {@link BayesianNetwork} used by this {@code Sampler}.
   */
  BayesianNetwork getNetwork();
}
