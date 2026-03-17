package io.github.alecredmond.export.serialization.network;

import io.github.alecredmond.export.serialization.constraint.SerializedProbabilityConstraint;
import io.github.alecredmond.export.serialization.node.SerializedNode;
import io.github.alecredmond.export.serialization.probabilitytable.SerializedMarginalTable;
import io.github.alecredmond.export.serialization.probabilitytable.SerializedProbabilityTable;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
public class SerializedBayesNetData implements Serializable {
  private String networkName;
  private List<SerializedNode> serializedNodes;
  private Map<Serializable, SerializedProbabilityTable> networkTableStoMap;
  private Map<Serializable, SerializedMarginalTable> observedTableStoMap;
  private List<SerializedProbabilityConstraint> constraintStos;
  private boolean solved;
}
