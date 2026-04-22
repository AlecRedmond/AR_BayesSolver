package io.github.alecredmond.export.application.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.export.method.probabilitytables.TableHelper;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;
import java.io.Serializable;
import java.util.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@EqualsAndHashCode()
@Slf4j
public abstract class ProbabilityTable {
  protected final Map<Serializable, NodeState> nodeStateIDMap;
  protected final Map<Serializable, Node> nodeIDMap;
  protected final ProbabilityVector vector;
  protected final Set<Node> nodes;
  protected final Set<Node> events;
  protected final Set<Node> conditions;
  @EqualsAndHashCode.Exclude protected final TableHelper<?> helper;
  @EqualsAndHashCode.Exclude @Setter protected Serializable tableName;

  protected <T extends Serializable> ProbabilityTable(
      Map<Serializable, NodeState> nodeStateIDMap,
      Map<Serializable, Node> nodeIDMap,
      ProbabilityVector vector,
      T tableName,
      Set<Node> nodes,
      Set<Node> events,
      Set<Node> conditions) {
    this.nodeStateIDMap = nodeStateIDMap;
    this.nodeIDMap = nodeIDMap;
    this.vector = vector;
    this.tableName = tableName;
    this.nodes = nodes;
    this.events = events;
    this.conditions = conditions;
    this.helper = buildHelper();
  }

  protected abstract TableHelper<? extends ProbabilityTable> buildHelper();

  public Double getProbability(Collection<NodeState> request) {
    try {
      TableUtils.confirmAllNodesQueried(request, this);
      return TableUtils.getProbability(request, this);
    } catch (IllegalArgumentException e) {
      log.error(e.getMessage());
      return null;
    }
  }

  public abstract TableHelper<? extends ProbabilityTable> getHelper();
}
