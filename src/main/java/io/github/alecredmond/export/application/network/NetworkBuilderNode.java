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
  private final List<? extends Serializable> cptStrideOrderDesc;
  private final double[] cptValues;

  public <T extends Serializable> NetworkBuilderNode(@NonNull T nodeId, @NonNull List<T> stateIds) {
    assertStateIdsNotEmpty(nodeId, stateIds);
    this.nodeId = nodeId;
    this.stateIds = stateIds;
    this.parentNodeIds = null;
    this.cptStrideOrderDesc = null;
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
    this.cptStrideOrderDesc = List.of(nodeId);
    this.cptValues = cptValues;
  }

  public <T extends Serializable> NetworkBuilderNode(
      @NonNull T nodeId, @NonNull List<T> stateIds, @NonNull List<T> parentNodeIds) {
    assertStateIdsNotEmpty(nodeId, stateIds);
    this.nodeId = nodeId;
    this.stateIds = stateIds;
    this.parentNodeIds = parentNodeIds;
    this.cptStrideOrderDesc = null;
    this.cptValues = null;
  }

  public <T extends Serializable> NetworkBuilderNode(
      @NonNull T nodeId,
      @NonNull List<T> stateIds,
      @NonNull List<T> cptStrideOrderDesc,
      @NonNull double[] cptValues) {
    assertStateIdsNotEmpty(nodeId, stateIds);
    this.nodeId = nodeId;
    this.stateIds = stateIds;
    this.parentNodeIds = buildParentIds(nodeId, cptStrideOrderDesc);
    this.cptStrideOrderDesc = cptStrideOrderDesc;
    this.cptValues = cptValues;
  }

  private <T extends Serializable> List<T> buildParentIds(T id, List<T> cptStrideOrderDesc) {
    List<T> parents = new ArrayList<>(cptStrideOrderDesc);
    if (parents.remove(id)) {
        return parents.isEmpty() ? null : parents;
    }
    throw new IllegalArgumentException(
        "Stride Order list [%s] does not contain node id [%s]!"
            .formatted(formatIDsToString(cptStrideOrderDesc), id));
  }
}
