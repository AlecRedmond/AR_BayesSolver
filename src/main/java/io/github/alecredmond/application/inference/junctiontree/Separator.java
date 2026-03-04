package io.github.alecredmond.application.inference.junctiontree;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.probabilitytables.internal.JunctionTreeTable;
import io.github.alecredmond.method.inference.junctiontree.handlers.readwrite.JTATransferWriter;
import io.github.alecredmond.method.probabilitytables.TableUtils;
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
  private Map<Clique, JTATransferWriter> inputWriters;
  private Map<Clique, JTATransferWriter> outputWriters;

  public void run(Clique start) {
    Clique end = connected.get(start);
    inputWriters.get(start).run();
    inputWriters.get(end).run();
  }

  public void reset() {
    TableUtils.setToUnity(table);
  }
}
