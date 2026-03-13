package io.github.alecredmond.internal.method.inference.junctiontree.separators;

import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.internal.application.inference.junctiontree.Clique;
import io.github.alecredmond.internal.application.inference.junctiontree.JunctionTreeData;
import io.github.alecredmond.internal.application.inference.junctiontree.Separator;
import io.github.alecredmond.internal.method.probabilitytables.transfer.TransferIterator;
import io.github.alecredmond.internal.method.probabilitytables.transfer.TransferIteratorBuilder;
import io.github.alecredmond.internal.method.probabilitytables.TableBuilder;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;

import java.util.HashMap;
import java.util.Map;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SeparatorFactory {
  public static final TransferIteratorBuilder TRANSFER_BUILDER = new TransferIteratorBuilder();

  public Separator buildSeparator(Clique cliqueA, Clique cliqueB, JunctionTreeData jtd) {
    Separator separator = new Separator();
    setConnectedCliques(cliqueA, cliqueB, separator);
    setNodes(cliqueA, cliqueB, separator);
    buildTable(jtd.getBayesianNetworkData(), separator);
    buildMessagePassers(separator, jtd, cliqueA, cliqueB);
    return separator;
  }

  private void setConnectedCliques(Clique cliqueA, Clique cliqueB, Separator separator) {
    separator.setConnected(Map.of(cliqueA, cliqueB, cliqueB, cliqueA));
  }

  private void setNodes(Clique cliqueA, Clique cliqueB, Separator separator) {
    separator.setNodes(TableUtils.getCommonNodes(cliqueA.getTable(), cliqueB.getTable()));
  }

  private void buildTable(BayesianNetworkData data, Separator separator) {
    separator.setTable(TableBuilder.buildJunctionTreeTable(separator.getNodes(), data));
  }

  private void buildMessagePassers(
      Separator separator, JunctionTreeData data, Clique cliqueA, Clique cliqueB) {
    Map<Clique, TransferIterator> messagePassers =
        data.isSolverConfig()
            ? buildSolverPassers(cliqueA, cliqueB)
            : buildInferencePassers(cliqueA, cliqueB, separator);
    separator.setMessagePassers(messagePassers);
  }

  private Map<Clique, TransferIterator> buildSolverPassers(Clique cliqueA, Clique cliqueB) {
    Map<Clique, TransferIterator> map = new HashMap<>();
    map.put(
        cliqueA,
        TRANSFER_BUILDER.buildMarginalTransferIterator(cliqueA.getTable(), cliqueB.getTable()));
    map.put(
        cliqueB,
        TRANSFER_BUILDER.buildMarginalTransferIterator(cliqueB.getTable(), cliqueA.getTable()));
    return map;
  }

  private Map<Clique, TransferIterator> buildInferencePassers(
      Clique cliqueA, Clique cliqueB, Separator separator) {
    Map<Clique, TransferIterator> map = new HashMap<>();
    map.put(
        cliqueA,
        TRANSFER_BUILDER.buildMessagePassIterator(
            cliqueA.getTable(), cliqueB.getTable(), separator.getTable()));
    map.put(
        cliqueB,
        TRANSFER_BUILDER.buildMessagePassIterator(
            cliqueB.getTable(), cliqueA.getTable(), separator.getTable()));
    return map;
  }
}
