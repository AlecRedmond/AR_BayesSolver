package io.github.alecredmond.internal.application.network.cptmapper;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.probabilitytables.NetworkTable;
import io.github.alecredmond.internal.method.constraints.strategies.CPTConstraintValidator;
import io.github.alecredmond.internal.method.inference.solver.cptmapper.CptMapperIterator;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public abstract class DirectMapperNodeInput<
    T extends NetworkTable, P extends ProbabilityConstraint, V extends CPTConstraintValidator<P>> {
  protected final Node node;
  protected final T networkTable;
  protected final V validator;
  protected final int minimumCPTEntries;
  protected final List<P> validConstraints;
  protected final List<P> addedConstraints;

  protected DirectMapperNodeInput(Node node, T networkTable, V validator) {
    this.node = node;
    this.networkTable = networkTable;
    this.validator = validator;
    this.minimumCPTEntries = calculateMinimumCptEntries(node, networkTable);
    this.validConstraints = new ArrayList<>();
    this.addedConstraints = new ArrayList<>();
  }

  private int calculateMinimumCptEntries(Node node, T networkTable) {
    int cptEntries = networkTable.getProbabilities().length;
    int numEventStates = node.getNodeStates().size();
    return (cptEntries / numEventStates) * (numEventStates - 1);
  }

  public boolean runIterator() {
    return addedConstraints.addAll(buildIterator().directMapCPTs());
  }

  protected abstract CptMapperIterator<T, P> buildIterator();
}
