package io.github.alecredmond.method.printer;

import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.printer.PrinterConfigs;
import io.github.alecredmond.application.probabilitytables.MarginalTable;
import io.github.alecredmond.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.method.node.NodeUtils;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NetworkPrinter {
  private final BayesianNetworkData networkData;
  private final PrinterConfigs printerConfigs;

  public NetworkPrinter(BayesianNetworkData networkData, PrinterConfigs printerConfigs) {
    this.networkData = networkData;
    this.printerConfigs = printerConfigs;
  }

  public void printObserved() {
    List<String> outputLines = new ArrayList<>();
    outputLines.add("OBSERVED TABLES:\n");
    networkData
        .getObservationMap()
        .values()
        .forEach(table -> outputLines.addAll(generateTableLines(table)));
    if (printerConfigs.isPrintToConsole()) {
      outputLines.forEach(System.out::println);
    } else {
      printToFile(outputLines, "observed");
    }
  }

  public void printNetwork() {
    List<String> outputLines = new ArrayList<>();
    outputLines.add("NETWORK TABLES:\n");
    networkData
        .getNetworkTablesMap()
        .values()
        .forEach(table -> outputLines.addAll(generateTableLines(table)));
    if (printerConfigs.isPrintToConsole()) {
      outputLines.forEach(System.out::println);
    } else {
      printToFile(outputLines, "network");
    }
  }

  private List<String> generateTableLines(ProbabilityTable table) {
    List<Set<NodeState>> eventCombinations = NodeUtils.generateStateCombinations(table.getEvents());
    List<Set<NodeState>> conditionCombinations =
        NodeUtils.generateStateCombinations(table.getConditions());

    List<String> eventHeaders = formatStateCombinations(eventCombinations, true);
    List<String> conditionLabels = formatStateCombinations(conditionCombinations, false);

    int eventColumnWidth = calculateColumnWidth(eventHeaders, table, true);
    int conditionColumnWidth = calculateColumnWidth(conditionLabels, table, false);

    String horizontalBorder =
        createHorizontalBorder(eventHeaders.size(), eventColumnWidth, conditionColumnWidth);
    String[][] probabilityCells =
        populateProbabilityCells(eventCombinations, conditionCombinations, table);

    return buildTableOutput(
        table.getTableID().toString(),
        horizontalBorder,
        eventHeaders,
        conditionLabels,
        probabilityCells,
        eventColumnWidth,
        conditionColumnWidth);
  }

  private void printToFile(List<String> lines, String type) {
    String filePath = createOrRename(getFilePath(), type, 0);
    try {
      FileWriter fw = new FileWriter(filePath);
      PrintWriter pw = new PrintWriter(fw);
      lines.forEach(pw::println);
      pw.close();
      log.info("File saved to {}", filePath);
    } catch (IOException e) {
      log.error("{} attempting to write to {}", e, filePath);
      return;
    }
    if (printerConfigs.isOpenFileOnCreation()) openCreatedFile(filePath);
  }

  private void openCreatedFile(String filePath) {
    Desktop dt = Desktop.getDesktop();
    File file = new File(filePath);
    try {
      dt.open(file);
    } catch (IOException e) {
      log.error("{} attempting to open created file at {}", e, file.getAbsolutePath());
    }
  }

  private List<String> formatStateCombinations(
      List<Set<NodeState>> stateCombinations, boolean alignLeft) {
    int maxStateNameLength = findLongestStateNameLength(stateCombinations);
    return stateCombinations.stream()
        .filter(combination -> !combination.isEmpty())
        .map(combination -> formatStateCombination(combination, maxStateNameLength, alignLeft))
        .toList();
  }

  private int calculateColumnWidth(
          List<String> columnContent, ProbabilityTable table, boolean isEventColumn) {
    if (!isEventColumn && table instanceof MarginalTable) return 0;
    int contentWidth = columnContent.stream().mapToInt(String::length).max().orElse(0);
    return Math.max(contentWidth, printerConfigs.getProbDecimalPlaces() + 2);
  }

  private String createHorizontalBorder(
      int eventCount, int eventColumnWidth, int conditionColumnWidth) {
    StringBuilder border = new StringBuilder();
    appendDashes(border, conditionColumnWidth);
    for (int i = 0; i < eventCount; i++) {
      appendDashes(border, eventColumnWidth);
    }
    if (conditionColumnWidth != 0) border.append("-");
    return border.toString();
  }

  private String[][] populateProbabilityCells(
      List<Set<NodeState>> eventCombinations,
      List<Set<NodeState>> conditionCombinations,
      ProbabilityTable table) {
    int rowCount = conditionCombinations.isEmpty() ? 1 : conditionCombinations.size();
    int columnCount = eventCombinations.size();
    String[][] cells = new String[rowCount][columnCount];

    for (int column = 0; column < columnCount; column++) {
      if (rowCount == 1) {
        cells[0][column] =
            formatProbability(eventCombinations.get(column), Collections.emptySet(), table);
      } else {
        for (int row = 0; row < rowCount; row++) {
          cells[row][column] =
              formatProbability(
                  eventCombinations.get(column), conditionCombinations.get(row), table);
        }
      }
    }
    return cells;
  }

  private List<String> buildTableOutput(
      String tableTitle,
      String horizontalBorder,
      List<String> eventHeaders,
      List<String> conditionLabels,
      String[][] probabilityCells,
      int eventColumnWidth,
      int conditionColumnWidth) {
    List<String> tableLines = new ArrayList<>();
    tableLines.add(tableTitle);
    tableLines.add(horizontalBorder);
    tableLines.add(buildHeaderRow(eventHeaders, eventColumnWidth, conditionColumnWidth));
    tableLines.addAll(
        buildDataRows(conditionLabels, probabilityCells, eventColumnWidth, conditionColumnWidth));
    tableLines.add(horizontalBorder);
    tableLines.add("");
    return tableLines;
  }

  private String getFilePath() {
    String networkName = networkData.getNetworkName();
    String dateTime = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    String directory = printerConfigs.getSaveDirectory();
    try {
      Files.createDirectories(Paths.get(directory));
    } catch (IOException e) {
      log.error("{} : COULD NOT CREATE DIRECTORY {}", e, directory);
    }
    return String.format("%s%s_%s", directory, dateTime, networkName);
  }

  private String createOrRename(String filePath, String type, int counter) {
    String newFilePath = String.format("%s_%s_%d", filePath, type, counter);
    File file = new File(newFilePath + ".txt");
    try {
      if (file.createNewFile()) {
        return newFilePath + ".txt";
      } else return createOrRename(filePath, type, counter + 1);

    } catch (IOException e) {
      log.error("{} creating new file {}", e, filePath);
    }
    return "";
  }

  private int findLongestStateNameLength(List<Set<NodeState>> stateCombinations) {
    return stateCombinations.stream()
        .flatMap(Collection::stream)
        .map(NodeState::toString)
        .mapToInt(String::length)
        .max()
        .orElse(0);
  }

  private String formatStateCombination(
      Set<NodeState> states, int paddingWidth, boolean alignLeft) {
    return states.stream()
        .map(NodeState::toString)
        .sorted()
        .map(
            state ->
                alignLeft
                    ? padStringLeft(state, paddingWidth)
                    : padStringRight(state, paddingWidth))
        .collect(Collectors.joining("|"));
  }

  private void appendDashes(StringBuilder builder, int dashCount) {
    builder.append("-".repeat(Math.max(0, dashCount + 1)));
  }

  private String formatProbability(
      Set<NodeState> events, Set<NodeState> conditions, ProbabilityTable table) {
    Set<NodeState> probabilityKey = createProbabilityKey(events, conditions);
    double probability = table.getProbability(probabilityKey);
    return String.format(getProbabilityFormatString(), probability);
  }

  private String buildHeaderRow(
      List<String> eventHeaders, int eventColumnWidth, int conditionColumnWidth) {
    StringBuilder headerRow = new StringBuilder("|");
    if (conditionColumnWidth != 0) {
      headerRow.append(" ".repeat(conditionColumnWidth)).append("|");
    }
    for (String header : eventHeaders) {
      headerRow.append(padStringLeft(header, eventColumnWidth)).append("|");
    }
    return headerRow.toString();
  }

  private List<String> buildDataRows(
      List<String> conditionLabels,
      String[][] probabilityCells,
      int eventColumnWidth,
      int conditionColumnWidth) {
    List<String> dataRows = new ArrayList<>();
    int rowCount = probabilityCells.length;

    for (int row = 0; row < rowCount; row++) {
      StringBuilder dataRow = new StringBuilder("|");
      if (!conditionLabels.isEmpty()) {
        dataRow.append(padStringRight(conditionLabels.get(row), conditionColumnWidth)).append("|");
      }
      for (int column = 0; column < probabilityCells[row].length; column++) {
        dataRow.append(padStringLeft(probabilityCells[row][column], eventColumnWidth)).append("|");
      }
      dataRows.add(dataRow.toString());
    }
    return dataRows;
  }

  private String padStringLeft(String text, int totalWidth) {
    return String.format("%%%ds".formatted(totalWidth), text);
  }

  private String padStringRight(String text, int totalWidth) {
    return String.format("%%-%ds".formatted(totalWidth), text);
  }

  private Set<NodeState> createProbabilityKey(Set<NodeState> events, Set<NodeState> conditions) {
    return conditions.isEmpty()
        ? events
        : Stream.concat(events.stream(), conditions.stream()).collect(Collectors.toSet());
  }

  private String getProbabilityFormatString() {
    int precision = printerConfigs.getProbDecimalPlaces();
    return "%." + precision + "f";
  }
}
