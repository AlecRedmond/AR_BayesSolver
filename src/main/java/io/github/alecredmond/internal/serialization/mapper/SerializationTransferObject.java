package io.github.alecredmond.internal.serialization.mapper;

import java.io.Serializable;

public interface SerializationTransferObject<T> extends Serializable {
  SerializationTransferObject<T> serialize(T t);

  T deSerialize(SerializationData data);
}
