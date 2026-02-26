package io.github.alecredmond.method.sampler;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.sampler.Sample;
import io.github.alecredmond.method.sampler.export.SampleCollection;
import io.github.alecredmond.exceptions.SampleValidationException;
import java.util.Map;
import java.util.stream.IntStream;
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
      sampleStatesInCorrectOrder();
      return collection;
    } catch (SampleValidationException e) {
      log.error(e.getMessage());
      return null;
    }
  }

  private void sampleCountCorrect() {
    int totalSamples = collection.getTotalSamples();
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

  private void sampleStatesInCorrectOrder() {
    Node[] nodes = collection.getNodes();
    boolean ok =
        collection.getSampleMap().keySet().stream()
            .map(Sample::getRawSample)
            .allMatch(
                rawSample ->
                    IntStream.range(0, rawSample.length)
                        .allMatch(i -> rawSample[i].getNode().equals(nodes[i])));
    if (ok) {
      return;
    }
    throw new SampleValidationException("Raw samples were found out of order!");
  }
}
