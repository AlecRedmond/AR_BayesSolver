package io.github.alecredmond.internal.method.probabilitytables.transfer;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.application.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.TransferIteratorData;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.VectorCombinationKeyFactory;
import java.util.*;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class TransferIteratorBuilder {
  private static final VectorCombinationKeyFactory KEY_FACTORY = new VectorCombinationKeyFactory();

  public MarginalTransferIterator buildMarginalTransferIterator(
      ProbabilityTable input, ProbabilityTable output) {
    Set<Node> common = TableUtils.getCommonNodes(input, output);
    int iterationSteps = calculateIterationSteps(common);
    return new MarginalTransferIterator(
        buildData(input, common, iterationSteps), buildData(output, common, iterationSteps));
  }

  private int calculateIterationSteps(Set<Node> sharedNodes) {
    return sharedNodes.stream().map(node -> node.getNodeStates().size()).reduce(1, (x, y) -> x * y);
  }

  private TransferIteratorData buildData(
      ProbabilityTable table, Set<Node> sharedNodes, int iterationSteps) {
    ProbabilityVector vector = table.getVector();
    return new TransferIteratorData(
        vector, KEY_FACTORY.buildReadWriteKey(table, sharedNodes), iterationSteps);
  }

  public MultiplyTransferIterator buildMultiplyTransferIterator(
      ProbabilityTable networkTable, JunctionTreeTable cliqueTable) {
    Set<Node> common = TableUtils.getCommonNodes(networkTable, cliqueTable);
    int iterationSteps = calculateIterationSteps(common);
    return new MultiplyTransferIterator(
        buildData(networkTable, common, iterationSteps),
        buildData(cliqueTable, common, iterationSteps));
  }

  public MessagePassIterator buildMessagePassIterator(
      ProbabilityTable input, ProbabilityTable output, ProbabilityTable separatorTable) {
    Set<Node> common = TableUtils.getCommonNodes(input, output);
    int iterationSteps = calculateIterationSteps(common);
    return new MessagePassIterator(
        buildData(input, common, iterationSteps),
        buildData(output, common, iterationSteps),
        separatorTable.getVector());
  }
}
