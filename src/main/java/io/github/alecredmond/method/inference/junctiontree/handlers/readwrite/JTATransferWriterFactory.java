package io.github.alecredmond.method.inference.junctiontree.handlers.readwrite;

import io.github.alecredmond.application.inference.junctiontree.Clique;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.application.probabilitytables.probabilityvector.VectorCombinationKey;
import io.github.alecredmond.method.probabilitytables.probabilityvector.VectorCombinationKeyFactory;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class JTATransferWriterFactory {

  public JTATransferWriter build(Clique read, Clique write) {
    return buildRatioWriter(read.getTable(), write.getTable());
  }

  public JTATransferWriter buildRatioWriter(
      ProbabilityTable readTable, ProbabilityTable writeTable) {
    Set<Node> sharedNodes = findSharedNodes(readTable.getNodes(), writeTable.getNodes());

    VectorCombinationKeyFactory keyFactory = new VectorCombinationKeyFactory();
    VectorCombinationKey readKey = keyFactory.buildReadWriteKey(readTable, sharedNodes);
    VectorCombinationKey writeKey = keyFactory.buildReadWriteKey(writeTable, sharedNodes);

    JTAReadWriteSynchronizer synchronizer = new JTAReadWriteSynchronizer();
    JTAReader reader = new JTAReader(synchronizer, readTable.getVector(), readKey);
    JTAWriterMessagePass writer = new JTAWriterMessagePass(synchronizer, writeTable.getVector(), writeKey);
    return new JTATransferWriter(reader, writer,synchronizer);
  }

  private Set<Node> findSharedNodes(Set<Node> readTableNodes, Set<Node> writeTableNodes) {
    return readTableNodes.stream()
        .filter(writeTableNodes::contains)
        .collect(Collectors.toSet());
  }

  public JTATransferWriter buildCopyInWriter(
      Set<Node> readTableNodes,
      Set<Node> writeTableNodes,
      ProbabilityVector readVector,
      ProbabilityVector writeVector) {
    Set<Node> sharedNodes = findSharedNodes(readTableNodes, writeTableNodes);

    VectorCombinationKeyFactory keyFactory = new VectorCombinationKeyFactory();
    VectorCombinationKey readKey = keyFactory.buildReadWriteKey(readVector, sharedNodes);
    VectorCombinationKey writeKey = keyFactory.buildReadWriteKey(writeVector, sharedNodes);

    JTAReadWriteSynchronizer synchronizer = new JTAReadWriteSynchronizer();
    JTAReader reader = new JTAReader(synchronizer, readVector, readKey);
    JTAWriterMessagePass writer = new JTAWriterMultiplyIn(synchronizer, writeVector, writeKey);
    return new JTATransferWriter(reader, writer,synchronizer);
  }

  public JTATransferWriter buildCopyInWriter(
      ProbabilityTable readTable, ProbabilityTable writeTable) {
    Set<Node> sharedNodes = findSharedNodes(readTable.getNodes(), writeTable.getNodes());

    VectorCombinationKeyFactory keyFactory = new VectorCombinationKeyFactory();
    VectorCombinationKey readKey = keyFactory.buildReadWriteKey(readTable, sharedNodes);
    VectorCombinationKey writeKey = keyFactory.buildReadWriteKey(writeTable, sharedNodes);

    JTAReadWriteSynchronizer synchronizer = new JTAReadWriteSynchronizer();
    JTAReader reader = new JTAReader(synchronizer, readTable.getVector(), readKey);
    JTAWriterMessagePass writer = new JTAWriterMultiplyIn(synchronizer, writeTable.getVector(), writeKey);
    return new JTATransferWriter(reader, writer,synchronizer);
  }
}
