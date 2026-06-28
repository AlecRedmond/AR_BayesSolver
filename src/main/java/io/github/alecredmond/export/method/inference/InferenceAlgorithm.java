package io.github.alecredmond.export.method.inference;

/**
 * The variants of direct inference an {@code InferenceEngine} can perform. These are set on the
 * creation of an {@code InferenceEngine} instance and cannot be changed. There are currently two
 * variants of inference that can be used, and the default can be configured in {@code
 * app.properties} using {@code app.inference.defaultInferenceAlgorithm}.
 *
 * <ul>
 *   <li><b>JUNCTION_TREE_ALGORITHM</b> (JTA) is the default inference algorithm and its use is
 *       advisable in almost all cases. JTA decomposes the network into a junction tree of smaller
 *       cliques, each with its own joint sub-table, connected by separators formed from their
 *       shared nodes. Observations are applied per-clique and propagated via message-passing
 *       through the separators. JTA scales on the treewidth of the network, and will typically
 *       yield an exponential improvement over the joint table method at the cost of a small
 *       overhead. It is also the only option for networks whose full joint Cartesian product would
 *       exceed 2<sup>31</sup>&minus;1 entries.
 *   <li><b>SINGLE_TABLE_ALGORITHM</b> (STA) is useful only in niche cases. STA maps the Cartesian
 *       product of all node states in the network to a single joint probability table. This has a
 *       low overhead but very poor time and memory complexity scaling. There may be some benefit in
 *       very small networks (with a joint Cartesian product of 2<sup>9</sup> entries or fewer)
 *       under high loads of observation queries. The absolute upper limit for this variant is a
 *       table length of 2<sup>31</sup>&minus;1 entries, the maximum length of a Java array.
 * </ul>
 */
public enum InferenceAlgorithm {
  /**
   * <b>JUNCTION_TREE_ALGORITHM</b> (JTA) is the default inference algorithm and its use is
   * advisable in almost all cases. JTA decomposes the network into a junction tree of smaller
   * cliques, each with its own joint sub-table, connected by separators formed from their shared
   * nodes. Observations are applied per-clique and propagated via message-passing through the
   * separators. JTA scales on the treewidth of the network, and will typically yield an exponential
   * time and memory complexity improvement over the joint table method at the cost of a small
   * overhead. It is also the only option for networks whose full joint Cartesian product would
   * exceed 2<sup>31</sup>&minus;1 entries.
   */
  JUNCTION_TREE_ALGORITHM,
  /**
   * <b>SINGLE_TABLE_ALGORITHM</b> (STA) is useful only in niche cases. STA maps the Cartesian
   * product of all node states in the network to a single joint probability table. This has a low
   * overhead but very poor time and memory complexity scaling. There may be some benefit in very
   * small networks (with a joint Cartesian product of 2<sup>9</sup> entries or fewer) under high
   * loads of observation queries. The absolute upper limit for this variant is a table length of
   * 2<sup>31</sup>&minus;1 entries, the maximum length of a Java array.
   */
  SINGLE_TABLE_ALGORITHM
}
