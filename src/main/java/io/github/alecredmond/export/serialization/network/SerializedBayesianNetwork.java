package io.github.alecredmond.export.serialization.network;

import io.github.alecredmond.export.serialization.constraint.SerializedProbabilityConstraint;
import io.github.alecredmond.export.serialization.node.SerializedNode;
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
public class SerializedBayesianNetwork implements Serializable {
  private String networkName;
  private List<SerializedNode> serializedNodes;
  private Map<Serializable, SerializedProbabilityTable> serializedCptMap;
  private List<SerializedProbabilityConstraint> serializedProbabilityConstraints;
  private boolean solved;
}
