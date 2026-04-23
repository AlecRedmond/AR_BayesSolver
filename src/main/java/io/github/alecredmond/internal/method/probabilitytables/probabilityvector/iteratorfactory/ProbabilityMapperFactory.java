package io.github.alecredmond.internal.method.probabilitytables.probabilityvector.iteratorfactory;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.BaseVectorIterator.UpdateConsumer;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.ProbabilityMapper;
import java.util.function.Predicate;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ProbabilityMapperFactory extends BaseVectorIteratorFactory<ProbabilityMapper> {

  public ProbabilityMapper build(ProbabilityTable table) {
    return super.buildIterator(table.getVector());
  }

  @Override
  protected UpdateConsumer updateConsumer() {
    return UPDATE_STATES;
  }

  @Override
  protected Predicate<Node> checkLockOuter() {
    return n -> true;
  }

  @Override
  protected Predicate<Node> checkLockInner() {
    return n -> false;
  }

  @Override
  public ProbabilityMapper supplyIterator() {
    return new ProbabilityMapper(vectorOdometer);
  }
}
