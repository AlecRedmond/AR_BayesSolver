package io.github.alecredmond.internal.application.probabilitytables;

import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.application.probabilitytables.base.ProbabilityTableData;
import io.github.alecredmond.internal.method.probabilitytables.tablebuilders.TableBuilderData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class JunctionTreeTableData extends ProbabilityTableData {
  @EqualsAndHashCode.Exclude private final ProbabilityVector backupVector;

  public JunctionTreeTableData(TableBuilderData tableBuilderData, ProbabilityVector backupVector) {
    super(tableBuilderData);
    this.backupVector = backupVector;
  }
}
