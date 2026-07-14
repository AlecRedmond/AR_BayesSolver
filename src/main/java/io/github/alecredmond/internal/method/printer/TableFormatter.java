package io.github.alecredmond.internal.method.printer;

import io.github.alecredmond.exceptions.NetworkPrinterException;
import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.application.printer.PrinterPropertyConfigs;
import io.github.alecredmond.internal.application.printer.PrinterStringMatrix;
import io.github.alecredmond.internal.method.probabilitytables.ProbabilityTableBase;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Data;

@Data
public class TableFormatter {
  private static final String RIGHT_ALIGN_FORMAT = "%%%ds";
  private static final String LEFT_ALIGN_FORMAT = "%%-%ds";
  private final PrinterPropertyConfigs configs;

  public TableFormatter(PrinterPropertyConfigs configs) {
    this.configs = configs;
  }

  public List<String> generateTableLines(ProbabilityTable table) {
    PrinterStringMatrix matrix = generateMatrix(table);
    padLabels(matrix.rowLabels(), matrix.rowLabelWidths());
    padLabels(matrix.columnLabels(), matrix.columnLabelWidths());

    String tableTitle = matrix.tableTitle();
    List<String> dataLines = buildDataLines(matrix);
    int lineLength = dataLines.getFirst().length();
    List<String> headerLines = buildHeaderLines(matrix, lineLength);
    String divider = "-".repeat(lineLength);

    List<String> tableLines = new ArrayList<>();
    tableLines.add(tableTitle);
    tableLines.add(divider);
    tableLines.addAll(headerLines);
    tableLines.add(divider);
    tableLines.addAll(dataLines);
    tableLines.add(divider);
    tableLines.add("");
    return tableLines;
  }

  private PrinterStringMatrix generateMatrix(ProbabilityTable table) {
    return Optional.of(table)
        .filter(ProbabilityTableBase.class::isInstance)
        .map(ProbabilityTableBase.class::cast)
        .map(tableBase -> tableBase.generatePrinterMatrix(configs))
        .orElseThrow(() -> noMeansToGenerateException(table));
  }

  private void padLabels(String[][] labels, int[] labelWidths) {
    for (int row = 0; row < labels.length; row++) {
      for (int col = 0; col < labelWidths.length; col++) {
        labels[row][col] = alignLeft(labels[row][col], labelWidths[col]);
      }
    }
  }

  private List<String> buildDataLines(PrinterStringMatrix matrix) {
    double[][] probs2D = matrix.probabilities2D();
    String pFormat = configs.getProbabilityFormatter();
    String[][] stateLabels = matrix.rowLabels();
    int[] columnWidths = matrix.columnLabelWidths();

    if (stateLabels.length == 0) {
      return List.of(stringifyDataRow(new String[0], probs2D[0], columnWidths, pFormat));
    }

    return IntStream.range(0, stateLabels.length)
        .mapToObj(row -> stringifyDataRow(stateLabels[row], probs2D[row], columnWidths, pFormat))
        .toList();
  }

  private List<String> buildHeaderLines(PrinterStringMatrix matrix, int totalLineLength) {
    return Arrays.stream(matrix.columnLabels())
        .map(this::stringifyHeaderLine)
        .map(xAxisLabel -> alignRight(xAxisLabel, totalLineLength))
        .map(string -> string.replaceFirst(" ", "|"))
        .toList();
  }

  private static NetworkPrinterException noMeansToGenerateException(ProbabilityTable table) {
    return new NetworkPrinterException(
        "Table %s had no means to generate a printer matrix!".formatted(table.getTableName()));
  }

  private String alignLeft(String text, int width) {
    return String.format(LEFT_ALIGN_FORMAT.formatted(width), text);
  }

  private String stringifyDataRow(
      String[] stateLabels, double[] rowProbabilities, int[] columnWidths, String pFormatter) {
    StringBuilder sb = new StringBuilder("|");
    addRowStateLabels(stateLabels, sb);
    addRowProbabilities(rowProbabilities, columnWidths, pFormatter, sb);
    return sb.toString();
  }

  private String stringifyHeaderLine(String[] headerRow) {
    return Arrays.stream(headerRow)
        .map(label -> label + "|")
        .collect(Collectors.joining("", "|", ""));
  }

  private String alignRight(String text, int width) {
    return String.format(RIGHT_ALIGN_FORMAT.formatted(width), text);
  }

  private void addRowStateLabels(String[] rowLabel, StringBuilder sb) {
    Arrays.stream(rowLabel).forEach(label -> sb.append(label).append("|"));
  }

  private void addRowProbabilities(
      double[] probabilities, int[] columnWidths, String pFormatter, StringBuilder sb) {
    IntStream.range(0, probabilities.length)
        .mapToObj(i -> alignRight(String.format(pFormatter, probabilities[i]), columnWidths[i]))
        .forEach(s -> sb.append(s).append("|"));
  }
}
