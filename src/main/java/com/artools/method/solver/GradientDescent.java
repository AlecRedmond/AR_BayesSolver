package com.artools.method.solver;

import com.artools.application.network.BayesNetData;
import com.artools.application.network.GradientDescentData;
import com.artools.application.node.Node;
import com.artools.application.node.NodeState;
import com.artools.application.probabilitytables.LogitTable;
import com.artools.application.probabilitytables.ProbabilityTable;
import com.artools.application.solver.SolverConfigs;
import com.artools.method.solver.netsampler.JunctionTreeAlgorithm;
import com.artools.method.solver.netsampler.NetworkSampler;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GradientDescent {
  private final SolverConfigs configs;
  private final GradientDescentData data;

  public GradientDescent(BayesNetData netData, SolverConfigs configs) {
    this.configs = configs;
    this.data = GradientDescentDataBuilder.build(netData, configs);
  }

  public void solveNetwork() {
    JunctionTreeAlgorithm jta = new JunctionTreeAlgorithm(data.getBayesNetData());
    data.setLastError(errorForConstraints(jta));
    int percentileStep = configs.getCyclesLimit() / 100;
    for (int i = 0; i < configs.getCyclesLimit(); i++) {
      double lastError = data.getLastError();
      if (endCycleLogic()) {
        logCycleComplete(i, data.getLastLoss(), data.getLastError());
        break;
      }
      runCycle(jta);
      double error = errorForConstraints(jta);
      double loss = error - lastError;
      scaleStepSize(loss);
      if (i % percentileStep == 0) logCycleComplete(i, loss, error);
      data.setLastLoss(Math.abs(loss));
      data.setLastError(error);
    }
  }

  private double errorForConstraints(NetworkSampler sampler) {
    return data.getConstraints().stream().mapToDouble(sampler::getR2error).sum();
  }

  private boolean endCycleLogic(){
      return data.getLastLoss() < configs.getConvergeThreshold();
  }

  private void logCycleComplete(int i, double loss, double error) {
    String output =
        String.format(
            "CYCLE %d : LOSS = %1.2e : ERROR = %1.2e : STEP_SIZE = %1.2e",
            i, loss, error, configs.getStepSize());
    log.info(output);
    System.out.println(output);
  }

  private void runCycle(NetworkSampler sampler) {
    // LOOP 1: Calculate the gradient for every parameter based on the current state.
    // The logits are NOT changed during this loop.
    for (Node node : data.getNodes()) {
      getRequests(node).forEach(request -> calculateGradient(node, request, sampler));
    }

    // LOOP 2: Apply the updates to all logits using the gradients calculated above.
    for (Node node : data.getNodes()) {
      getRequests(node).forEach(request -> descendGradient(node, request));
    }
  }

  private void scaleStepSize(double loss) {
    if (loss > 0) {
      configs.setStepSize(configs.getStepSize() * configs.getStepDecreaseScalar());
    } else if (Math.abs(loss) < configs.getStepIncreaseThreshold()) {
      configs.setStepSize(configs.getStepSize() * configs.getStepIncreaseScalar());
    }
  }

  private Set<Set<NodeState>> getRequests(Node node) {
    return data.getNetworkTablesMap().get(node).getProbabilitiesMap().keySet();
  }

  private void calculateGradient(Node node, Set<NodeState> request, NetworkSampler sampler) {
    double h = configs.getGradientEpsilon();

    double originalError = errorForConstraints(sampler);
    adjustLogit(node, request, h);
    double errorPlus = errorForConstraints(sampler);
    adjustLogit(node, request, -1 * h);

    double gradient = (errorPlus - originalError) / (2 * h);
    setGradient(node, request, gradient);
  }

  private void descendGradient(Node node, Set<NodeState> request) {
    double gradient = getGradient(node, request);
    double updateStep = -1.0 * configs.getStepSize() * gradient;
    adjustLogit(node, request, updateStep);
  }

  private void adjustLogit(Node node, Set<NodeState> request, double step) {
    LogitTable logitTable = data.getLogitTableMap().get(node);
    double oldLogit = logitTable.getLogit(request);
    double newLogit = oldLogit + step;
    logitTable.setLogit(request, newLogit);
    writeLogitToNetworkTable(logitTable, node, request);
  }

  private void writeLogitToNetworkTable(LogitTable logitTable, Node node, Set<NodeState> request) {

    Set<NodeState> conditionStates =
        request.stream()
            .filter(state -> node.getParents().contains(state.getParentNode()))
            .collect(Collectors.toCollection(HashSet::new));

    double softMaxDenominator =
        logitTable.getProbabilitiesMap().entrySet().stream()
            .filter(entry -> entry.getKey().containsAll(conditionStates))
            .mapToDouble(entry -> Math.exp(entry.getValue()))
            .sum();

    ProbabilityTable networkTable = data.getNetworkTablesMap().get(node);

    node.getStates()
        .forEach(
            state -> {
              conditionStates.add(state);
              double logitExp = Math.exp(logitTable.getLogit(conditionStates));
              double probability = logitExp / softMaxDenominator;
              networkTable.setProbability(conditionStates, probability);
              conditionStates.remove(state);
            });
  }

  private void setGradient(Node node, Set<NodeState> request, double newGradient) {
    data.getGradientsMap().get(node).setGradient(request, newGradient);
  }

  private double getGradient(Node node, Set<NodeState> request) {
    return data.getGradientsMap().get(node).getGradient(request);
  }
}
