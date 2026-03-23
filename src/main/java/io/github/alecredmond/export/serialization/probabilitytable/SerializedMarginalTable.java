package io.github.alecredmond.export.serialization.probabilitytable;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SerializedMarginalTable extends SerializedProbabilityTable implements Serializable {
  private Serializable networkNodeId;
}
