package io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.application.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.iteratorutils.OdometerResetLogic;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class ObservationCopier implements OdometerResetLogic {
  private final ProbabilityVector mainVector;
  private final ProbabilityVector backupVector;
  private final VectorIterator iterator;
  private Map<Node, NodeState> observations;

  public ObservationCopier(JunctionTreeTable table) {
    this.observations = new HashMap<>();
    this.backupVector = table.getBackupVector();
    this.mainVector = table.getVector();
    this.iterator = new VectorIterator(mainVector, this);
  }

  public void observeTable(Collection<NodeState> observedStates) {
    this.observations = NodeUtils.generateRequest(observedStates);
    iterator.reset();
    double[] observed = mainVector.getProbabilities();
    Arrays.fill(observed, 0.0);
    double[] backup = backupVector.getProbabilities();
    iterator.iterateInner((o, i) -> observed[i] = backup[i]);
  }

  @Override
  public Function<Node, NodeState> initialStatePositionSetter() {
    return n -> observations.containsKey(n) ? observations.get(n) : n.getNodeStates().getFirst();
  }

  @Override
  public Predicate<Node> checkLockOuter() {
    return node -> false;
  }

  @Override
  public Predicate<Node> checkLockInner() {
    return observations::containsKey;
  }
}
