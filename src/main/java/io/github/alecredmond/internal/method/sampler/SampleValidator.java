package io.github.alecredmond.internal.method.sampler;

import io.github.alecredmond.exceptions.SampleValidationException;
import io.github.alecredmond.export.method.sampler.Sample;
import io.github.alecredmond.export.method.sampler.SampleCollection;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SampleValidator {
  private final SampleCollection collection;

  public SampleValidator(SampleCollection collection) {
    this.collection = collection;
  }

  public SampleCollection validateSamples() {
    try {
      sampleCountCorrect();
      return collection;
    } catch (SampleValidationException e) {
      log.error(e.getMessage());
      return null;
    }
  }

  private void sampleCountCorrect() {
    int totalSamples = collection.size();
    Map<Sample, Integer> sampleMap = collection.getSampleMap();
    int sampleMapCount =
        sampleMap.isEmpty() ? 0 : sampleMap.values().stream().mapToInt(Integer::intValue).sum();
    if (totalSamples == sampleMapCount) {
      return;
    }
    throw new SampleValidationException(
        "Mismatch between expected total samples %d and counted samples %d"
            .formatted(totalSamples, sampleMapCount));
  }
}
