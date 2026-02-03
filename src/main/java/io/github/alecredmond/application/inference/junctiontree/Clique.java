package io.github.alecredmond.application.inference.junctiontree;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.method.inference.junctiontree.handlers.JTATableHandler;
import io.github.alecredmond.method.inference.junctiontree.handlers.readwrite.JTATransferWriter;
import java.util.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(exclude = {"separatorMap"})
public class Clique {
  private Set<Node> nodes;
  private JunctionTreeTable table;
  private JTATableHandler handler;
  private Map<Clique, JTATransferWriter> separatorMap;
  private List<JTATransferWriter> initializeFrom;
  private List<JTATransferWriter> networkWriters;
  private List<JTATransferWriter> observedWriters;
  private List<JTATransferWriter> unObservedWriters;

  public Clique(Set<Node> nodes, JunctionTreeTable table) {
    this.nodes = nodes;
    this.table = table;
    this.handler = new JTATableHandler(table);
    this.separatorMap = new HashMap<>();
    this.initializeFrom = new ArrayList<>();
    this.networkWriters = new ArrayList<>();
    this.observedWriters = new ArrayList<>();
    this.unObservedWriters = new ArrayList<>();
  }

  public JTATransferWriter getSeparator(Clique clique) {
    return separatorMap.get(clique);
  }

  public List<JTATransferWriter> getCorrectObservedWriter() {
    return table.isObserved() ? observedWriters : unObservedWriters;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Clique : ");
    nodes.forEach(node -> sb.append(node.toString()).append(" "));
    return sb.toString();
  }
}
