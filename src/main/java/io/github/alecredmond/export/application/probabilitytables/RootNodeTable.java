package io.github.alecredmond.export.application.probabilitytables;

import io.github.alecredmond.export.method.probabilitytables.RootNodeTableHelper;

public interface RootNodeTable extends NetworkTable {
  @Override
  RootNodeTableHelper getHelper();
}
