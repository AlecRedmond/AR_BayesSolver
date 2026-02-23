package io.github.alecredmond.application.inference;

import io.github.alecredmond.method.utils.PropertiesLoader;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@Slf4j
public class SolverConfigs {
  private int cyclesLimit;
  private int timeLimitSeconds;
  private int logIntervalSeconds;
  private double convergeThreshold;
  private boolean logSolverProgress;

  public SolverConfigs() {
    PropertiesLoader loader = new PropertiesLoader();
    setCyclesLimit(loader.loadInt("app.solver.cyclesLimit"));
    setTimeLimitSeconds(loader.loadInt("app.solver.timeLimitSeconds"));
    setLogIntervalSeconds(loader.loadInt("app.solver.logIntervalSeconds"));
    setConvergeThreshold(loader.loadDouble("app.solver.convergeThreshold"));
    setLogSolverProgress(loader.loadBoolean("app.solver.logSolverProgress"));
  }

  private void setCyclesLimit(int cyclesLimit) {
    assureGreaterThanZero("Solver Cycles Limit", cyclesLimit);
    this.cyclesLimit = cyclesLimit;
  }

  private void setTimeLimitSeconds(int timeLimitSeconds) {
    assureGreaterThanZero("Time Limit", timeLimitSeconds);
    this.timeLimitSeconds = timeLimitSeconds;
  }

  private void setLogIntervalSeconds(int logIntervalSeconds) {
    assureGreaterThanZero("Solver Log Interval", logIntervalSeconds);
    this.logIntervalSeconds = logIntervalSeconds;
  }

  private void setConvergeThreshold(double convergeThreshold) {
    assureGreaterThanZero("Solver Converge Threshold", convergeThreshold);
    this.convergeThreshold = convergeThreshold;
  }

  private void assureGreaterThanZero(String type, Number number) {
    if (number.doubleValue() > 0) return;
    throw new IllegalArgumentException(type + " must be greater than zero!");
  }
}
