package io.github.alecredmond.export.serialization.probabilitytable;

import java.io.Serializable;
import java.util.*;

import io.github.alecredmond.export.serialization.probabilitytable.probabilityvector.SerializedProbabilityVector;
import lombok.Data;

@Data
public abstract class SerializedNetworkTable implements Serializable {
  private SerializedProbabilityVector vectorSTO;
  private List<Serializable> nodeIds;
  private List<Serializable> eventNodeIds;
  private List<Serializable> conditionNodeIds;
  private Serializable tableName;
}
