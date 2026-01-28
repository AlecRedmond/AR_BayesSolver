package io.github.alecredmond.method.utils;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MapCollector {
  public <T, U, V> Map<T, U> convertToMap(
      Function<V, Map.Entry<T, U>> entryConversionFunction, Collection<V> input) {
    return input.stream()
        .map(entryConversionFunction)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }
}
