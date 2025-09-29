package com.artools.application.sampler;

import com.artools.application.node.Node;
import com.artools.application.probabilitytables.JunctionTreeTable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.artools.method.indexer.TableIndexer;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(exclude = {"separatorMap"})
public class Clique {
  private Set<Node> nodes;
  private JunctionTreeTable table;
  private TableIndexer indexer;
  private Map<Clique, Separator> separatorMap;


  public Clique(Set<Node> nodes,JunctionTreeTable table) {
    this.nodes = nodes;
    this.table = table;
    this.indexer = new TableIndexer(table);
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
