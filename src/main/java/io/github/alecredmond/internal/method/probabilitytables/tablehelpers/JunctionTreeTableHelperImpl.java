package io.github.alecredmond.internal.method.probabilitytables.tablehelpers;

import io.github.alecredmond.export.method.probabilitytables.TableHelper;
import io.github.alecredmond.internal.application.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;

public class JunctionTreeTableHelperImpl extends TableHelperBase<JunctionTreeTable>
    implements TableHelper<JunctionTreeTable> {

  public JunctionTreeTableHelperImpl(JunctionTreeTable table) {
    super(table);
  }

  @Override
  public void marginalizeTable() {
    TableUtils.marginalizeJointTable(table);
  }
}
