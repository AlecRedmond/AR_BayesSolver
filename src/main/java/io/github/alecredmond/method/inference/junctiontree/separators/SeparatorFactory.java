package io.github.alecredmond.method.inference.junctiontree.separators;

import io.github.alecredmond.application.inference.junctiontree.*;
import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.method.inference.junctiontree.handlers.JTATableHandler;
import io.github.alecredmond.method.inference.junctiontree.handlers.readwrite.JTATransferWriter;
import io.github.alecredmond.method.inference.junctiontree.handlers.readwrite.JTATransferWriterFactory;
import io.github.alecredmond.method.probabilitytables.TableBuilder;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SeparatorFactory {

  public Separator buildSeparator(Clique cliqueA, Clique cliqueB, JunctionTreeData jtd) {
    Separator separator = new Separator();
    setConnectedCliques(cliqueA, cliqueB, separator);
    setNodes(cliqueA, cliqueB, separator);
    buildTable(jtd.getBayesianNetworkData(), separator);
    buildTableHandler(separator);
    JTATransferWriterFactory factory = new JTATransferWriterFactory();
    buildInputWriters(factory, separator);
    buildOutputWriters(factory, separator);
    return separator;
  }

  private void setConnectedCliques(Clique cliqueA, Clique cliqueB, Separator separator) {
    separator.setConnected(Map.of(cliqueA, cliqueB, cliqueB, cliqueA));
  }

  private void setNodes(Clique cliqueA, Clique cliqueB, Separator separator) {
    separator.setNodes(
        cliqueA.getNodes().stream()
            .filter(cliqueB.getNodes()::contains)
            .collect(Collectors.toSet()));
  }

  private void buildTable(BayesianNetworkData data, Separator separator) {
    separator.setTable(TableBuilder.buildJunctionTreeTable(separator.getNodes(), data));
  }

  private void buildTableHandler(Separator separator) {
    separator.setHandler(new JTATableHandler(separator.getTable()));
  }

  private void buildInputWriters(JTATransferWriterFactory factory, Separator separator) {
    setWriterMap(
        separator,
        clique -> factory.buildMessagePassWriter(clique.getTable(), separator.getTable()),
        separator::setInputWriters);
  }

  private void buildOutputWriters(JTATransferWriterFactory factory, Separator separator) {
    setWriterMap(
        separator,
        clique -> factory.buildMessagePassWriter(separator.getTable(), clique.getTable()),
        separator::setOutputWriters);
  }

  private void setWriterMap(
      Separator separator,
      Function<Clique, JTATransferWriter> writerBuilder,
      Consumer<Map<Clique, JTATransferWriter>> setter) {
    Map<Clique, JTATransferWriter> writerMap =
        separator.getConnected().keySet().stream()
            .map(clique -> Map.entry(clique, writerBuilder.apply(clique)))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    setter.accept(writerMap);
  }
}
