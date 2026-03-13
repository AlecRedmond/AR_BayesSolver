package io.github.alecredmond.internal.method.printer;

import static io.github.alecredmond.internal.application.printer.PrinterConfigs.LEFT_PAD_FORMAT;
import static io.github.alecredmond.internal.application.printer.PrinterConfigs.RIGHT_PAD_FORMAT;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.MarginalTable;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.application.printer.PrinterConfigs;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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

    List<String> eventLabels = eventCombos.stream().map(this::joinStateStrings).toList();
    List<String> conditionLabels = createConditionLabels(conditionCombos, table.getConditions());

    int eventColWidth = Math.max(getMaxLength(eventLabels), configs.getProbabilityCharLength());
    int condColWidth = conditionLabels.isEmpty() ? 0 : conditionLabels.getFirst().length();

    List<String> dataRows =
        renderDataRows(
            table, eventCombos, conditionCombos, conditionLabels, eventColWidth, condColWidth);

    String headerRow = renderHeaderRow(eventLabels, eventColWidth, condColWidth);
    String borderRow = createBorderRow(dataRows);

    List<String> tableLines = new ArrayList<>();
    tableLines.add(table.getTableName().toString());
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

  private String joinStateStrings(List<NodeState> states) {
    return states.stream().map(NodeState::toString).collect(Collectors.joining("|"));
  }

  private List<String> createConditionLabels(
      List<List<NodeState>> conditionCombos, Set<Node> conditionNodes) {
    if (conditionCombos.isEmpty()) {
      return new ArrayList<>();
    }
    Map<NodeState, String> toStringMap = new HashMap<>();
    Map<Node, Integer> widthMap = new HashMap<>();
    fillWidthAndToStringMap(toStringMap, widthMap, conditionNodes);
    return conditionCombos.stream()
        .map(combo -> buildStringForConditionCombo(combo, toStringMap, widthMap))
        .toList();
  }

  private int getMaxLength(List<String> eventLabels) {
    return eventLabels.stream().mapToInt(String::length).max().orElse(0);
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

  private String createBorderRow(List<String> dataRows) {
    int firstLength = dataRows.getFirst().length();
    return "-".repeat(firstLength);
  }

  private void fillWidthAndToStringMap(
      Map<NodeState, String> toStringMap, Map<Node, Integer> widthMap, Set<Node> conditionNodes) {
    for (Node node : conditionNodes) {
      int longest = 0;
      for (NodeState state : node.getNodeStates()) {
        String stateString = state.toString();
        toStringMap.put(state, stateString);
        if (stateString.length() > longest) {
          longest = stateString.length();
        }
      }
      widthMap.put(node, longest);
    }
  }

  private String buildStringForConditionCombo(
      List<NodeState> combo, Map<NodeState, String> toStringMap, Map<Node, Integer> widthMap) {
    StringBuilder sb = new StringBuilder();
    combo.forEach(
        state ->
            sb.append(padRight(toStringMap.get(state), widthMap.get(state.getNode()))).append("|"));
    sb.setLength(sb.length() - 1);
    return sb.toString();
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
        .map(events -> NodeUtils.combineStates(events, currentConditions))
        .map(table::getProbability)
        .map(probability -> String.format(probFormatter, probability))
        .map(probString -> padLeft(probString, eventWidth))
        .forEach(padded -> row.append(padded).append("|"));

    return row.toString();
  }

  private String padLeft(String text, int width) {
    return String.format(LEFT_PAD_FORMAT.formatted(width), text);
  }

  private String padRight(String text, int width) {
    return String.format(RIGHT_PAD_FORMAT.formatted(width), text);
  }
}
