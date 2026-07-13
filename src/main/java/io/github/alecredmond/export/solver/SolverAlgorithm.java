package io.github.alecredmond.export.solver;

/**
 * The Iterative Proportional Fitting Procedure (IPFP) variant used during the solving process.
 * These can be manually specified in a {@link BayesSolver} instance using {@link
 * BayesSolver#solve(SolverAlgorithm)} or {@link BayesSolver#forceSolve(SolverAlgorithm)}. When not
 * manually specified, the solver will use the default variant in {@code app.properties} under
 * {@code app.bayes.solver.defaultSolverAlgorithm}.
 *
 * <p>There are two IPFP variants:
 *
 * <ul>
 *   <li>
 *       <p><b>SINGLE_TABLE_IPFP</b> is the most basic form of iterative proportional fitting. The
 *       Cartesian product of all node states is mapped to a single joint probability table, and all
 *       constraints are applied to that table.
 *       <p><b>Time complexity: O(2<sup>n</sup>), where n is the number of nodes in the network.</b>
 *       <p>This method has a low overhead and will typically converge faster for small networks
 *       where the joint table would contain fewer than 2<sup>10</sup> entries. Performance degrades
 *       exponentially beyond this threshold. The absolute upper limit is 2<sup>31</sup>&minus;1
 *       entries, the maximum length of a Java array.
 *   <li><b>JUNCTION_TREE_IPFP</b> <i>is the default solver algorithm.</i> This decomposes the
 *       network into a junction tree of smaller cliques, each with its own joint sub-table,
 *       connected by separators formed from their shared nodes. Constraints are batched per clique,
 *       and all cliques solved locally in parallel. The marginal sums are then transferred from one
 *       of the cliques through the separators. A single cycle completes when every clique has been
 *       used as the distribution point.
 *       <p><b>Time complexity: O(c &times; 2<sup>t</sup>), where t is the treewidth and c is the
 *       number of cliques in the junction tree.</b>
 *       <p>This variant carries higher overhead but scales with treewidth rather than total node
 *       count, typically yielding an exponential speed improvement over {@link
 *       SolverAlgorithm#SINGLE_TABLE_IPFP} as nodes are added. It is also the only option for
 *       networks whose full joint Cartesian product would exceed 2<sup>31</sup>&minus;1 entries.
 * </ul>
 */
public enum SolverAlgorithm {
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
   * <b>JUNCTION_TREE_IPFP</b> <i>is the default solver algorithm.</i> This decomposes the network
   * into a junction tree of smaller cliques, each with its own joint sub-table, connected by
   * separators formed from their shared nodes. Constraints are batched per clique, and all cliques
   * solved locally in parallel. The marginal sums are then transferred from one of the cliques
   * through the separators. A single cycle completes when every clique has been used as the
   * distribution point.
   *
   * <p><b>Time complexity: O(c &times; 2<sup>t</sup>), where t is the treewidth and c is the number
   * of cliques in the junction tree.</b>
   *
   * <p>This variant carries higher overhead but scales with treewidth rather than total node count,
   * typically yielding an exponential speed improvement over {@link
   * SolverAlgorithm#SINGLE_TABLE_IPFP} as nodes are added. It is also the only option for networks
   * whose full joint Cartesian product would exceed 2<sup>31</sup>&minus;1 entries.
   */
  JUNCTION_TREE_IPFP
}
