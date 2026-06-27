package io.github.alecredmond.internal.application.network;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.probabilitytables.ConditionalTable;
import io.github.alecredmond.export.application.probabilitytables.RootNodeTable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class CptMapperData {
  private final List<ProbabilityConstraint> allConstraints;
  private final Map<Node, RootNodeTable> rootNodeTableMap;
  private final Map<Node, ConditionalTable> conditionalTableMap;
  private Map<Node, List<ProbabilityConstraint>> cptConstraints;
  private List<Node> directInputSuccess;

  public CptMapperData(BayesianNetworkData networkData) {
    this.allConstraints = networkData.getConstraints();
    this.rootNodeTableMap = new HashMap<>();
    this.conditionalTableMap = new HashMap<>();
    reset();
  }

  public void reset() {
    this.cptConstraints = new HashMap<>();
    this.directInputSuccess = new ArrayList<>();
  }
}
