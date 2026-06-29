package io.github.alecredmond.internal.application.probabilitytables;

import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.application.probabilitytables.base.ProbabilityTableBase;
import io.github.alecredmond.internal.method.probabilitytables.tablebuilders.TableBuilderData;
import io.github.alecredmond.internal.method.probabilitytables.tablehelpers.JunctionTreeTableQueryTool;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class JunctionTreeTableImpl extends ProbabilityTableBase<JunctionTreeTableQueryTool>
    implements JunctionTreeTable {
  @EqualsAndHashCode.Exclude private final ProbabilityVector backupVector;

  public JunctionTreeTableImpl(TableBuilderData tableBuilderData, ProbabilityVector backupVector) {
    super(tableBuilderData);
    this.backupVector = backupVector;
  }
}
