package io.github.alecredmond.export.probabilitytables.serialized;

import java.io.Serializable;
import java.util.*;
import lombok.Data;

@Data
public abstract class SerializedNetworkTable implements Serializable {
  private SerializedProbabilityVector vectorSTO;
  private List<Serializable> nodeIds;
  private List<Serializable> eventNodeIds;
  private List<Serializable> conditionNodeIds;
  private Serializable tableName;
}
