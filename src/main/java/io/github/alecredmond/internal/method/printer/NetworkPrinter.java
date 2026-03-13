package io.github.alecredmond.internal.method.printer;

import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.application.printer.PrinterConfigs;
import java.util.*;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NetworkPrinter {
  private final BayesianNetworkData networkData;
  private final PrinterConfigs configs;
  private final TableFormatter tableFormatter;
  private final FileExporter fileExporter;

  public NetworkPrinter(BayesianNetworkData networkData) {
    this.networkData = networkData;
    this.configs = new PrinterConfigs();
    this.fileExporter = new FileExporter(configs);
    this.tableFormatter = new TableFormatter(configs);
  }

  public void printObserved() {
    printTables(networkData.getObservedTablesMap(), configs.getObservedFileTitle());
  }

  public <T extends ProbabilityTable> void printTables(
      Map<Node, T> associatedMap, String tableType) {
    List<String> outputLines = new ArrayList<>(List.of(tableType, ""));

    networkData.getNodes().stream()
        .map(associatedMap::get)
        .map(tableFormatter::generateTableLines)
        .forEach(outputLines::addAll);

    if (configs.isPrintToConsole()) {
      outputLines.forEach(log::info);
    }
    if (configs.isPrintToTextFile()) {
      fileExporter.exportLinesToFile(outputLines, tableType, networkData.getNetworkName());
    }
  }

  public void printNetwork() {
    printTables(networkData.getNetworkTablesMap(), configs.getNetworkFileTitle());
  }
}
