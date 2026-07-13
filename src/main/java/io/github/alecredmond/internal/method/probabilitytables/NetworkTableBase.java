package io.github.alecredmond.internal.method.probabilitytables;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.probabilitytables.cptentry.CptEntry;
import io.github.alecredmond.export.probabilitytables.cptentry.CptRow;
import io.github.alecredmond.internal.application.probabilitytables.base.SingleEventTableData;
import io.github.alecredmond.internal.method.vectoriterator.misciterators.CptConditionIterator;
import java.util.*;
import java.util.function.Consumer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public abstract class NetworkTableBase<D extends SingleEventTableData>
    extends ProbabilityTableBase<D> {
  private final CptConditionIterator conditionIterator;

  protected NetworkTableBase(D tableData) {
    super(tableData);
    this.conditionIterator = supplyConditionIterator();
  }

  protected abstract CptConditionIterator supplyConditionIterator();

  public Node getNetworkNode() {
    return tableData.getEventNode();
  }

  public List<CptEntry> getCptEntries() {
    List<CptEntry> entries = new ArrayList<>();
    iterateOverConditions(cptRow -> entries.addAll(cptRow.rowEntries()));
    return entries;
  }

  public void iterateOverConditions(Consumer<CptRow> conditionalRowConsumer) {
    conditionIterator.iterateConditions(conditionalRowConsumer, List.of());
  }
}
