package com.artools.method.solver;

import com.artools.application.constraints.ParameterConstraint;
import com.artools.application.constraints.SumToOneConstraint;
import com.artools.application.network.BayesNetData;
import com.artools.application.network.GradientDescentData;
import com.artools.application.node.Node;
import com.artools.application.probabilitytables.GradientTable;
import com.artools.application.probabilitytables.LogitTable;
import com.artools.application.probabilitytables.MarginalTable;
import com.artools.application.probabilitytables.ProbabilityTable;
import com.artools.application.solver.SolverConfigs;
import com.artools.method.probabilitytables.TableBuilder;
import java.util.*;
import java.util.stream.Collectors;

public class GradientDescentDataBuilder {

  private GradientDescentDataBuilder() {}

  public static GradientDescentData build(BayesNetData data, SolverConfigs solverConfigs) {
    addSumToOneConstraints(data);
    Map<Node, MarginalTable> functionTables = buildFunctionTables(data.getNodes());
    Map<Node, Boolean> functionTablesCalculated = buildFunctionTablesCalculated(data.getNodes());
    Map<Node, GradientTable> gradients = buildGradients(data.getNetworkTablesMap());
    Map<Node, LogitTable> logitTableMap = buildLogitTableMap(data.getNetworkTablesMap());
    double lastError = 0.0;
    double convergence = Double.MAX_VALUE;
    return new GradientDescentData(
        data,
        functionTables,
        functionTablesCalculated,
        gradients,
        logitTableMap,
        lastError,
        convergence);
  }

  private static void addSumToOneConstraints(BayesNetData data) {
    List<ParameterConstraint> parameterConstraints = data.getConstraints();
    data.getNodes().stream()
        .findAny()
        .map(SumToOneConstraint::new)
        .ifPresent(parameterConstraints::add);
  }

  private static Map<Node, MarginalTable> buildFunctionTables(List<Node> nodes) {
    return nodes.stream()
        .map(n -> Map.entry(n, TableBuilder.buildTable(n)))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static Map<Node, Boolean> buildFunctionTablesCalculated(List<Node> nodes) {
    return nodes.stream()
        .map(n -> Map.entry(n, false))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static Map<Node, GradientTable> buildGradients(
      Map<Node, ProbabilityTable> networkTablesMap) {
    return networkTablesMap.entrySet().stream()
        .map(
            entry ->
                Map.entry(
                    entry.getKey(), TableBuilder.buildGradientTable(entry.getValue())))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static Map<Node, LogitTable> buildLogitTableMap(
      Map<Node, ProbabilityTable> networkTablesMap) {
    return networkTablesMap.entrySet().stream()
        .map(entry -> Map.entry(entry.getKey(), TableBuilder.createLogitTable(entry.getValue())))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }
}
