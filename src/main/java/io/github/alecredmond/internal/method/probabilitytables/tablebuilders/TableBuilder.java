package io.github.alecredmond.internal.method.probabilitytables.tablebuilders;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import java.util.List;

public interface TableBuilder<T extends ProbabilityTable> {
  T buildTable(List<Node> events, List<Node> conditions);

  T copyTable(T table);
}
