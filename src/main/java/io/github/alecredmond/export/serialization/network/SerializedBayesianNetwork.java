package io.github.alecredmond.export.serialization.network;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.serialization.constraint.SerializedProbabilityConstraint;
import io.github.alecredmond.export.serialization.node.SerializedNode;
import io.github.alecredmond.export.serialization.probabilitytable.SerializedNetworkTable;
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
  private Map<Serializable, SerializedNetworkTable> serializedCptMap;
  private List<SerializedProbabilityConstraint<ProbabilityConstraint>> serializedConstraints;
  private boolean solved;
}
