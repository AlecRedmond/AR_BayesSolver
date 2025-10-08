package io.github.alecredmond.application.sampler;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.method.sampler.jtasampler.jtahandlers.SeparatorTableHandler;
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
  private SeparatorTableHandler handler;

  public Separator(
      Clique cliqueA, Clique cliqueB, Set<Node> connectingNodes, JunctionTreeTable table) {
    this.cliqueA = cliqueA;
    this.cliqueB = cliqueB;
    this.connectingNodes = connectingNodes;
    this.table = table;
    this.handler = new SeparatorTableHandler(this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("SEPARATOR : ");
    connectingNodes.forEach(node -> sb.append(node.toString()).append(" "));
    return sb.toString();
  }
}
