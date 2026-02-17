package io.github.alecredmond.application.inference;

import static io.github.alecredmond.application.inference.SampleGeneratorType.LIKELIHOOD_WEIGHTING_SAMPLER;

import io.github.alecredmond.method.utils.PropertiesLoader;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@Slf4j
public class InferenceEngineConfigs {
  private int solverCyclesLimit;
  private int solverTimeLimitSeconds;
  private int solverLogIntervalSeconds;
  private double solverConvergeThreshold;
  private boolean logSolverProgress;
  private SampleGeneratorType sampleGenerator;

  public InferenceEngineConfigs() {
    PropertiesLoader loader = new PropertiesLoader();
    setSolverCyclesLimit(loader.loadInt("app.solver.cyclesLimit"));
    setSolverTimeLimitSeconds(loader.loadInt("app.solver.timeLimitSeconds"));
    setSolverLogIntervalSeconds(loader.loadInt("app.solver.logIntervalSeconds"));
    setSolverConvergeThreshold(loader.loadDouble("app.solver.convergeThreshold"));
    setLogSolverProgress(loader.loadBoolean("app.solver.logSolverProgress"));
    sampleGenerator = LIKELIHOOD_WEIGHTING_SAMPLER;
  }

  public void setSolverCyclesLimit(int solverCyclesLimit) {
    assureGreaterThanZero("Solver Cycles Limit", solverCyclesLimit);
    this.solverCyclesLimit = solverCyclesLimit;
  }

  public void setSolverTimeLimitSeconds(int solverTimeLimitSeconds) {
    assureGreaterThanZero("Time Limit", solverTimeLimitSeconds);
    this.solverTimeLimitSeconds = solverTimeLimitSeconds;
  }

  public void setSolverLogIntervalSeconds(int solverLogIntervalSeconds) {
    assureGreaterThanZero("Solver Log Interval", solverLogIntervalSeconds);
    this.solverLogIntervalSeconds = solverLogIntervalSeconds;
  }

  public void setSolverConvergeThreshold(double solverConvergeThreshold) {
    assureGreaterThanZero("Solver Converge Threshold", solverConvergeThreshold);
    this.solverConvergeThreshold = solverConvergeThreshold;
  }

  private void assureGreaterThanZero(String type, Number number) {
    if (number.doubleValue() > 0) return;
    throw new IllegalArgumentException(type + " must be greater than zero!");
  }
}
