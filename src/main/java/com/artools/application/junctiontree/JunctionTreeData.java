package com.artools.application.junctiontree;

import com.artools.application.constraints.ParameterConstraint;
import com.artools.application.network.BayesNetData;
import com.artools.application.node.Node;
import com.artools.application.probabilitytables.JunctionTreeTable;
import com.artools.application.probabilitytables.MarginalTable;
import com.artools.application.probabilitytables.ProbabilityTable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;

@Getter
public class JunctionTreeData {
  private final BayesNetData bayesNetData;
  private final Set<Clique> cliqueSet;
  private final Set<Separator> separators;
  private final Set<Clique> leafCliques;
  private final Map<Clique, Set<ProbabilityTable>> associatedTables;
  private final List<JunctionTreeTable> junctionTreeTables;

  public JunctionTreeData(
      BayesNetData bayesNetData,
      Set<Clique> cliqueSet,
      Set<Separator> separators,
      Set<Clique> leafCliques,
      Map<Clique, Set<ProbabilityTable>> associatedTables,
      List<JunctionTreeTable> junctionTreeTables) {
    this.bayesNetData = bayesNetData;
    this.cliqueSet = cliqueSet;
    this.separators = separators;
    this.leafCliques = leafCliques;
    this.associatedTables = associatedTables;
    this.junctionTreeTables = junctionTreeTables;
  }

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
