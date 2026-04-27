package io.github.alecredmond.internal.method.probabilitytables.tablehelpers;

import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.MarginalTable;
import io.github.alecredmond.export.method.probabilitytables.MarginalTableHelper;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MarginalTableHelperImpl extends TableHelperBase<MarginalTable>
    implements MarginalTableHelper {

  public MarginalTableHelperImpl(MarginalTable table) {
    super(table);
  }

  @Override
  public void marginalizeTable() {
    TableUtils.marginalizeJointTable(table);
  }

  @Override
  public Map<NodeState, Double> getConditionalProb(Collection<NodeState> condition) {
    return buildProbabilityMap();
  }

  @Override
  public Map<NodeState, Double> getConditionalProbByIds(Collection<Serializable> conditionIDs) {
    return buildProbabilityMap();
  }

  @Override
  public Map<NodeState, Double> buildProbabilityMap() {
    List<NodeState> states = table.getNetworkNode().getNodeStates();
    double[] prob = table.getVector().getProbabilities();
    return IntStream.range(0, prob.length)
        .mapToObj(i -> Map.entry(states.get(i), prob[i]))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  @Override
  public Double getProbability(NodeState state) {
    return super.getProbability(List.of(state));
  }

  @Override
  public Double getProbabilityById(Serializable id) {
    return super.getProbabilityFromIDs(List.of(id));
  }

  @Override
  public boolean setProbability(NodeState state, double probability) {
    return super.setProbability(List.of(state), probability);
  }

  @Override
  public boolean setProbabilityById(Serializable id, double probability) {
    return super.setProbabilityById(List.of(id), probability);
  }
}
