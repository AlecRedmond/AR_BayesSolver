package io.github.alecredmond.application.solver;

import io.github.alecredmond.method.sampler.InferenceEngine;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the configuration parameters for the {@link InferenceEngine} class.
 *
 * <p>This class holds settings that control the solver's execution, such as iteration limits, time
 * limits, logging frequency, and the threshold for determining convergence.
 */
@Data
@NoArgsConstructor
public class InferenceEngineConfigs {
  /**
   * The maximum number of cycles (iterations * constraints) the solver is allowed to run. Default
   * is 1,000.
   */
  private int solverCyclesLimit = 1_000;

  /** The maximum execution time in seconds for the solver. Default is 60 seconds. */
  private int solverTimeLimitSeconds = 60;

  /**
   * The time interval in seconds at which the solver's progress should be logged. Default is 1
   * second.
   */
  private int solverLogIntervalSeconds = 1;

  /** The threshold value used to determine if the solver has converged. Default is 1e-9. */
  private double solverConvergeThreshold = 1e-9;

  /** The number of blocks changed at once in the Gibbs sampler algorithm. Default is 2 */
  private int gibbsSamplerBlocks = 2;

  /**
   * The ratio of burn-in cycles to sampled cycles in the Gibbs sampler algorithm. Default is 0.3
   */
  private double gibbsBurnInFactor = 0.3;

  /**
   * Sets a new convergence threshold for the solver
   *
   * @param solverConvergeThreshold the loss threshold at which the solver considers itself solved.
   *     Default == 1e-9
   * @throws IllegalArgumentException if the converge threshold is not greater than zero
   */
  public void setSolverConvergeThreshold(double solverConvergeThreshold) {
    if (solverConvergeThreshold <= 0) {
      throw new IllegalArgumentException("Convergence threshold must be greater than zero!");
    }
    this.solverConvergeThreshold = solverConvergeThreshold;
  }

  /**
   * Sets the number of seconds between the logger outputting an update status.
   *
   * @param solverLogIntervalSeconds new value for seconds between logging times. Default == 1
   *     second.
   * @throws IllegalArgumentException if the log interval is not greater than 0
   */
  public void setSolverLogIntervalSeconds(int solverLogIntervalSeconds) {
    if (solverLogIntervalSeconds <= 0) {
      throw new IllegalArgumentException("Log interval must be positive!");
    }
    this.solverLogIntervalSeconds = solverLogIntervalSeconds;
  }

  /**
   * Sets the maximum number of cycles the solver is allowed to run. A cycle's length is equal to
   * the number of constraints on the network.
   *
   * @param solverCyclesLimit number of cycles before the solver will force quit. Default == 1,000
   * @throws IllegalArgumentException if the number of cycles is not greater than 0.
   */
  public void setSolverCyclesLimit(int solverCyclesLimit) {
    if (solverCyclesLimit <= 0) {
      throw new IllegalArgumentException("Number of cycles must be greater than zero!");
    }
    this.solverCyclesLimit = solverCyclesLimit;
  }

  /**
   * Sets the time limit for the solver's run. After the given amount, the solver will complete its
   * current cycle then exit.
   *
   * @param solverTimeLimitSeconds the new time limit in seconds. Default == 60
   * @throws IllegalArgumentException if given a time limit which is not greater than 0
   */
  public void setSolverTimeLimitSeconds(int solverTimeLimitSeconds) {
    if (solverTimeLimitSeconds <= 0) {
      throw new IllegalArgumentException("Time limit must be greater than zero!");
    }
    this.solverTimeLimitSeconds = solverTimeLimitSeconds;
  }

  /**
   * Sets the number of blocks changed at once in the Gibbs sampler algorithm. This is recommended
   * to be above 1 to avoid the sampler "locking" under deterministic conditions.
   *
   * @param gibbsSamplerBlocks the new Gibbs sampler block size. Default == 2
   * @throws IllegalArgumentException if given a block size less than 1
   */
  public void setGibbsSamplerBlocks(int gibbsSamplerBlocks) {
    if (gibbsSamplerBlocks <= 0) {
      throw new IllegalArgumentException("number of sampler blocks must be greater than zero!");
    }
    this.gibbsSamplerBlocks = gibbsSamplerBlocks;
  }

  /**
   * Sets the ratio of burn-in samples to desired samples in the Gibbs sampler algorithm.
   *
   * @param gibbsBurnInFactor the new Gibbs sampler burn-in factor. Default == 0.3
   * @throws IllegalArgumentException if given a block size less than 1
   */
  public void setGibbsBurnInFactor(double gibbsBurnInFactor) {
    if (gibbsBurnInFactor <= 0) {
      throw new IllegalArgumentException("burn in factor must be greater than zero!");
    }
    this.gibbsBurnInFactor = gibbsBurnInFactor;
  }
}
