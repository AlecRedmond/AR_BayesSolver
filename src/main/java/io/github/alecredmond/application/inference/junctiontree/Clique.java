package io.github.alecredmond.application.inference.junctiontree;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.application.probabilitytables.MarginalTable;
import io.github.alecredmond.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.method.inference.junctiontree.handlers.JTATableHandler;
import io.github.alecredmond.method.inference.junctiontree.handlers.readwrite.JTAMessagePasser;
import java.util.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(exclude = {"separatorMap"})
public class Clique {
  private Set<Node> nodes;
  private JunctionTreeTable table;
  private JTATableHandler handler;
  private Map<Clique, JTAMessagePasser> separatorMap;
  private List<JTAMessagePasser> initializeFrom;
  private Map<ProbabilityTable, JTAMessagePasser> networkWriteMap;
  private Map<MarginalTable, JTAMessagePasser> observationWriteMap;

  public Clique(Set<Node> nodes, JunctionTreeTable table) {
    this.nodes = nodes;
    this.table = table;
    this.handler = new JTATableHandler(table);
    this.separatorMap = new HashMap<>();
    this.initializeFrom = new ArrayList<>();
    this.networkWriteMap = new HashMap<>();
    this.observationWriteMap = new HashMap<>();
  }

  public JTAMessagePasser getSeparator(Clique clique) {
    return separatorMap.get(clique);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Clique : ");
    nodes.forEach(node -> sb.append(node.toString()).append(" "));
    return sb.toString();
  }
}
