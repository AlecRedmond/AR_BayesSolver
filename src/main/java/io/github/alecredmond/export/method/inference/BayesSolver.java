package io.github.alecredmond.export.method.inference;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.inference.SolverResults;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.internal.method.inference.BayesSolverImpl;

/**
 * A solver that provides the best fit solution for a network given its constraints. BayesSolver
 * uses a variation of Iterative Proportional Fitting Procedure (IPFP) to repeatedly fit After
 * solving, results showing the error and loss descent per constraint can be reviewed by calling
 * {@code .getResults()} on the instance.
 *
 * <p>Properties concerning the maximum number of cycles, time, convergence values, and logging
 * options can be found under 'app.properties' in the 'app.solver' section.
 *
 * <p>There are two types of IPFP currently implemented; either a full joint distribution IPFP
 * procedure, or a Junction Tree Algorithm derived procedure. These can be changed in
 * 'app.properties' on the line {@code app.inference.useJunctionTreeSolver} (default = false).
 *
 * @see SolverResults
 */
public interface BayesSolver {

  /**
   * Creates a new BayesSolver instance linked to the given network.
   *
   * @param network a Bayesian network instance.
   * @return a new BayesSolver instance.
   */
  static BayesSolver create(BayesianNetwork network) {
    return new BayesSolverImpl(network);
  }

  /**
   * Runs the solver with the default IPFP if the network is not yet solved. The default procedure
   * can be changed in 'app.properties' using {@code app.inference.useJunctionTreeSolver} (default =
   * false). If this is unsuccessful, it will catch and log the associated error.
   *
   * @return true if the solver procedure completes successfully, or the network was already solved.
   */
  boolean solve();

  /**
   * Forces the solver to run with the default IPFP, regardless of if the network is already solved.
   * The default procedure can be changed in 'app.properties' using {@code
   * app.inference.useJunctionTreeSolver} (default = false). If this is unsuccessful, it will catch
   * and log the associated error.
   *
   * @return true if the solver procedure completes successfully.
   */
  boolean solve(SolverType solverType);

  boolean forceSolve();

  /**
   * Checks if the network has already been solved.
   *
   * @return true if the network is already solved
   */
  boolean forceSolve(SolverType solverType);

  boolean isSolved();

  /**
   * Returns the results of the last solver run using this instance. SolverResults include
   * information on the overall error and error loss per cycle over every {@link
   * ProbabilityConstraint} defined on the network.
   *
   * @return results of the latest run, or null if no runs have occurred.
   */
  SolverResults getResults();

  enum SolverType {
    JOINT_TABLE_IPFP,
    JUNCTION_TREE_IPFP
  }
}
