package com.artools.application.junctiontree;

import com.artools.application.node.Node;
import com.artools.application.probabilitytables.JunctionTreeTable;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Separator {
  private Clique cliqueA;
  private Clique cliqueB;
  private Set<Node> connectingNodes;
  private JunctionTreeTable table;

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("SEPARATOR : ");
    connectingNodes.forEach(node -> sb.append(node.toString()).append(" "));
    return sb.toString();
  }
}
