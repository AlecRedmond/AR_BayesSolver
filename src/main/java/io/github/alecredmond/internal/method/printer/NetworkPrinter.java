package io.github.alecredmond.internal.method.printer;

import io.github.alecredmond.exceptions.NetworkPrinterException;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.method.inference.InferenceEngine;
import io.github.alecredmond.internal.application.printer.PrinterConfigs;
import java.util.*;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NetworkPrinter {
  private final InferenceEngine engine;
  private final BayesianNetworkData networkData;
  private final PrinterConfigs configs;
  private final TableFormatter tableFormatter;
  private final FileExporter fileExporter;

  public NetworkPrinter(BayesianNetworkData networkData) {
    this.engine = null;
    this.networkData = networkData;
    this.configs = new PrinterConfigs();
    this.fileExporter = new FileExporter(configs);
    this.tableFormatter = new TableFormatter(configs);
  }

  public NetworkPrinter(InferenceEngine engine) {
    this.engine = engine;
    this.networkData = engine.getNetwork().getNetworkData();
    this.configs = new PrinterConfigs();
    this.fileExporter = new FileExporter(configs);
    this.tableFormatter = new TableFormatter(configs);
  }

  public void printObserved() {
    if (engine == null) return;
    printTables(engine.getObservedTables(), configs.getObservedFileTitle());
  }

  public <T extends ProbabilityTable> void printTables(
      Map<Node, T> associatedMap, String tableType) {
    List<String> outputLines = new ArrayList<>(List.of(tableType, ""));

    networkData.getNodes().stream()
        .filter(associatedMap::containsKey)
        .map(associatedMap::get)
        .map(tableFormatter::generateTableLines)
        .forEach(outputLines::addAll);

    try {
      printLines(outputLines, tableType);
    } catch (NetworkPrinterException e) {
      log.error(e.getMessage());
    }
  }

  public void printLines(List<String> outputLines, String documentTitle) {
    if (configs.isPrintToConsole()) {
      outputLines.forEach(log::info);
    }
    if (configs.isPrintToTextFile()) {
      fileExporter.exportLinesToFile(outputLines, documentTitle, networkData.getNetworkName());
    }
  }

  public <T extends ProbabilityTable> void printTable(T table) {
    List<String> outputLines = new ArrayList<>(List.of("TABLE", ""));
    outputLines.addAll(tableFormatter.generateTableLines(table));
    printLines(outputLines, "TABLE");
  }

  public void printNetwork() {
    printTables(networkData.getNetworkTablesMap(), configs.getNetworkFileTitle());
  }
}
