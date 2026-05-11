package io.github.alecredmond.internal.method.probabilitytables.tabletransfer.factory;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.application.vectoriterator.OdometerInitializer;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.readwriters.TransferIterator;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.OdometerInitializerUtils;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.resetlogictypes.BaseOdometerResetLogic;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.updatelogictypes.BlankUpdater;
import java.util.Set;
import java.util.function.Predicate;

public abstract class TransferReadWriteFactory<T extends TransferIterator>
    implements BaseOdometerResetLogic, BlankUpdater {
  protected ProbabilityTable readTable;
  protected ProbabilityTable writeTable;
  protected double[] transferArray;
  protected Set<Node> commonNodes;

  protected TransferReadWriteFactory(ProbabilityTable readTable, ProbabilityTable writeTable) {
    super();
    this.readTable = readTable;
    this.writeTable = writeTable;
    this.commonNodes = TableUtils.getCommonNodes(readTable, writeTable);
    this.transferArray = new double[calculateTransferArrayLength()];
  }

  private int calculateTransferArrayLength() {
    return commonNodes.stream().map(node -> node.getNodeStates().size()).reduce(1, (x, y) -> x * y);
  }

  protected TransferReadWriteFactory(
      ProbabilityTable readTable, ProbabilityTable writeTable, double[] transferArray) {
    this.readTable = readTable;
    this.writeTable = writeTable;
    this.commonNodes = TableUtils.getCommonNodes(readTable, writeTable);
    this.transferArray = transferArray;
  }

  @Override
  public void updateInitializer(
      OdometerInitializer initializer, VectorOdometer odometer, boolean[] positionLocks) {
    OdometerInitializerUtils.updateIterateInner(initializer, odometer);
  }

  public abstract T build();

  @Override
  public Predicate<Node> checkLockOuter() {
    return node -> !commonNodes.contains(node);
  }

  @Override
  public Predicate<Node> checkLockInner() {
    return commonNodes::contains;
  }
}
