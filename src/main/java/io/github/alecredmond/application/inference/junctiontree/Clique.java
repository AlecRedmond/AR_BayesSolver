package io.github.alecredmond.application.inference.junctiontree;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.probabilitytables.internal.JunctionTreeTable;
import io.github.alecredmond.method.inference.junctiontree.handlers.JTATableHandler;
import io.github.alecredmond.method.probabilitytables.transfer.TransferIterator;
import java.util.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Clique {
  @EqualsAndHashCode.Include private final Set<Node> nodes;
  private final JunctionTreeTable table;
  private JTATableHandler handler;
  private Map<Clique, Separator> separatorMap;
  private List<TransferIterator> writeFromCPTs;
  private List<TransferIterator> writeToCPTs;
  private List<TransferIterator> writeToObserved;

  public Clique(Set<Node> nodes, JunctionTreeTable table) {
    this.nodes = nodes;
    this.table = table;
    this.handler = new JTATableHandler(table);
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
