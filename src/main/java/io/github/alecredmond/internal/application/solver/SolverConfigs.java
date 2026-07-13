package io.github.alecredmond.internal.application.solver;

import static io.github.alecredmond.internal.method.utils.AppProperty.*;

import io.github.alecredmond.export.solver.SolverAlgorithm;
import io.github.alecredmond.internal.method.utils.PropertiesLoader;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@Slf4j
public class SolverConfigs {
  private SolverAlgorithm solverAlgorithm;
  private int cyclesLimit;
  private int timeLimitSeconds;
  private int logIntervalSeconds;
  private double convergeThreshold;
  private boolean logSolverProgress;

  public SolverConfigs() {
    updateConfigs();
  }

  public void updateConfigs() {
    PropertiesLoader l = new PropertiesLoader();
    loadSolverAlgorithm(l.loadString(SOLVER_ALGORITHM));
    setCyclesLimit(l.loadInt(SOLVER_CYCLES_LIMIT));
    setTimeLimitSeconds(l.loadInt(SOLVER_TIME_LIMIT_SECONDS));
    setLogIntervalSeconds(l.loadInt(SOLVER_LOG_INTERVAL_SECONDS));
    setConvergeThreshold(l.loadDouble(SOLVER_CONVERGE_THRESHOLD));
    setLogSolverProgress(l.loadBoolean(SOLVER_LOG_PROGRESS));
  }

  private void loadSolverAlgorithm(String algorithmName) {
    String stripped = algorithmName.toUpperCase().replaceAll("\\s+", "");
    solverAlgorithm = SolverAlgorithm.valueOf(stripped);
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
