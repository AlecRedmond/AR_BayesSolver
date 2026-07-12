package io.github.alecredmond.internal.application.probabilitytables;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ObservedTable;
import io.github.alecredmond.export.probabilitytables.ObservedTableQueryTool;
import io.github.alecredmond.internal.application.probabilitytables.base.SingleEventTable;
import io.github.alecredmond.internal.method.probabilitytables.tablebuilders.TableBuilderData;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ObservedTableImpl extends SingleEventTable<ObservedTableQueryTool>
    implements ObservedTable {
  protected Map<Node, NodeState> observations = new LinkedHashMap<>();

  public ObservedTableImpl(TableBuilderData tableBuilderData) {
    super(tableBuilderData);
  }

  @Override
  public Node getMeasuredNode() {
    return eventNode;
  }

  @Override
  public Map<Node, NodeState> getObservations() {
    return observations;
  }
}
