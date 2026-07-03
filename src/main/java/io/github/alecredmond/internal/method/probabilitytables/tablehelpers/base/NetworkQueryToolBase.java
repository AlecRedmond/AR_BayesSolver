package io.github.alecredmond.internal.method.probabilitytables.tablehelpers.base;

import io.github.alecredmond.export.application.probabilitytables.NetworkTable;
import io.github.alecredmond.export.application.probabilitytables.cptentry.CptEntry;
import io.github.alecredmond.export.application.probabilitytables.cptentry.CptRow;
import io.github.alecredmond.internal.method.vectoriterator.misciterators.CptConditionIterator;
import java.util.*;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public abstract class NetworkQueryToolBase<T extends NetworkTable> extends QueryToolBase<T> {
  private final CptConditionIterator conditionIterator;

  protected NetworkQueryToolBase(T table) {
    super(table);
    this.conditionIterator = new CptConditionIterator(table);
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
