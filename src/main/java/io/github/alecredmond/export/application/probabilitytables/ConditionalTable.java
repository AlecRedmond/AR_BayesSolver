package io.github.alecredmond.export.application.probabilitytables;

import io.github.alecredmond.export.method.probabilitytables.ConditionalTableHelper;

public interface ConditionalTable extends NetworkTable {
  @Override
  ConditionalTableHelper getHelper();
}
