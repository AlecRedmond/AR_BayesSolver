package io.github.alecredmond.internal.method.probabilitytables.printerformat;

import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.application.printer.PrinterPropertyConfigs;
import io.github.alecredmond.internal.application.printer.PrinterStringMatrix;
import java.util.Arrays;
import java.util.Objects;

public abstract class PrinterMatrixGeneratorBase {

  public PrinterStringMatrix generate(PrinterPropertyConfigs configs) {
    PrinterStateMatrix matrix = buildPrinterStateMatrix();
    AxisLabels rowLabels = buildRowLabels(matrix);
    AxisLabels columnLabels = buildColumnLabels(matrix, configs);
    return new PrinterStringMatrix(
        matrix.tableTitle(),
        rowLabels.labels,
        rowLabels.widths,
        columnLabels.labels,
        columnLabels.widths,
        matrix.probabilities2D);
  }

  protected abstract PrinterStateMatrix buildPrinterStateMatrix();

  private AxisLabels buildRowLabels(PrinterStateMatrix matrix) {
    NodeState[][] rowStates = matrix.rowStates();
    String[][] labels = new String[rowStates.length][];
    if (labels.length == 0) {
      return new AxisLabels(labels, new int[0]);
    }
    int[] labelWidths = new int[rowStates[0].length];
    for (int i = 0; i < rowStates.length; i++) {
      String[] substrings = statesToString(rowStates[i]);
      checkReplaceMaxWidth(substrings, labelWidths);
      labels[i] = substrings;
    }
    return new AxisLabels(labels, labelWidths);
  }

  private AxisLabels buildColumnLabels(PrinterStateMatrix matrix, PrinterPropertyConfigs configs) {
    NodeState[][] columnStates = matrix.columnStates();
    String[][] labels = new String[columnStates.length][];
    int[] labelWidths = new int[columnStates.length];
    int probabilityCharLength = configs.getProbabilityCharLength();
    for (int i = 0; i < columnStates.length; i++) {
      String[] subStrings = statesToString(columnStates[i]);
      labelWidths[i] = maximumWidthOfStateStrings(subStrings, probabilityCharLength);
      labels[i] = subStrings;
    }
    return new AxisLabels(labels, labelWidths);
  }

  private static String[] statesToString(NodeState[] states) {
    return Arrays.stream(states)
        .map(NodeState::getId)
        .map(Objects::toString)
        .toArray(String[]::new);
  }

  private static void checkReplaceMaxWidth(String[] subStrings, int[] rowWidths) {
    for (int j = 0; j < subStrings.length; j++) {
      String stateString = subStrings[j];
      if (stateString.length() <= rowWidths[j]) continue;
      rowWidths[j] = stateString.length();
    }
  }

  private static int maximumWidthOfStateStrings(String[] subStrings, int probabilityCharLength) {
    int maxStringLength = Arrays.stream(subStrings).mapToInt(String::length).max().orElse(0);
    return Math.max(probabilityCharLength, maxStringLength);
  }

  protected record AxisLabels(String[][] labels, int[] widths) {}

  protected record PrinterStateMatrix(
      String tableTitle,
      NodeState[][] columnStates,
      NodeState[][] rowStates,
      double[][] probabilities2D) {}
}
