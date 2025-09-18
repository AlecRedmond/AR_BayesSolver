package com.artools.application.network;

import com.artools.application.constraints.ParameterConstraint;
import com.artools.application.node.Node;
import com.artools.application.probabilitytables.GradientTable;
import com.artools.application.probabilitytables.LogitTable;
import com.artools.application.probabilitytables.MarginalTable;
import com.artools.application.probabilitytables.ProbabilityTable;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class GradientDescentData {
  private final BayesNetData bayesNetData;
  private final Map<Node, MarginalTable> functionTables;
  private final Map<Node, Boolean> functionTablesSolved;
  private final Map<Node, GradientTable> gradientsMap;
  private final Map<Node, LogitTable> logitTableMap;
  private double lastError;
  private double lastLoss;

  public GradientDescentData(
      BayesNetData data,
      Map<Node, MarginalTable> functionTables,
      Map<Node, Boolean> functionTablesSolved,
      Map<Node, GradientTable> gradientsMap,
      Map<Node, LogitTable> logitTableMap,
      double lastError,
      double lastLoss) {
    this.bayesNetData = data;
    this.functionTables = functionTables;
    this.functionTablesSolved = functionTablesSolved;
    this.gradientsMap = gradientsMap;
    this.logitTableMap = logitTableMap;
    this.lastError = lastError;
    this.lastLoss = lastLoss;
  }

  public List<Node> getNodes() {
    return bayesNetData.getNodes();
  }

  public Map<Node, ProbabilityTable> getNetworkTablesMap() {
    return bayesNetData.getNetworkTablesMap();
  }

  public List<ParameterConstraint> getConstraints(){
      return bayesNetData.getConstraints();
  }
}
