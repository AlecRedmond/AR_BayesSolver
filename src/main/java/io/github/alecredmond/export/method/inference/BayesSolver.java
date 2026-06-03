package io.github.alecredmond.export.method.inference;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.inference.SolverResults;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.internal.method.inference.BayesSolverImpl;

/**
 * A solver that provides the best fit solution for a {@link BayesianNetwork} given its constraints.
 * {@code BayesSolver} applies a variation of the Iterative Proportional Fitting Procedure (IPFP) to
 * iteratively adjust the network's distribution until it satisfies all defined constraints. After
 * solving, per-constraint error and loss results can be reviewed via {@link #getResults()}.
 *
 * <p>Two IPFP variants are currently available; a full joint-distribution procedure ({@link
 * SolverType#JOINT_TABLE_IPFP}), and a Junction Tree Algorithm-derived procedure ({@link
 * SolverType#JUNCTION_TREE_IPFP}). The default variant is controlled by {@code
 * app.solver.useJunctionTreeSolver} in {@code app.properties} (default: {@code true}).
 *
 * <p>Additional solver properties - including the maximum number of cycles, time limits, and
 * convergence thresholds - are configurable under the {@code app.solver} section of {@code
 * app.properties}.
 *
 * @see SolverResults
 * @see SolverType
 * @author Alec Redmond
 */
public interface BayesSolver {

  /**
   * Creates a new {@code BayesSolver} instance linked to the given network.
   *
   * @param network the Bayesian network to solve.
   * @return a new {@code BayesSolver} instance.
   */
  static BayesSolver create(BayesianNetwork network) {
    return new BayesSolverImpl(network);
  }

  /**
   * Runs the solver using the IPFP variant configured in {@code app.properties}, unless the network
   * has already been solved. The active variant is controlled by {@code
   * app.solver.useJunctionTreeSolver} (default: {@code true}). Any exception thrown during the
   * process is caught and logged rather than propagated.
   *
   * @return {@code true} if the solver procedure completes successfully, or the network was already
   *     solved; {@code false} if an error was encountered.
   */
  boolean solve();

  /**
   * Runs the solver using the specified IPFP variant, unless the network has already been solved.
   * Any exception thrown during the solving process is caught and logged rather than propagated.
   *
   * @param solverType the IPFP variant to use.
   * @return {@code true} if the solver completes successfully, or the network was already solved;
   *     {@code false} if an error was encountered.
   */
  boolean solve(SolverType solverType);

  /**
   * Runs the solver using the IPFP variant configured in {@code app.properties}, regardless of
   * whether the network has already been solved. The active variant is controlled by {@code
   * app.solver.useJunctionTreeSolver} (default: {@code true}). Any exception thrown during the
   * solving process is caught and logged rather than propagated.
   *
   * @return {@code true} if the solver completes successfully; {@code false} if an error was
   *     encountered.
   */
  boolean forceSolve();

  /**
   * Runs the solver using the specified IPFP variant, regardless of whether the network has already
   * been solved. Any exception thrown during the solving process is caught and logged rather than
   * propagated.
   *
   * @param solverType the IPFP variant to use.
   * @return {@code true} if the solver completes successfully; {@code false} if an error was
   *     encountered.
   */
  boolean forceSolve(SolverType solverType);

  /**
   * Returns whether the network has already been solved.
   *
   * @return {@code true} if the network is already solved; {@code false} otherwise.
   */
  boolean isSolved();

  /**
   * Returns the results of the most recent solver run on this instance, including per-cycle error
   * and loss information for every {@link ProbabilityConstraint} on the network.
   *
   * @return the results of the most recent run, or {@code null} if no run has been performed.
   */
  SolverResults getResults();

  /**
   * The Iterative Proportional Fitting Procedure (IPFP) variant used during the solving process.
   *
   * <ul>
   *   <li>
   *       <p><b>JOINT_TABLE_IPFP</b> is the most basic form of iterative proportional fitting. The
   *       Cartesian product of all node states is mapped to a single joint probability table, and
   *       all constraints are applied to that table.
   *       <p><b>Time complexity: O(2<sup>n</sup>), where n is the number of nodes in the
   *       network.</b>
   *       <p>This method has a low overhead and is typically the more efficient choice for small
   *       networks where the joint table contains fewer than 2<sup>16</sup> entries. Performance
   *       degrades exponentially beyond this threshold. The absolute upper limit is
   *       2<sup>31</sup>&minus;1 entries, the maximum length of a Java array.
   *   <li>
   *       <p><b>JUNCTION_TREE_IPFP</b> decomposes the network into a junction tree of smaller
   *       cliques, each with its own joint sub-table, connected by separators formed from their
   *       shared nodes. Constraints are batched per clique, solved locally, and the results
   *       propagated through the separators.
   *       <p><b>Time complexity: O(c &times; 2<sup>t</sup>), where t is the treewidth and c is the
   *       number of cliques in the junction tree.</b>
   *       <p>This variant carries higher overhead but scales with treewidth rather than total node
   *       count, typically yielding an exponential speed improvement over {@link
   *       SolverType#JOINT_TABLE_IPFP} as nodes are added. It is also the only option for networks
   *       whose full joint Cartesian product would exceed 2<sup>31</sup>&minus;1 entries.
   * </ul>
   */
  enum SolverType {
    /**
     * <b>JOINT_TABLE_IPFP</b> is the most basic form of iterative proportional fitting. The
     * Cartesian product of all node states is mapped to a single joint probability table, and all
     * constraints are applied to that table.
     *
     * <p><b>Time complexity: O(2<sup>n</sup>), where n is the number of nodes in the network.</b>
     *
     * <p>This method has a low overhead and is typically the more efficient choice for small
     * networks where the joint table contains fewer than 2<sup>16</sup> entries. Performance
     * degrades exponentially beyond this threshold. The absolute upper limit is
     * 2<sup>31</sup>&minus;1 entries, the maximum length of a Java array.
     */
    JOINT_TABLE_IPFP,
    /**
     * <b>JUNCTION_TREE_IPFP</b> decomposes the network into a junction tree of smaller cliques,
     * each with its own joint sub-table, connected by separators formed from their shared nodes.
     * Constraints are batched per clique, solved locally, and the results propagated through the
     * separators.
     *
     * <p><b>Time complexity: O(c &times; 2<sup>t</sup>), where t is the treewidth and c is the
     * number of cliques in the junction tree.</b>
     *
     * <p>This variant carries higher overhead but scales with treewidth rather than total node
     * count, typically yielding an exponential speed improvement over {@link
     * SolverType#JOINT_TABLE_IPFP} as nodes are added. It is also the only option for networks
     * whose full joint Cartesian product would exceed 2<sup>31</sup>&minus;1 entries.
     */
    JUNCTION_TREE_IPFP
  }
}
