package io.github.alecredmond.application.solver;

import io.github.alecredmond.method.solver.BayesSolver;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the configuration parameters for the {@link BayesSolver}. <br>
 * This class holds settings that control the solver's execution, such as iteration limits, time
 * limits, logging frequency, and the threshold for determining convergence.
 */
@Data
@NoArgsConstructor
public class SolverConfigs {
  /**
   * The maximum number of cycles (iterations * constraints) the solver is allowed to run. Default
   * is 1,000.
   */
  private int cyclesLimit = 1_000;

  /** The maximum execution time in seconds for the solver. Default is 60 seconds. */
  private int timeLimitSeconds = 60;

  /**
   * The time interval in seconds at which the solver's progress should be logged. Default is 1
   * second.
   */
  private int logIntervalSeconds = 1;

  /** The threshold value used to determine if the solver has converged. Default is 1e-9. */
  private double convergeThreshold = 1e-9;

  public void setConvergeThreshold(double convergeThreshold) {
    if (convergeThreshold <= 0) {
      throw new IllegalArgumentException("Convergence threshold must be greater than zero!");
    }
    this.convergeThreshold = convergeThreshold;
  }

  public void setLogIntervalSeconds(int logIntervalSeconds) {
    if (logIntervalSeconds <= 0) {
      throw new IllegalArgumentException("Log interval must be positive!");
    }
    this.logIntervalSeconds = logIntervalSeconds;
  }

  public void setCyclesLimit(int cyclesLimit) {
    if (cyclesLimit <= 0) {
      throw new IllegalArgumentException("Number of cycles must be greater than zero!");
    }
    this.cyclesLimit = cyclesLimit;
  }

  public void setTimeLimitSeconds(int timeLimitSeconds) {
    if (timeLimitSeconds <= 0) {
      throw new IllegalArgumentException("Time limit must be greater than zero!");
    }
    this.timeLimitSeconds = timeLimitSeconds;
  }
}
