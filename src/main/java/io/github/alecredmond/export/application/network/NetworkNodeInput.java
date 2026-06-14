package io.github.alecredmond.export.application.network;

import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.NonNull;

@Data
public class NetworkNodeInput {
  private Serializable id;
  private List<Serializable> stateIds;
  private List<Serializable> parentIds = null;
  private Serializable[] cptStrideOrderIds = null;
  private double[] cptValues = null;

  public NetworkNodeInput(@NonNull Serializable id, @NonNull List<Serializable> stateIds) {
    checkStateIds(id, stateIds);
    this.id = id;
    this.stateIds = stateIds;
  }

  private static void checkStateIds(Serializable id, List<Serializable> stateIds) {
    if (stateIds.isEmpty()) {
      throw new IllegalArgumentException("STATE IDs EMPTY FOR NODE ID : %s".formatted(id));
    }
  }

  public NetworkNodeInput(
      @NonNull Serializable id,
      @NonNull List<Serializable> stateIds,
      @NonNull List<Serializable> parentIds) {
    checkStateIds(id, stateIds);
    this.id = id;
    this.stateIds = stateIds;
    this.parentIds = parentIds;
  }

  public NetworkNodeInput(
      @NonNull Serializable id,
      @NonNull List<Serializable> stateIds,
      @NonNull List<Serializable> parentIds,
      @NonNull Serializable[] cptStrideOrderIds,
      @NonNull double[] cptValues) {
    checkStateIds(id, stateIds);
    this.id = id;
    this.stateIds = stateIds;
    this.parentIds = parentIds;
    this.cptStrideOrderIds = cptStrideOrderIds;
    this.cptValues = cptValues;
  }
}
