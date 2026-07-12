package io.github.alecredmond.internal.method.probabilitytables.tablebuilders;

import io.github.alecredmond.exceptions.TableBuilderException;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.probabilitytables.ConditionalTable;
import io.github.alecredmond.internal.application.probabilitytables.ConditionalTableImpl;
import io.github.alecredmond.internal.method.probabilitytables.tablehelpers.impl.ConditionalTableQueryToolImpl;

import java.util.List;

public class ConditionalTableBuilder extends BaseTableBuilder
    implements TableBuilder<ConditionalTable> {
  @Override
  public ConditionalTable buildTable(List<Node> events, List<Node> conditions) {
    if (events.size() != 1) {
      throw new TableBuilderException(
          "Attempted to build a Conditional table with more than 1 event!");
    }
    return buildTable(
        events, conditions, ConditionalTableImpl::new, ConditionalTableQueryToolImpl::new);
  }

  @Override
  public ConditionalTable copyTable(ConditionalTable table) {
    return copyTable(table, this::buildTable);
  }
}
