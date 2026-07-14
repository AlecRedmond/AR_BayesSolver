package io.github.alecredmond.internal.method.probabilitytables.printerformat;

import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ConditionalTable;
import io.github.alecredmond.export.probabilitytables.cptentry.CptEntry;
import java.util.List;

public class ConditionalMatrixGenerator extends PrinterMatrixGeneratorBase
    implements PrinterMatrixGenerator {

  private final ConditionalTable table;

  public ConditionalMatrixGenerator(ConditionalTable table) {
    this.table = table;
  }

  @Override
  protected PrinterStateMatrix buildPrinterStateMatrix() {
    List<NodeState> states = table.getNetworkNode().getNodeStates();
    int cols = states.size();
    int rows = table.getProbabilities().length / cols;
    int conditionSize = table.getConditions().size();

    String tableName = table.getTableName().toString();
    NodeState[][] rowStates = new NodeState[rows][conditionSize];
    NodeState[][] colStates = new NodeState[cols][1];
    double[][] probabilities2D = new double[rows][cols];

    fillColLabels(colStates, states);

    int[] rowIndexContainer = {0};
    table.iterateOverConditions(
        cptRow -> {
          int rowIndex = rowIndexContainer[0]++;
          rowStates[rowIndex] = cptRow.conditionStates().toArray(NodeState[]::new);
          List<CptEntry> entries = cptRow.rowEntries();
          for (int i = 0; i < entries.size(); i++) {
            probabilities2D[rowIndex][i] = entries.get(i).probability();
          }
        });

    return new PrinterStateMatrix(tableName, colStates, rowStates, probabilities2D);
  }

  private void fillColLabels(NodeState[][] colLabels, List<NodeState> states) {
    for (int i = 0; i < states.size(); i++) {
      colLabels[i][0] = states.get(i);
    }
  }
}
