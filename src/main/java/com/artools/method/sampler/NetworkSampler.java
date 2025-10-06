package com.artools.method.sampler;

import com.artools.application.network.BayesianNetworkData;
import com.artools.application.node.Node;
import com.artools.application.node.NodeState;
import com.artools.application.solver.SolverConfigs;
import com.artools.method.probabilitytables.TableUtils;
import com.artools.method.sampler.jtasampler.JunctionTreeAlgorithm;
import com.artools.method.solver.BayesSolver;
import java.util.*;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NetworkSampler {
  private final BayesianNetworkData networkData;
  private final SolverConfigs configs;
  private JunctionTreeAlgorithm jta;

  public NetworkSampler(BayesianNetworkData networkData, SolverConfigs configs) {
    this.networkData = networkData;
    this.configs = configs;
    this.jta = null;
  }

  public <T> List<List<T>> generateSamples(
      int numberOfSamples,
      Set<Node> excludeNodes,
      Set<Node> includeNodes,
      Class<T> tClass) {
    return new SampleCreator<>(networkData, tClass)
        .generateSamples(networkData.getObservedStatesMap(), excludeNodes, includeNodes, numberOfSamples);
  }

  public double observeProbability(Set<NodeState> eventStates) {
    return eventStates.stream()
        .mapToDouble(
            state ->
                networkData
                    .getObservationMap()
                    .get(state.getParentNode())
                    .getProbability(Set.of(state)))
        .reduce(1.0, (a, b) -> a * b);
  }

  public void observeNetwork(Collection<NodeState> observed) {
    if (!networkData.isSolved()) runSolver();
    jta.sampleNetwork(convertToMap(observed));
  }

  public void runSolver() {
    new BayesSolver(networkData, configs).solveNetwork();
    writeProbabilityTables();
    networkData.setSolved(true);
    jta = new JunctionTreeAlgorithm(networkData);
  }

  public Map<Node, NodeState> convertToMap(Collection<NodeState> evidenceStates) {
    Map<Node, NodeState> evidence = new HashMap<>();
    evidenceStates.forEach(
        state -> {
          checkNoDuplicates(evidence, state);
          evidence.put(state.getParentNode(), state);
        });
    return evidence;
  }

  private void writeProbabilityTables() {
    Stream.concat(
            networkData.getNetworkTablesMap().values().stream(),
            networkData.getObservationMap().values().stream())
        .forEach(TableUtils::writeProbabilityMap);
  }

  private void checkNoDuplicates(Map<Node, NodeState> evidence, NodeState state) {
    if (evidence.containsKey(state.getParentNode())) {
      throw new IllegalArgumentException("Tried to observe multiple NodeStates on the same node!");
    }
  }
}
