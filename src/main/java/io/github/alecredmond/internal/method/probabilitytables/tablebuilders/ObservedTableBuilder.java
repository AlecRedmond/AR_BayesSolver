package io.github.alecredmond.internal.method.probabilitytables.tablebuilders;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.probabilitytables.ObservedTable;
import io.github.alecredmond.internal.application.probabilitytables.ObservedTableImpl;
import io.github.alecredmond.internal.method.probabilitytables.tablehelpers.impl.ObservedTableQueryToolImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.SerializationUtils;

public class ObservedTableBuilder extends BaseTableBuilder implements TableBuilder<ObservedTable> {

  public ObservedTable buildTable(Node node) {
    return buildTable(List.of(node), new ArrayList<>());
  }

  @Override
  public ObservedTable buildTable(List<Node> events, List<Node> conditions) {
    return buildTable(events, conditions, ObservedTableImpl::new, ObservedTableQueryToolImpl::new);
  }

  @Override
  public ObservedTable copyTable(ObservedTable table) {
    return copyTable(table, this::buildTable, this::additionalCopyLogic);
  }

  private void additionalCopyLogic(ObservedTable original, ObservedTable copied) {
    ObservedTableImpl copiedImpl = (ObservedTableImpl) copied;
    copiedImpl.setTableName(SerializationUtils.clone(original.getTableName()));
    copiedImpl.setObservations(Map.copyOf(original.getObservations()));
  }
}
