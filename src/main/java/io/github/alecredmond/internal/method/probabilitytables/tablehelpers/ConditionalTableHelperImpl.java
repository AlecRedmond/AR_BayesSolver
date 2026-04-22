package io.github.alecredmond.internal.method.probabilitytables.tablehelpers;

import io.github.alecredmond.exceptions.NodeStateConflictException;
import io.github.alecredmond.exceptions.ProbabilityTableRequestException;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ConditionalTable;
import io.github.alecredmond.export.method.probabilitytables.ConditionalTableHelper;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.iteratorfactory.TableMarginalizerFactory;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.TableMarginalizer;
import java.io.Serializable;
import java.util.*;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConditionalTableHelperImpl extends TableHelperBase<ConditionalTable>
    implements ConditionalTableHelper {
  private final TableMarginalizer marginalizer;

  public ConditionalTableHelperImpl(ConditionalTable table) {
    super(table);
    this.marginalizer = new TableMarginalizerFactory().build(table);
  }

  @Override
  public void marginalizeTable() {
    marginalizer.performRun();
  }

  @Override
  public Map<NodeState, Double> getConditionalProbByIds(Collection<Serializable> conditionIDs) {
    Collection<NodeState> states;
    try {
      states = TableUtils.assertAllIdsPresent(conditionIDs, table.getConditions(), table);
    } catch (ProbabilityTableRequestException e) {
      log.error(e.getMessage());
      return new HashMap<>();
    }
    return getConditionalProb(states);
  }

  @Override
  public Map<NodeState, Double> getConditionalProb(Collection<NodeState> conditionStates) {
    try {
      TableUtils.assertAllNodesPresent(conditionStates, table.getConditions());
      Map<NodeState, Double> map = new LinkedHashMap<>();
      List<NodeState> events = table.getNetworkNode().getNodeStates();
      double[] probs = table.getVector().getProbabilities();
      int firstIndex = TableUtils.getIndex(conditionStates, table);
      IntStream.range(firstIndex, firstIndex + events.size())
          .forEach(i -> map.put(events.get(i), probs[i]));
      return map;
    } catch (NodeStateConflictException | ProbabilityTableRequestException e) {
      log.error(e.getMessage());
      return new HashMap<>();
    }
  }
}
