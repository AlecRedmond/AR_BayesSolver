package io.github.alecredmond.application.sampler;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.method.sampler.jtasampler.jtahandlers.JunctionTableHandler;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(exclude = {"separatorMap"})
public class Clique {
  private Set<Node> nodes;
  private JunctionTreeTable table;
  private JunctionTableHandler handler;
  private Map<Clique, Separator> separatorMap;

  public Clique(Set<Node> nodes, JunctionTreeTable table) {
    this.nodes = nodes;
    this.table = table;
    this.handler = new JunctionTableHandler(table);
    this.separatorMap = new HashMap<>();
  }

  public Set<Separator> getSeparators() {
    return new HashSet<>(separatorMap.values());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Clique : ");
    nodes.forEach(node -> sb.append(node.toString()).append(" "));
    return sb.toString();
  }
}
