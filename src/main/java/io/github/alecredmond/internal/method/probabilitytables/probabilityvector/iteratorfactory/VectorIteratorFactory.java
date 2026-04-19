package io.github.alecredmond.internal.method.probabilitytables.probabilityvector.iteratorfactory;

import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.VectorIterator;

public interface VectorIteratorFactory<T> {
  VectorIterator build(ProbabilityVector vector, T requestItem);
}
