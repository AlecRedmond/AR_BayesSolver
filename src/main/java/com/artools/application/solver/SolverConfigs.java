package com.artools.application.solver;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SolverConfigs {
  private int cyclesLimit = 60_000;
  private int timeLimitSeconds = 60;
  private double stepSize = 1;
  private double convergeThreshold = 1e-9;
  private double gradientEpsilon = 1e-3;
  private double stepDecreaseScalar = 0.65;
  private double stepIncreaseThreshold = 1e-3;
  private double stepIncreaseScalar = 1.25;
}
