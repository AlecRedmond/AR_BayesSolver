package io.github.alecredmond.internal.serialization.mapper;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SerializerUtils {
  private SerializerUtils() {}

  public static <T> Serializable[] serializeArray(
      T[] array, Function<T, Serializable> serializableFunction) {
    return Arrays.stream(array).map(serializableFunction).toArray(Serializable[]::new);
  }

  public static <T> T[] deserializeArray(
      Serializable[] array, Function<Serializable, T> tFunction, IntFunction<T[]> generator) {
    return Arrays.stream(array).map(tFunction).toArray(generator);
  }

  public static <T extends Collection<Node>, R extends T> T deSerializeNodes(
      Collection<Serializable> nodeIds, Supplier<R> collectionSupplier, SerializationData data) {
    return deSerializeCollection(nodeIds, data.getNodeIdMap()::get, collectionSupplier);
  }

  public static <T extends Collection<S>, R extends T, S> T deSerializeCollection(
      Collection<Serializable> toDeserialize,
      Function<Serializable, S> deSerializationFunction,
      Supplier<R> collectionSupplier) {
    return toDeserialize.stream()
        .map(deSerializationFunction)
        .collect(Collectors.toCollection(collectionSupplier));
  }

  public static <T extends Collection<NodeState>, R extends T> T deSerializeNodeStates(
      Collection<Serializable> stateIds, Supplier<R> collectionSupplier, SerializationData data) {
    return deSerializeCollection(stateIds, data.getNodeStateIdMap()::get, collectionSupplier);
  }

  public static List<Serializable> serializeNodes(Collection<Node> nodes) {
    return serializeCollection(nodes, Node::getId, ArrayList::new);
  }

  public static <T extends Collection<Serializable>, R extends T, S> T serializeCollection(
      Collection<S> toSerialize,
      Function<S, Serializable> serializatonFunction,
      Supplier<R> collectionSupplier) {
    return toSerialize.stream()
        .map(serializatonFunction)
        .collect(Collectors.toCollection(collectionSupplier));
  }

  public static List<Serializable> serializeNodeStates(Collection<NodeState> states) {
    return serializeCollection(states, NodeState::getId, ArrayList::new);
  }
}
