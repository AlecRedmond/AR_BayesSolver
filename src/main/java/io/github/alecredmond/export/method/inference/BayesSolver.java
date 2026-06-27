package io.github.alecredmond.export.method.inference;

import io.github.alecredmond.export.application.constraints.ConditionalConstraint;
import io.github.alecredmond.export.application.constraints.MarginalConstraint;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.inference.SolverResults;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ConditionalTable;
import io.github.alecredmond.export.application.probabilitytables.RootNodeTable;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.internal.method.inference.solver.BayesSolverImpl;

/**
 * A solver that provides the best fit solution for a {@link BayesianNetwork} given its constraints.
 * {@code BayesSolver} applies a variation of the Iterative Proportional Fitting Procedure (IPFP) to
 * iteratively adjust the network's distribution until it satisfies all defined constraints. After
 * solving, per-constraint error and loss results can be reviewed via {@link #getResults()}.
 *
 * <p>Two IPFP variants are currently available; a full joint-distribution procedure ({@link
 * SolverType#SINGLE_TABLE_IPFP}), and a Junction Tree Algorithm-derived procedure ({@link
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
   * <p><i>Note: This method will first call {@link #writeConstraintsToCPTs()}, which will run in
   * place of the IPFP algorithm in cases where all constraints can be mapped directly to the
   * network's CPTs. To force the use of an IPFP variant, use the method {@link
   * #forceSolve(SolverType)}.</i>
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
   * <p><i>Note: This method will first call {@link #writeConstraintsToCPTs()}, which will run in
   * place of the IPFP algorithm in cases where all constraints can be mapped directly to the
   * network's CPTs. To force the use of an IPFP variant, use the method {@link
   * #forceSolve(SolverType)}.</i>
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
   * Maps all constraints in the {@link BayesianNetwork} to their CPT entries, if it is possible to
   * do so. For this to succeed every constraint must be either:
   *
   * <ul>
   *   <li>A {@link MarginalConstraint} mapping an entry in a valid {@link RootNodeTable}.
   *   <li>A {@link ConditionalConstraint} mapping an entry in a valid {@link ConditionalTable}.
   * </ul>
   *
   * For every condition {@link NodeState} combination in a given table, there must be at least
   * {@code n - 1} valid constraints where {@code n} is the number of {@link NodeState}s in the
   * table's event {@link Node}. The final constraint will be filled in automatically.
   *
   * <p><i>Note: This is run automatically when {@link #solve()} or {@link #forceSolve()} are called
   * and will bypass IPFP if successful. To force the use of an IPFP variant, use the method {@link
   * #forceSolve(SolverType)}.</i>
   *
   * @return {@code true} if all constraints were successfully mapped directly to the network's
   *     CPTs.
   */
  boolean writeConstraintsToCPTs();

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
   *       <p><b>SINGLE_TABLE_IPFP</b> is the most basic form of iterative proportional fitting. The
   *       Cartesian product of all node states is mapped to a single joint probability table, and
   *       all constraints are applied to that table.
   *       <p><b>Time complexity: O(2<sup>n</sup>), where n is the number of nodes in the
   *       network.</b>
   *       <p>This method has a low overhead and will typically converge faster for small networks
   *       where the joint table would contain fewer than 2<sup>10</sup> entries. Performance
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
   *       SolverType#SINGLE_TABLE_IPFP} as nodes are added. It is also the only option for networks
   *       whose full joint Cartesian product would exceed 2<sup>31</sup>&minus;1 entries.
   * </ul>
   */
  enum SolverType {
    /**
     * <b>SINGLE_TABLE_IPFP</b> is the most basic form of iterative proportional fitting. The
     * Cartesian product of all node states is mapped to a single joint probability table, and all
     * constraints are applied to that table.
     *
     * <p><b>Time complexity: O(2<sup>n</sup>), where n is the number of nodes in the network.</b>
     *
     * <p>This method has a low overhead and will typically converge faster for small networks where
     * the joint table would contain fewer than 2<sup>10</sup> entries. Performance degrades
     * exponentially beyond this threshold. The absolute upper limit is 2<sup>31</sup>&minus;1
     * entries, the maximum length of a Java array.
     */
    SINGLE_TABLE_IPFP,
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
     * SolverType#SINGLE_TABLE_IPFP} as nodes are added. It is also the only option for networks
     * whose full joint Cartesian product would exceed 2<sup>31</sup>&minus;1 entries.
     */
    JUNCTION_TREE_IPFP
  }
}
