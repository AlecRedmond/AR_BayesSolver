package io.github.alecredmond.internal.method.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MapUtils {
  public static <R, S, T, U> Map<R, S> convertEntries(
      Map<T, U> input, Function<T, R> keyConverter, Function<U, S> valConverter) {
    return input.entrySet().stream()
        .map(e -> Map.entry(keyConverter.apply(e.getKey()), valConverter.apply(e.getValue())))
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

  public static <I, K, U> Map<K, U> mapFromInput(
      Stream<I> inputStream, Function<I, K> keyMapper, Function<I, U> valMapper) {
    return mapFromInput(inputStream, keyMapper, valMapper, collectMap());
  }

  private static <I, K, U, M extends Map<K, U>> M mapFromInput(
      Stream<I> inputStream,
      Function<I, K> keyMapper,
      Function<I, U> valMapper,
      Collector<Entry<K, U>, ?, M> collector) {
    return inputStream
        .map(i -> Map.entry(keyMapper.apply(i), valMapper.apply(i)))
        .collect(collector);
  }

  public static <K, U> Collector<Entry<K, U>, ?, Map<K, U>> collectMap() {
    return Collectors.toMap(Entry::getKey, Entry::getValue);
  }

  public static <I, K, U, M extends Map<K, U>> M mapFromInput(
      Stream<I> inputStream,
      Function<I, K> keyMapper,
      Function<I, U> valMapper,
      Supplier<M> mapFactory) {
    return mapFromInput(inputStream, keyMapper, valMapper, collectMap(mapFactory));
  }

  public static <K, U, M extends Map<K, U>> Collector<Entry<K, U>, ?, M> collectMap(
      Supplier<M> mapFactory) {
    return Collectors.toMap(Entry::getKey, Entry::getValue, (x, y) -> y, mapFactory);
  }

  public static <K, U> Map<K, U> filterMap(Map<K, U> map, BiPredicate<K, U> biPredicate) {
    return filterMap(map, biPredicate, HashMap::new);
  }

  public static <K, U, M extends Map<K, U>> M filterMap(
      Map<K, U> map, BiPredicate<K, U> biPredicate, Supplier<M> mapFactory) {
    return filterMap(map, biPredicate, collectMap(mapFactory));
  }

  private static <K, U, M extends Map<K, U>> M filterMap(
      Map<K, U> map, BiPredicate<K, U> biPredicate, Collector<Entry<K, U>, ?, M> collector) {
    return map.entrySet().stream()
        .filter(kuEntry -> biPredicate.test(kuEntry.getKey(), kuEntry.getValue()))
        .collect(collector);
  }

  public static <K, U> Map<K, U> mapFromInput(K[] input, Function<K, U> valMapper) {
    return mapFromInput(input, Function.identity(), valMapper);
  }

  public static <I, K, U> Map<K, U> mapFromInput(
      I[] input, Function<I, K> keyMapper, Function<I, U> valMapper) {
    return mapFromInput(Arrays.stream(input), keyMapper, valMapper, collectMap());
  }

  public static <K, U> Map<K, U> mapFromInput(Collection<K> input, Function<K, U> valMapper) {
    return mapFromInput(input, Function.identity(), valMapper);
  }

  public static <I, K, U> Map<K, U> mapFromInput(
      Collection<I> input, Function<I, K> keyMapper, Function<I, U> valMapper) {
    return mapFromInput(input.stream(), keyMapper, valMapper, collectMap());
  }

  public static <I, K, U> Map<K, U> intStreamMap(
      I[] input, IntFunction<K> keyMapper, IntFunction<U> valMapper) {
    return intStreamMap(input, keyMapper, valMapper, HashMap::new);
  }

  public static <I, K, U, M extends Map<K, U>> M intStreamMap(
      I[] input, IntFunction<K> keyMapper, IntFunction<U> valMapper, Supplier<M> mapFactory) {
    return IntStream.range(0, input.length)
        .mapToObj(n -> Map.entry(keyMapper.apply(n), valMapper.apply(n)))
        .collect(collectMap(mapFactory));
  }

  public static <K, U, M extends Map<K, U>> M intStreamMap(
            double[] input, IntFunction<K> keyMapper, IntFunction<U> valMapper,Supplier<M> mapFactory){
      return IntStream.range(0, input.length)
              .mapToObj(n -> Map.entry(keyMapper.apply(n), valMapper.apply(n)))
              .collect(collectMap(mapFactory));
  }
}
