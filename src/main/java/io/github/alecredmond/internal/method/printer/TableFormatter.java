package io.github.alecredmond.internal.method.printer;

import io.github.alecredmond.exceptions.NetworkPrinterException;
import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.application.printer.PrinterPropertyConfigs;
import io.github.alecredmond.internal.application.printer.PrinterStringMatrix;
import io.github.alecredmond.internal.method.probabilitytables.ProbabilityTableBase;
import java.util.*;
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
    padRowLabels(matrix);
    padColLabels(matrix);
    List<String> rowLines = buildRowLines(matrix);
    int lineLength = rowLines.getFirst().length();
    List<String> columnLabelLines = buildColumnLabelLines(matrix, lineLength);
    String divider = "-".repeat(lineLength);

    List<String> tableLines = new ArrayList<>();
    tableLines.add(table.getTableName().toString());
    tableLines.add(divider);
    tableLines.addAll(columnLabelLines);
    tableLines.add(divider);
    tableLines.addAll(rowLines);
    tableLines.add(divider);
    tableLines.add("");
    return tableLines;
  }

  private PrinterStringMatrix generateMatrix(ProbabilityTable table) {
    if (table instanceof ProbabilityTableBase<?> tableBase) {
      return tableBase.generateStringMatrix(configs);
    }
    throw new NetworkPrinterException(
        "Table %s had no means to generate a printer matrix!".formatted(table.getTableName()));
  }

  private void padRowLabels(PrinterStringMatrix matrix) {
    String[][] rowLabels = matrix.rowLabels();
    int[] rowLabelWidths = matrix.rowLabelWidths();
    for (int row = 0; row < rowLabels.length; row++) {
      for (int col = 0; col < rowLabelWidths.length; col++) {
        int width = rowLabelWidths[col];
        rowLabels[row][col] = alignLeft(rowLabels[row][col], width);
      }
    }
  }

  private void padColLabels(PrinterStringMatrix matrix) {
    String[][] colLabels = matrix.columnLabels();
    int[] colLabelWidths = matrix.columnLabelWidths();
    for (int col = 0; col < colLabelWidths.length; col++) {
      String[] labelStack = colLabels[col];
      int width = colLabelWidths[col];
      for (int i = 0; i < labelStack.length; i++) {
        labelStack[i] = alignLeft(labelStack[i], width);
      }
    }
  }

  private List<String> buildRowLines(PrinterStringMatrix matrix) {
    double[][] probabilities2D = matrix.probabilities2D();
    String probabilityFormatter = configs.getProbabilityFormatter();
    String[][] rowLabels = matrix.rowLabels();
    int[] columnWidths = matrix.columnLabelWidths();
    List<String> strings = new ArrayList<>();
    if (rowLabels.length == 0) {
      strings.add(
          buildSingleRow(new String[0], probabilities2D[0], columnWidths, probabilityFormatter));
      return strings;
    }
    for (int row = 0; row < rowLabels.length; row++) {
      strings.add(
          buildSingleRow(rowLabels[row], probabilities2D[row], columnWidths, probabilityFormatter));
    }
    return strings;
  }

  private List<String> buildColumnLabelLines(PrinterStringMatrix matrix, int totalLineLength) {
    String[][] columnLabels = matrix.columnLabels();
    int rows = columnLabels[0].length;
    List<String> strings = new ArrayList<>();
    for (int row = 0; row < rows; row++) {
      StringBuilder sb = new StringBuilder("|");
      for (String[] columnLabel : columnLabels) {
        sb.append(columnLabel[row]).append("|");
      }
      strings.add(alignRight(sb.toString(), totalLineLength).replaceFirst(" ", "|"));
    }
    return strings;
  }

  private String alignLeft(String text, int width) {
    return String.format(LEFT_ALIGN_FORMAT.formatted(width), text);
  }

  private String buildSingleRow(
      String[] rowLabel, double[] rowProbs, int[] columnWidths, String probabilityFormatter) {
    StringBuilder sb = new StringBuilder("|");
    Arrays.stream(rowLabel).forEach(label -> sb.append(label).append("|"));
    IntStream.range(0, rowProbs.length)
        .mapToObj(
            i -> alignRight(String.format(probabilityFormatter, rowProbs[i]), columnWidths[i]))
        .forEach(s -> sb.append(s).append("|"));
    return sb.toString();
  }

  private String alignRight(String text, int width) {
    return String.format(RIGHT_ALIGN_FORMAT.formatted(width), text);
  }
}
