package io.github.alecredmond.internal.application.inference.junctiontree;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.internal.application.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.method.probabilitytables.tablehelpers.JunctionTreeTableHelper;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.TableTransfer;
import java.util.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Clique {
  @EqualsAndHashCode.Include private final Set<Node> nodes;
  private final JunctionTreeTable table;
  private JunctionTreeTableHelper handler;
  private Map<Clique, Separator> separatorMap;
  private List<TableTransfer> writeFromCPTs;
  private List<TableTransfer> writeToCPTs;
  private List<TableTransfer> writeToObserved;

  public Clique(Set<Node> nodes, JunctionTreeTable table) {
    this.nodes = nodes;
    this.table = table;
    this.handler = table.getHelper();
    this.separatorMap = new HashMap<>();
    this.writeFromCPTs = new ArrayList<>();
    this.writeToCPTs = new ArrayList<>();
    this.writeToObserved = new ArrayList<>();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Clique : ");
    nodes.forEach(node -> sb.append(node.toString()).append(" "));
    return sb.toString();
  }
}
