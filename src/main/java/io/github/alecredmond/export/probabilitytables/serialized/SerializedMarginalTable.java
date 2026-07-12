package io.github.alecredmond.export.probabilitytables.serialized;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SerializedMarginalTable extends SerializedNetworkTable implements Serializable {
  private Serializable networkNodeId;
}
