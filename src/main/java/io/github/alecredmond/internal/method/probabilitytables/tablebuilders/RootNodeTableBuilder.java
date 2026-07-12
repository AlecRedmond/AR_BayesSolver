package io.github.alecredmond.internal.method.probabilitytables.tablebuilders;

import io.github.alecredmond.exceptions.TableBuilderException;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.probabilitytables.RootNodeTable;
import io.github.alecredmond.internal.application.probabilitytables.RootNodeTableImpl;
import io.github.alecredmond.internal.method.probabilitytables.tablehelpers.impl.RootNodeTableQueryToolImpl;

import java.util.List;

public class RootNodeTableBuilder extends BaseTableBuilder implements TableBuilder<RootNodeTable> {

  @Override
  public RootNodeTable buildTable(List<Node> events, List<Node> conditions) {
    if (!conditions.isEmpty()) {
      throw new TableBuilderException("Attempted to build a Root table with condition size != 0!");
    }
    if (events.size() != 1) {
      throw new TableBuilderException("Attempted to build a Root table with more than 1 event!");
    }
    return buildTable(events, conditions, RootNodeTableImpl::new, RootNodeTableQueryToolImpl::new);
  }

  @Override
  public RootNodeTable copyTable(RootNodeTable table) {
    return copyTable(table, this::buildTable);
  }
}
