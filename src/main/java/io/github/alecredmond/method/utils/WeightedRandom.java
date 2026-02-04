package io.github.alecredmond.method.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import lombok.Getter;


@Getter
public class WeightedRandom<T> {
  public static final Random RANDOM = new Random();
  private final Map<T, Double> cumulativeWeights;
  private final double totalWeight;


  public <R extends T, E extends Number & Comparable<? super E>> WeightedRandom(Map<R, E> weights) {
    if (weights.isEmpty()) {
      throw new IllegalArgumentException("Weights map cannot be empty");
    }
    this.cumulativeWeights = buildSortedWeightsMap(weights);
    this.totalWeight = cumulativeWeights.values().stream().mapToDouble(Double::doubleValue).sum();
  }


  private <R extends T, E extends Number & Comparable<? super E>>
      Map<T, Double> buildSortedWeightsMap(Map<R, E> weightedMap) {
    Map<T, Double> sortedMap = new LinkedHashMap<>();
    weightedMap.entrySet().stream()
        .sorted(Map.Entry.<R, E>comparingByValue().reversed())
        .forEach(entry -> sortedMap.put(entry.getKey(), entry.getValue().doubleValue()));
    return sortedMap;
  }


  public T nextRandom() {
    return nextRandomFromMap(cumulativeWeights);
  }

  public <S> S nextRandomFromMap(Map<S, Double> weights) {
    double randomValue = RANDOM.nextDouble() * totalWeight;
    for (Map.Entry<S, Double> entry : weights.entrySet()) {
      randomValue -= entry.getValue();
      if (randomValue <= 0.0) {
        return entry.getKey();
      }
    }
    return null;
  }

  public static synchronized <R, E extends Number> R nextRandom(Map<R, E> weights) {
    if (weights.isEmpty()) {
      throw new IllegalArgumentException("nextRandom received an empty weights map!");
    }
    double totalWeight = weights.values().stream().mapToDouble(Number::doubleValue).sum();
    double randomValue = RANDOM.nextDouble() * totalWeight;
    for (Map.Entry<R, E> entry : weights.entrySet()) {
      randomValue -= entry.getValue().doubleValue();
      if (randomValue <= 0.0) return entry.getKey();
    }
    return null;
  }
}
