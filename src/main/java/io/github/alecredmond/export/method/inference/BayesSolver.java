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
   * Runs the solver with the IPFP set in 'app.properties', if the network is not yet solved. The
   * default procedure can be changed using {@code app.solver.useJunctionTreeSolver} (default =
   * true). If this is unsuccessful, it will catch and log the associated error.
   *
   * @return true if the solver procedure completes successfully, or the network was already solved.
   */
  boolean solve();

  /**
   * Runs the solver with the specified IPFP variation, if the network is not yet solved. If this is
   * unsuccessful, it will catch and log the associated error.
   *
   * @param solverType the type of IPFP to be used.
   * @return true if the solver procedure completes successfully, or the network was already solved.
   */
  boolean solve(SolverType solverType);

  /**
   * Forces the solver to run with the IPFP set in 'app.properties', regardless of if the network is
   * already solved. The default procedure can be changed in using {@code
   * app.solver.useJunctionTreeSolver} (default = true). If this is unsuccessful, it will catch and
   * log the associated error.
   *
   * @return true if the solver procedure completes successfully.
   */
  boolean forceSolve();

  /**
   * Forces the solver to run with specified IPFP variation, regardless of if the network is already
   * solved. If this is unsuccessful, it will catch and log the associated error.
   *
   * @param solverType the type of IPFP to be used.
   * @return true if the solver procedure completes successfully.
   */
  boolean forceSolve(SolverType solverType);

  /**
   * Checks if the network has already been solved.
   *
   * @return true if the network is already solved
   */
  boolean isSolved();

  /**
   * Returns the results of the last solver run using this instance. SolverResults include
   * information on the overall error and error loss per cycle over every {@link
   * ProbabilityConstraint} defined on the network.
   *
   * @return results of the latest run, or null if no runs have occurred.
   */
  SolverResults getResults();

  /**
   * The type of Iterative Proportional Fitting Procedure (IPFP) used in the solver process. There
   * are currently two variations:
   *
   * <ul>
   *   <li>
   *       <p><b>JOINT_TABLE_IPFP</b> is the most basic form of iterative proportional fitting,
   *       where the Cartesian product of all node states are mapped to a single joint probability
   *       table to which all constraints are applied.
   *       <p>This method has a low overhead and is typically faster for small networks where the
   *       joint product of the network would result in ~100,000 entries or fewer (roughly
   *       equivalent to 17 True/False nodes). After this point, adding nodes will result in
   *       exponentially diminishing returns. The hard limit for this is 2^31-1 entries, or the max
   *       value of a signed 32-bit integer.
   *   <li>
   *       <p><b>JUNCTION_TREE_IPFP</b> discretizes the network into a junction tree of several
   *       smaller cliques, each with its own miniature joint table, connected to other cliques by
   *       separators formed from their shared nodes. Batches of constraints associated with each
   *       clique are solved and the results distributed through the separators.
   *       <p>This method has a higher overhead but scales on the Cartesian product of the maximum
   *       <i>Treewidth</i> of the network and its constraints. As a result, this method will
   *       typically provide an exponential speed increase over the full joint table method for
   *       every additional node added. This is also the only way to solve a network whose joint
   *       Cartesian product would exceed 2^31 entries, which is the length limit of an array in
   *       Java.
   * </ul>
   */
  enum SolverType {
    JOINT_TABLE_IPFP,
    JUNCTION_TREE_IPFP
  }
}
