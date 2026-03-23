package io.github.alecredmond.internal.application.inference.junctiontree;

import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.internal.method.inference.junctiontree.handlers.JTAConstraintHandler;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JunctionTreeData {
  private BayesianNetworkData networkData;
  private Clique[] cliques;
  private Separator[] separators;
  private Map<Clique, List<JTAConstraintHandler>> constraintHandlersMap;
  private boolean solverConfig;
  private double jointProbability;
}
