package io.github.alecredmond.internal.application.probabilitytables;

import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.method.probabilitytables.tablehelpers.JunctionTreeTableQueryTool;

public interface JunctionTreeTable extends ProbabilityTable {
  ProbabilityVector getBackupVector();

  JunctionTreeTableQueryTool getQueryTool();
}
