package io.github.alecredmond.method.printer;

import static io.github.alecredmond.application.printer.PrinterConfigs.LEFT_PAD_FORMAT;
import static io.github.alecredmond.application.printer.PrinterConfigs.RIGHT_PAD_FORMAT;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.printer.PrinterConfigs;
import io.github.alecredmond.application.probabilitytables.MarginalTable;
import io.github.alecredmond.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.method.probabilitytables.TableUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.Data;

@Data
public class TableFormatter {
  private final PrinterConfigs configs;

  public TableFormatter(PrinterConfigs configs) {
    this.configs = configs;
  }

  public List<String> generateTableLines(ProbabilityTable table) {
    List<List<NodeState>> eventCombos = createCombinations(table.getEvents(), table);
    List<List<NodeState>> conditionCombos = createCombinations(table.getConditions(), table);

    List<String> eventLabels = eventCombos.stream().map(this::joinStates).toList();
    List<String> conditionLabels = conditionCombos.stream().map(this::joinStates).toList();

    int eventColWidth = Math.max(getMaxLength(eventLabels), configs.getProbabilityCharLength());
    int condColWidth = getMaxLength(conditionLabels);

    String borderRow = createBorderRow(eventLabels.size(), eventColWidth, condColWidth);
    String headerRow = renderHeaderRow(eventLabels, eventColWidth, condColWidth);
    List<String> dataRows =
        renderDataRows(
            table, eventCombos, conditionCombos, conditionLabels, eventColWidth, condColWidth);

    List<String> tableLines = new ArrayList<>();
    tableLines.add(table.getTableID().toString());
    tableLines.add(borderRow);
    tableLines.add(headerRow);
    tableLines.addAll(dataRows);
    tableLines.add(borderRow);
    tableLines.add("");
    return tableLines;
  }

  private List<List<NodeState>> createCombinations(Set<Node> nodes, ProbabilityTable table) {
    return TableUtils.generateStateCombinations(nodes, ArrayList::new, table);
  }

  private String joinStates(List<NodeState> states) {
    return states.stream().map(NodeState::toString).collect(Collectors.joining("|"));
  }

  private int getMaxLength(List<String> eventLabels) {
    return eventLabels.stream().mapToInt(String::length).max().orElse(0);
  }

  private String createBorderRow(int eventCount, int eventColWidth, int condColWidth) {
    int numberOfDashes = condColWidth + (eventCount * eventColWidth) + (condColWidth > 0 ? 1 : 0);
    return "-" + "-".repeat(numberOfDashes + 1) + "-";
  }

  private String renderHeaderRow(List<String> eventLabels, int eventWidth, int condWidth) {
    StringBuilder header = new StringBuilder("|");
    if (condWidth > 0) {
      header.append(" ".repeat(condWidth)).append("|");
    }
    for (String label : eventLabels) {
      header.append(padLeft(label, eventWidth)).append("|");
    }
    return header.toString();
  }

  private List<String> renderDataRows(
      ProbabilityTable table,
      List<List<NodeState>> eventCombos,
      List<List<NodeState>> conditionCombos,
      List<String> conditionLabels,
      int eventWidth,
      int condWidth) {
    String probabilityFormatter = configs.getProbabilityFormatter();

    if (table instanceof MarginalTable) {
      return List.of(
          buildSingleRow(
              "", eventCombos, List.of(), table, eventWidth, condWidth, probabilityFormatter));
    }
    return IntStream.range(0, conditionCombos.size())
        .mapToObj(
            i ->
                buildSingleRow(
                    conditionLabels.get(i),
                    eventCombos,
                    conditionCombos.get(i),
                    table,
                    eventWidth,
                    condWidth,
                    probabilityFormatter))
        .toList();
  }

  private String padLeft(String text, int width) {
    return String.format(LEFT_PAD_FORMAT.formatted(width), text);
  }

  private String buildSingleRow(
      String conditionsLabel,
      List<List<NodeState>> eventCombos,
      List<NodeState> currentConditions,
      ProbabilityTable table,
      int eventWidth,
      int condWidth,
      String probFormatter) {
    StringBuilder row = new StringBuilder("|");

    if (!conditionsLabel.isEmpty()) {
      row.append(padRight(conditionsLabel, condWidth)).append("|");
    }

    eventCombos.stream()
        .map(events -> concatStates(currentConditions, events))
        .map(table::getProbability)
        .map(probability -> String.format(probFormatter, probability))
        .map(probString -> padLeft(probString, eventWidth))
        .forEach(padded -> row.append(padded).append("|"));

    return row.toString();
  }

  private String padRight(String text, int width) {
    return String.format(RIGHT_PAD_FORMAT.formatted(width), text);
  }

  private List<NodeState> concatStates(List<NodeState> currentConditions, List<NodeState> events) {
    return Stream.concat(currentConditions.stream(), events.stream()).toList();
  }
}
