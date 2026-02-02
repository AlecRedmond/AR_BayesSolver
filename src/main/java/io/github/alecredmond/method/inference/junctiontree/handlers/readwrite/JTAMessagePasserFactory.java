package io.github.alecredmond.method.inference.junctiontree.handlers.readwrite;

import io.github.alecredmond.application.inference.junctiontree.Clique;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.application.probabilitytables.probabilityvector.VectorCombinationKey;
import io.github.alecredmond.method.probabilitytables.probabilityvector.VectorCombinationKeyFactory;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class JTAMessagePasserFactory {

  public JTAMessagePasser build(Clique read, Clique write) {
    return build(read.getTable(), write.getTable());
  }

  public JTAMessagePasser build(ProbabilityTable readTable, ProbabilityTable writeTable) {
    Set<Node> sharedNodes = findSharedNodes(readTable, writeTable);

    VectorCombinationKeyFactory keyFactory = new VectorCombinationKeyFactory();
    VectorCombinationKey readKey = keyFactory.buildReadWriteKey(readTable, sharedNodes);
    VectorCombinationKey writeKey = keyFactory.buildReadWriteKey(writeTable, sharedNodes);

    JTAReadWriteSynchronizer synchronizer = new JTAReadWriteSynchronizer();
    JTAReader reader = new JTAReader(synchronizer, readTable.getVector(), readKey);
    JTAWriter writer = new JTAWriter(synchronizer, writeTable.getVector(), writeKey);
    return new JTAMessagePasser(reader, writer);
  }

  private Set<Node> findSharedNodes(ProbabilityTable tableA, ProbabilityTable tableB) {
    return tableA.getNodes().stream()
        .filter(tableB.getNodes()::contains)
        .collect(Collectors.toSet());
  }
}
