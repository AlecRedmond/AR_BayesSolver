package io.github.alecredmond.internal.method.inference.solver;

import io.github.alecredmond.export.application.inference.SolverResults;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.method.inference.BayesSolver;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.internal.application.inference.SolverConfigs;
import io.github.alecredmond.internal.method.inference.solver.cptmapper.DirectCptMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BayesSolverImpl extends JTASolver implements BayesSolver {
  private final DirectCptMapper directCptMapper;
  private SolverResults results;

  public BayesSolverImpl(BayesianNetwork network) {
    super(network, new SolverConfigs());
    this.results = null;
    this.directCptMapper = new DirectCptMapper(network.getNetworkData());
  }

  @Override
  public boolean solve() {
    if (isSolved()) return true;
    return forceSolve();
  }

  @Override
  public boolean solve(SolverType solverType) {
    if (isSolved()) return true;
    return forceSolve(solverType);
  }

  @Override
  public boolean forceSolve(SolverType solverType) {
    try {
      configs.updateConfigs();
      configs.setSolverType(solverType);
      network.buildNetworkData();
      return forceSolveCommon();
    } catch (Exception e) {
      log.error(e.getLocalizedMessage(), e);
      return false;
    }
  }

  @Override
  public boolean isSolved() {
    return network.isSolved();
  }

  @Override
  public boolean forceSolve() {
    try {
      configs.updateConfigs();
      network.buildNetworkData();
      if (writeConstraintsToCPTs()) return true;
      return forceSolveCommon();
    } catch (Exception e) {
      log.error(e.getLocalizedMessage(), e);
      return false;
    }
  }

  public boolean writeConstraintsToCPTs() {
    boolean mapped = directCptMapper.tryDirectImpute();
    if (mapped) {
      log.info(
          "Constraints on Network '{}' match a complete CPT entry set and were written directly to the network.",
          network.getNetworkData().getNetworkName());
      network.getNetworkData().setSolved(true);
    }
    return mapped;
  }

  private boolean forceSolveCommon() {
    BayesianNetworkData data = network.getNetworkData();
    data.setSolved(false);
    results = null;
    results = solveNetwork();
    data.setSolved(true);
    return true;
  }

  @Override
  public SolverResults getResults() {
    if (results == null) {
      log.warn("NO RESULTS FROM CURRENT SOLVER");
    }
    return results;
  }
}
