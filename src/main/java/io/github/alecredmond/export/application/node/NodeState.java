package io.github.alecredmond.export.application.node;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

/**
 * A single state that a {@link Node} variable can exhibit. Nodes within a Bayesian Network will
 * typically have multiple states, for example "TRUE" or "FALSE", and these should cover all
 * possible outcomes a Node can achieve.
 *
 * <p>While a NodeState's id can be anything both unique and serializable, when defining a
 * NodeState's ID using a descriptive string it is advisable to pre-append the state
 * name with the name of the Node:
 *
 * <ul>
 *   <li><b>Recommended:</b>{@code NodeState rainTrue = new NodeState("RAIN:TRUE",rain)}
 *   <li><b>Avoid:</b>{@code NodeState rainTrue = new NodeState("TRUE",rain)}
 * </ul>
 *
 * This avoids any ID conflicts with other nodes that share the same state names ("TRUE"/"FALSE"
 * being the most obvious) while also making the origin of the state clear.
 *
 * @author Alec Redomond
 * @see Node
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class NodeState {
  @EqualsAndHashCode.Include private final Serializable id;
  private final Node node;

  public <T extends Serializable> NodeState(@NonNull T id, @NonNull Node node) {
    this.id = id;
    this.node = node;
  }

  @Override
  public String toString() {
    return id.toString();
  }
}
