package io.github.alecredmond.application.sampler;

import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.method.sampler.SampleUtils;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class Sample {
  private final NodeState[] rawSampledStates;
  private NodeState[] sampledStates;

  public Sample(NodeState[] rawSampledStates) {
    this.rawSampledStates = rawSampledStates;
    this.sampledStates = Arrays.copyOf(rawSampledStates, rawSampledStates.length);
  }

  public int size() {
    return sampledStates.length;
  }

  public <T extends Collection<NodeState>, S extends T> T getStateCollection(Supplier<S> supplier) {
    return SampleUtils.getStateCollection(sampledStates, supplier);
  }

  public <T extends Collection<R>, S extends T, R> T getStateIdCollection(
      Supplier<S> supplier, Class<R> idClass) {
    try {
      return SampleUtils.getStateIdCollection(sampledStates, supplier, idClass);
    } catch (ClassCastException e) {
      log.error(e.getMessage());
      log.error("Error casting NodeState ids to {}", idClass);
      return supplier.get();
    }
  }
}
