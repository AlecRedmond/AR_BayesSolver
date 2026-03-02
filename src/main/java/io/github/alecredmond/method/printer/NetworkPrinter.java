package io.github.alecredmond.method.printer;

import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.printer.PrinterConfigs;
import io.github.alecredmond.application.probabilitytables.ProbabilityTable;
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
    printTables(networkData.getObservationMap(), configs.getObservedFileTitle());
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
