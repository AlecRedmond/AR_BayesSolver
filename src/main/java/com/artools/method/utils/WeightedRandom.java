package com.artools.method.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/**
 * A utility class for performing weighted-random selections from a collection of items.
 * <p>
 * This class provides two primary ways to select items:
 * <ol>
 * <li><b>Instance Method:</b> For scenarios where many selections will be made from the same set of weights.
 * The constructor pre-calculates cumulative weights for efficiency.</li>
 * <li><b>Static Method:</b> For one-off selections where an instance is not needed. This is convenient
 * but less performant if called repeatedly with the same weights.</li>
 * </ol>
 *
 * @param <T> The type of the objects to be selected.
 */
public class WeightedRandom<T> {
    private static final Random RANDOM = new Random();
    private final Map<T, Double> cumulativeWeights;
    private final double totalWeight;

    /**
     * Constructs a weighted random instance for efficient repeated selections.
     *
     * @param weights a map of objects and their associated weights.
     * @throws IllegalArgumentException if the weightsMap is empty
     */
    public <R extends T, E extends Number & Comparable<? super E>> WeightedRandom(Map<R, E> weights) {
        if (weights.isEmpty()) {
            throw new IllegalArgumentException("Weights map cannot be empty");
        }
        this.cumulativeWeights = buildSortedWeightsMap(weights);
        this.totalWeight = cumulativeWeights.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    /** Builds a map of cumulative weights, sorted in descending order */
    private <R extends T, E extends Number & Comparable<? super E>>
    Map<T, Double> buildSortedWeightsMap(Map<R, E> weightedMap) {
        Map<T, Double> sortedMap = new LinkedHashMap<>();
        weightedMap.entrySet().stream()
                .sorted(Map.Entry.<R, E>comparingByValue().reversed())
                .forEach(entry -> sortedMap.put(entry.getKey(), entry.getValue().doubleValue()));
        return sortedMap;
    }

    /**
     * Returns a randomly selected object based on its weight.
     *
     * @return A weighted-random object.
     */
    public T nextRandom() {
        double randomValue = RANDOM.nextDouble() * totalWeight;
        for (Map.Entry<T, Double> entry : cumulativeWeights.entrySet()) {
            randomValue -= entry.getValue();
            if (randomValue <= 0.0) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * A static method variant of the nextRandom method. Advisable in single-use cases.
     *
     * @param weights a map of objects and their associated weights
     * @return a weighted-random object
     * @throws IllegalArgumentException if the weightsMap is empty
     */
    public static <R, E extends Number> R nextRandom(Map<R, E> weights) {
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
