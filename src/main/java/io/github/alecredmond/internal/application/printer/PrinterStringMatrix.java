package io.github.alecredmond.internal.application.printer;

public record PrinterStringMatrix(
    String tableTitle,
    String[][] rowLabels,
    int[] rowLabelWidths,
    String[][] columnLabels,
    int[] columnLabelWidths,
    double[][] probabilities2D) {}
