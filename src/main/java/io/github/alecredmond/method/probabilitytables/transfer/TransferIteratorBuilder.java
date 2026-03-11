package io.github.alecredmond.method.probabilitytables.transfer;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.probabilitytables.export.ProbabilityTable;
import io.github.alecredmond.application.probabilitytables.export.probabilityvector.ProbabilityVector;
import io.github.alecredmond.application.probabilitytables.internal.probabilityvector.TransferIteratorData;
import io.github.alecredmond.method.probabilitytables.TableUtils;
import io.github.alecredmond.method.probabilitytables.probabilityvector.VectorCombinationKeyFactory;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransferIteratorBuilder {
  private static final VectorCombinationKeyFactory KEY_FACTORY = new VectorCombinationKeyFactory();

  private TransferIteratorBuilder() {}

  public static MarginalTransferIterator buildMarginalTransferIterator(
      ProbabilityTable input, ProbabilityTable output) {
    Set<Node> common = TableUtils.getCommonNodes(input, output);
    int iterationSteps = calculateIterationSteps(common);
    return new MarginalTransferIterator(
        buildData(input, common, iterationSteps), buildData(output, common, iterationSteps));
  }

  private static int calculateIterationSteps(Set<Node> sharedNodes) {
    return sharedNodes.stream().map(node -> node.getNodeStates().size()).reduce(1, (x, y) -> x * y);
  }

  private static TransferIteratorData buildData(
      ProbabilityTable table, Set<Node> sharedNodes, int iterationSteps) {
    ProbabilityVector vector = table.getVector();
    return new TransferIteratorData(
        vector, KEY_FACTORY.buildReadWriteKey(table, sharedNodes), iterationSteps);
  }

  public static MessagePassIterator buildMessagePassIterator(
      ProbabilityTable input, ProbabilityTable output, ProbabilityTable separatorTable) {
    Set<Node> common = TableUtils.getCommonNodes(input, output);
    int iterationSteps = calculateIterationSteps(common);
    return new MessagePassIterator(
        buildData(input, common, iterationSteps),
        buildData(output, common, iterationSteps),
        separatorTable.getVector());
  }
}
