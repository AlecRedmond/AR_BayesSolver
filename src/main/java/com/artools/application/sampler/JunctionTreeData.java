package com.artools.application.sampler;

import com.artools.application.constraints.ParameterConstraint;
import com.artools.application.network.BayesNetData;
import com.artools.application.node.Node;
import com.artools.application.probabilitytables.JunctionTreeTable;
import com.artools.application.probabilitytables.MarginalTable;
import com.artools.application.probabilitytables.ProbabilityTable;
import com.artools.method.jtahandlers.ConstraintHandler;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class JunctionTreeData {
  private final BayesNetData bayesNetData;
  private final Set<Clique> cliqueSet;
  private final Set<Separator> separators;
  private final Set<Clique> leafCliques;
  private final Map<Clique, Set<ProbabilityTable>> associatedTables;
  private final List<JunctionTreeTable> junctionTreeTables;
  private final Map<ParameterConstraint, Clique> cliqueForConstraint;
  private final Map<ParameterConstraint, ConstraintHandler> constraintHandlers;

  public List<Node> getNodes() {
    return bayesNetData.getNodes();
  }

  public Map<Node, MarginalTable> getObservationMap() {
    return bayesNetData.getObservationMap();
  }

  public List<ParameterConstraint> getConstraints() {
    return bayesNetData.getConstraints();
  }
}
