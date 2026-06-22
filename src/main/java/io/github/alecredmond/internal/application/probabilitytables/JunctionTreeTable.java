package io.github.alecredmond.internal.application.probabilitytables;

import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.method.probabilitytables.tablehelpers.JunctionTreeTableHelper;

public interface JunctionTreeTable extends ProbabilityTable {
  ProbabilityVector getBackupVector();

  JunctionTreeTableHelper getHelper();
}
