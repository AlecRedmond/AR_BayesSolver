package io.github.alecredmond.application.inference;

import static io.github.alecredmond.application.inference.SampleGeneratorType.LIKELIHOOD_WEIGHTING_SAMPLER;

import io.github.alecredmond.method.inference.InferenceEngine;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents the configuration parameters for the {@link InferenceEngine} class.
 *
 * <p>This class holds settings that control the solver's execution, such as iteration limits, time
 * limits, logging frequency, and the threshold for determining convergence.
 */
@Data
@NoArgsConstructor
@Slf4j
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

  /**
   * The type of Sample Generator used by the inference engine. Default = Likelihood Weighting
   * Sampler. <br>
   * {@code Note: Currently, the only Sample Generator is Likelihood Weighting Sampler. This may
   * change in future versions.}.
   */
  private SampleGeneratorType sampleGenerator = LIKELIHOOD_WEIGHTING_SAMPLER;

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
    double warningValue = 1E-3;
    if (solverConvergeThreshold > warningValue) {
      log.warn(
          "Convergence Threshold set to {}. It is recommended to set the threshold below {}.",
          solverConvergeThreshold,
          warningValue);
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
}
