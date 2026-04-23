package io.github.alecredmond.internal.method.probabilitytables.probabilityvector.iteratorfactory;

import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.VectorIterator;

@FunctionalInterface
public interface VectorIteratorFactorySupplier<T extends VectorIterator,R extends BaseVectorIteratorFactory<T>> {
  T supplyIterator(R factory);
}
