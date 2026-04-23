package io.github.alecredmond.internal.method.probabilitytables.probabilityvector.iteratorfactory;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.ObservationCopier;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.VectorIterator;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class ObservationCopierFactory extends BaseVectorIteratorFactory<ObservationCopier> {
  private final ProbabilityVector backupVector;
  private final ProbabilityVector mainVector;
  private Map<Node, NodeState> observations;

  public ObservationCopierFactory(ProbabilityVector mainVector, ProbabilityVector backupVector) {
    super();
    this.mainVector = mainVector;
    this.backupVector = backupVector;
  }

  public VectorIterator build(Collection<NodeState> observedStates) {
    this.observations = NodeUtils.generateRequest(observedStates);
    return buildIterator(mainVector);
  }

  @Override
  public ObservationCopier supplyIterator() {
    return new ObservationCopier(vectorOdometer, backupVector);
  }

  @Override
  protected Function<Node, NodeState> initialStatePositionSetter() {
    return n -> observations.containsKey(n) ? observations.get(n) : n.getNodeStates().getFirst();
  }

  @Override
  protected Predicate<Node> checkLockOuter() {
    return node -> false;
  }

  @Override
  protected Predicate<Node> checkLockInner() {
    return observations::containsKey;
  }
}
