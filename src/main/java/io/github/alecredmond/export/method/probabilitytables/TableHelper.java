package io.github.alecredmond.export.method.probabilitytables;

import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import java.io.Serializable;
import java.util.Collection;

public interface TableHelper<T extends ProbabilityTable> {
  Double getProbability(Collection<NodeState> states);

  <S extends Serializable> Double getProbabilityFromIDs(Collection<S> stateIds);

  T copyTable();

  void marginalizeTable();

  void setSafeMode(boolean safeMode);
}
