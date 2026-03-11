package io.github.alecredmond.application.inference.junctiontree;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.probabilitytables.internal.JunctionTreeTable;
import io.github.alecredmond.method.probabilitytables.transfer.TransferIterator;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
public class Separator {
  @EqualsAndHashCode.Include private Set<Node> nodes;
  private JunctionTreeTable table;
  private Map<Clique, Clique> connected;
  private Map<Clique, TransferIterator> messagePassers;

  public void run(Clique start) {
    messagePassers.get(start).transfer();
  }

  public void setToUnity() {
    Arrays.fill(table.getVector().getProbabilities(), 1.0);
  }
}
