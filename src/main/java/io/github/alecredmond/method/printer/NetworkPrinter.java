package io.github.alecredmond.method.printer;

import static io.github.alecredmond.application.printer.PrinterConfigs.*;

import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.printer.PrinterConfigs;
import io.github.alecredmond.application.probabilitytables.ConditionalTable;
import io.github.alecredmond.application.probabilitytables.MarginalTable;
import io.github.alecredmond.application.probabilitytables.ProbabilityTable;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NetworkPrinter {
  private final BayesianNetworkData networkData;
  private final PrinterConfigs configs;

  public NetworkPrinter(BayesianNetworkData networkData) {
    this.networkData = networkData;
    this.configs = new PrinterConfigs();
  }

  public <T extends ProbabilityTable> void printTables(
      Map<Node, T> associatedMap, String tableType) {
    List<String> outputLines = new ArrayList<>(List.of(tableType, ""));

    networkData.getNodes().stream()
        .map(associatedMap::get)
        .forEach(table -> outputLines.addAll(generateTableLines(table)));

    if (configs.isPrintToConsole()) {
      outputLines.forEach(System.out::println);
    }
    if (configs.isPrintToTextFile()) {
      printToFile(outputLines, tableType);
    }
  }

  public void printObserved() {
    printTables(networkData.getObservationMap(), configs.getObservedFileTitle());
  }

  public void printNetwork() {
    printTables(networkData.getNetworkTablesMap(), configs.getNetworkFileTitle());
  }

  private List<String> generateTableLines(ProbabilityTable table) {
    List<List<NodeState>> eventCombinations =
        table.getUtils().generateStateCombinations(table.getEvents(), ArrayList::new);
    List<List<NodeState>> conditionCombinations =
        table.getUtils().generateStateCombinations(table.getConditions(), ArrayList::new);

    List<String> eventHeaders = formatStateCombinations(eventCombinations, EVENT_COLUMN);
    List<String> conditionLabels = formatStateCombinations(conditionCombinations, CONDITION_COLUMN);

    int eventColumnWidth = calculateColumnWidth(eventHeaders, table, EVENT_COLUMN);
    int conditionColumnWidth = calculateColumnWidth(conditionLabels, table, CONDITION_COLUMN);

    String[][] probabilityCells =
        populateProbabilityCells(eventCombinations, conditionCombinations, table);

    String tableTitle = table.getTableID().toString();
    String border = createBorder(eventHeaders.size(), eventColumnWidth, conditionColumnWidth);
    String headerRow = buildHeaderRow(eventHeaders, eventColumnWidth, conditionColumnWidth);
    List<String> dataRows =
        buildDataRows(conditionLabels, probabilityCells, eventColumnWidth, conditionColumnWidth);

    List<String> tableLines = new ArrayList<>();
    tableLines.add(tableTitle);
    tableLines.add(border);
    tableLines.add(headerRow);
    tableLines.addAll(dataRows);
    tableLines.add(border);
    tableLines.add("");
    return tableLines;
  }

  private void printToFile(List<String> lines, String observedOrNetwork) {
    String filePath = validateFilePath(getDefaultFilePath(), observedOrNetwork, 0);
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
    if (configs.isOpenFileOnCreation()) openCreatedFile(filePath);
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
      List<List<NodeState>> stateCombinations, boolean alignLeft) {
    int paddingWidth = findLongestStateNameLength(stateCombinations) + 1;
    return stateCombinations.stream()
        .filter(combination -> !combination.isEmpty())
        .map(combination -> formatStateCombination(combination, paddingWidth, alignLeft))
        .toList();
  }

  private int calculateColumnWidth(
      List<String> columnContent, ProbabilityTable table, boolean isEventColumn) {
    if (!isEventColumn && table instanceof MarginalTable) {
      return 0;
    }
    int characterWidth = columnContent.stream().mapToInt(String::length).max().orElse(0);
    return Math.max(characterWidth, configs.getProbabilityCharLength());
  }

  private String createBorder(int eventCount, int eventColumnWidth, int conditionColumnWidth) {
    StringBuilder border = new StringBuilder();
    int numberOfDashes = conditionColumnWidth + eventCount * eventColumnWidth;
    border.append("-".repeat(Math.max(0, numberOfDashes + 1)));
    if (conditionColumnWidth != 0) {
      border.append("-");
    }
    return border.toString();
  }

  private String[][] populateProbabilityCells(
      List<List<NodeState>> eventCombinations,
      List<List<NodeState>> conditionCombinations,
      ProbabilityTable table) {
    switch (table) {
      case MarginalTable mt -> {
        return populateCellsMarginal(eventCombinations, mt);
      }
      case ConditionalTable ct -> {
        return populateCellsConditional(eventCombinations, conditionCombinations, ct);
      }
      default -> throw new IllegalStateException("Unexpected value: " + table);
    }
  }

  private String[][] populateCellsConditional(
      List<List<NodeState>> eventCombinations,
      List<List<NodeState>> conditionCombinations,
      ConditionalTable table) {
    int rowCount = conditionCombinations.size();
    int columnCount = eventCombinations.size();
    String[][] cells = new String[rowCount][columnCount];
    for (int column = 0; column < columnCount; column++) {
      for (int row = 0; row < rowCount; row++) {
        List<NodeState> eventState = eventCombinations.get(column);
        List<NodeState> conditionStates = conditionCombinations.get(row);
        cells[row][column] = getProbabilityAsString(eventState, conditionStates, table);
      }
    }
    return cells;
  }

  private String[][] populateCellsMarginal(
      List<List<NodeState>> eventCombinations, MarginalTable table) {
    int columnCount = eventCombinations.size();
    String[][] cells = new String[1][columnCount];
    for (int column = 0; column < columnCount; column++) {
      List<NodeState> eventState = eventCombinations.get(column);
      cells[0][column] = getProbabilityAsString(eventState, List.of(), table);
    }
    return cells;
  }

  private String getDefaultFilePath() {
    String networkName = networkData.getNetworkName();
    String dateTime = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    String directory = configs.getSaveDirectory();
    try {
      Files.createDirectories(Paths.get(directory));
    } catch (IOException e) {
      log.error("{} : COULD NOT CREATE DIRECTORY {}", e, directory);
    }
    return String.format("%s%s_%s", directory, dateTime, networkName);
  }

  private String validateFilePath(String filePath, String observedOrNetwork, int counter) {
    String newFilePath = String.format("%s_%s_%d", filePath, observedOrNetwork, counter);
    File file = new File(newFilePath + ".txt");
    try {
      if (file.createNewFile()) {
        return newFilePath + ".txt";
      } else return validateFilePath(filePath, observedOrNetwork, counter + 1);

    } catch (IOException e) {
      log.error("{} creating new file {}", e, filePath);
    }
    return "";
  }

  private int findLongestStateNameLength(List<List<NodeState>> stateCombinations) {
    return stateCombinations.stream()
        .flatMap(Collection::stream)
        .map(NodeState::toString)
        .mapToInt(String::length)
        .max()
        .orElse(0);
  }

  private String formatStateCombination(
      List<NodeState> states, int paddingWidth, boolean alignLeft) {
    return states.stream()
        .map(NodeState::toString)
        .map(
            state ->
                alignLeft
                    ? padString(state, paddingWidth, LEFT_PAD_FORMAT)
                    : padString(state, paddingWidth, RIGHT_PAD_FORMAT))
        .collect(Collectors.joining("|"));
  }

  private String getProbabilityAsString(
      List<NodeState> events, List<NodeState> conditions, ProbabilityTable table) {
    List<NodeState> probabilityKey = new ArrayList<>(events);
    probabilityKey.addAll(conditions);
    double probability = table.getProbability(probabilityKey);
    return String.format(configs.getProbabilityFormatter(), probability);
  }

  private String buildHeaderRow(
      List<String> eventHeaders, int eventColumnWidth, int conditionColumnWidth) {
    StringBuilder headerRow = new StringBuilder("|");
    if (conditionColumnWidth != 0) {
      headerRow.append(" ".repeat(conditionColumnWidth)).append("|");
    }
    for (String header : eventHeaders) {
      headerRow.append(padString(header, eventColumnWidth, LEFT_PAD_FORMAT)).append("|");
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
        dataRow
            .append(padString(conditionLabels.get(row), conditionColumnWidth, RIGHT_PAD_FORMAT))
            .append("|");
      }
      for (int column = 0; column < probabilityCells[row].length; column++) {
        dataRow
            .append(padString(probabilityCells[row][column], eventColumnWidth, LEFT_PAD_FORMAT))
            .append("|");
      }
      dataRows.add(dataRow.toString());
    }
    return dataRows;
  }

  private String padString(String text, int width, String formatting) {
    return String.format(formatting.formatted(width), text);
  }
}
