package com.artools.application.solver;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SolverConfigs {
  private int cyclesLimit = 1_000;
  private int timeLimitSeconds = 60;
  private int logIntervalSeconds = 1;
  private double convergeThreshold = 1e-9;
}
