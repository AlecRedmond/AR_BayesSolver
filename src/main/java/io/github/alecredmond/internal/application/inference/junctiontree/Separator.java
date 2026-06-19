package io.github.alecredmond.internal.application.inference.junctiontree;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.internal.application.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.TableTransfer;
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
  @EqualsAndHashCode.Include private Map<Clique, Clique> connected;
  private Map<Clique, TableTransfer> messagePassers;

  public void run(Clique start) {
    messagePassers.get(start).transfer();
  }

  public void resetSeparator() {
    double marginalised = 1.0 / table.getProbabilities().length;
    Arrays.fill(table.getProbabilities(), marginalised);
  }
}
