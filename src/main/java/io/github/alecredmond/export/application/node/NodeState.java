package io.github.alecredmond.export.application.node;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

/**
 * Represents a single state that a {@link Node} variable can exhibit.
 *
 * <p>Nodes within a Bayesian Network will typically have multiple states (for example "TRUE" or
 * "FALSE"), and these should comprehensively cover all possible outcomes a Node can achieve.
 *
 * <p>While a {@code NodeState}'s identifier can be anything both unique and serializable, when
 * defining an identifier using a descriptive string, it is highly advisable to prepend the state
 * name with the name of its parent {@code Node}:
 *
 * <ul>
 *   <li><b>Recommended:</b> {@code NodeState rainTrue = new NodeState("RAIN:TRUE", rain)}
 *   <li><b>Avoid:</b> {@code NodeState rainTrue = new NodeState("TRUE", rain)}
 * </ul>
 *
 * <p>This avoids any ID conflicts with other nodes that share the same state names ("TRUE" and
 * "FALSE" being the most obvious) while also making the origin of the state immediately clear.
 *
 * @author Alec Redmond
 * @see Node
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class NodeState {
  /** The unique identifier for this state. */
  @EqualsAndHashCode.Include private final Serializable id;

  /** The parent {@link Node} that exhibits this state. */
  private final Node node;

  /**
   * Constructs a new {@code NodeState} with a unique identifier and its associated {@link Node}.
   *
   * @param id the {@link Serializable} unique identifier for this state
   * @param node the {@link Node} that this state belongs to
   * @param <T> the type of the state identifier
   * @throws NullPointerException if either the id or the node is {@code null}
   */
  public <T extends Serializable> NodeState(@NonNull T id, @NonNull Node node) {
    this.id = id;
    this.node = node;
  }

  @Override
  public String toString() {
    return id.toString();
  }
}
