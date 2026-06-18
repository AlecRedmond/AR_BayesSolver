package io.github.alecredmond.export.application.network;

import static io.github.alecredmond.internal.method.node.NodeUtils.formatIDsToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NonNull;

@Data
public class NetworkBuilderNode {
  private final Serializable nodeId;
  private final List<? extends Serializable> stateIds;
  private final List<? extends Serializable> parentNodeIds;
  private final List<? extends Serializable> cptNodeOrder;
  private final double[] cptValues;

  public <T extends Serializable> NetworkBuilderNode(@NonNull T nodeId, @NonNull List<T> stateIds) {
    assertStateIdsNotEmpty(nodeId, stateIds);
    this.nodeId = nodeId;
    this.stateIds = stateIds;
    this.parentNodeIds = null;
    this.cptNodeOrder = null;
    this.cptValues = null;
  }

  private static <T extends Serializable> void assertStateIdsNotEmpty(T id, List<T> stateIds) {
    if (!stateIds.isEmpty()) return;
    throw new IllegalArgumentException("STATE IDs EMPTY FOR NODE ID : %s".formatted(id));
  }

  public <T extends Serializable> NetworkBuilderNode(T id, List<T> stateIds, double[] cptValues) {
    assertStateIdsNotEmpty(id, stateIds);
    this.nodeId = id;
    this.stateIds = stateIds;
    this.parentNodeIds = List.of();
    this.cptNodeOrder = List.of(nodeId);
    this.cptValues = cptValues;
  }

  public <T extends Serializable> NetworkBuilderNode(
      @NonNull T nodeId, @NonNull List<T> stateIds, @NonNull List<T> parentNodeIds) {
    assertStateIdsNotEmpty(nodeId, stateIds);
    this.nodeId = nodeId;
    this.stateIds = stateIds;
    this.parentNodeIds = parentNodeIds;
    this.cptNodeOrder = null;
    this.cptValues = null;
  }

  public <T extends Serializable> NetworkBuilderNode(
      @NonNull T nodeId,
      @NonNull List<T> stateIds,
      @NonNull List<T> cptNodeOrder,
      @NonNull double[] cptValues) {
    assertStateIdsNotEmpty(nodeId, stateIds);
    this.nodeId = nodeId;
    this.stateIds = stateIds;
    this.parentNodeIds = buildParentIds(nodeId, cptNodeOrder);
    this.cptNodeOrder = cptNodeOrder;
    this.cptValues = cptValues;
  }

  private <T extends Serializable> List<T> buildParentIds(T id, List<T> cptStrideOrderDesc) {
    List<T> parents = new ArrayList<>(cptStrideOrderDesc);
    if (parents.remove(id)) {
      return parents.isEmpty() ? null : parents;
    }
    throw new IllegalArgumentException(
        "cptNodeOrder list {%s} does not contain node id {%s}!"
            .formatted(formatIDsToString(cptStrideOrderDesc), id));
  }
}
