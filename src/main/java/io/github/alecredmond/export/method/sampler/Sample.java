package io.github.alecredmond.export.method.sampler;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.sampler.SampleData;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.sampler.SampleUtils;
import java.util.Collection;
import java.util.function.Supplier;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class Sample {
  private final SampleData sampleData;

  public Sample(NodeState[] rawArray) {
    this.sampleData = new SampleData(rawArray);
  }

  public int size() {
    return sampleData.getExportArray().length;
  }

  public <T extends Collection<NodeState>, S extends T> T getSampleCollection(
      Supplier<S> supplier) {
    return SampleUtils.getSampleCollection(sampleData, supplier);
  }

  public Collection<NodeState> getSampleCollection() {
    return sampleData.getStateCollection();
  }

  public void resetExportNodes() {
    SampleUtils.resetExportArray(sampleData);
    SampleUtils.rebuildStateCollection(sampleData, sampleData.getSupplier());
  }

  public void setExportNodes(Collection<Node> nodeCollection) {
    SampleUtils.setExportArrayFromNodes(nodeCollection, sampleData);
    SampleUtils.rebuildStateCollection(sampleData, sampleData.getSupplier());
  }

  public <R extends Collection<NodeState>> void setSampleSupplier(Supplier<R> supplier) {
    SampleUtils.setSampleSupplier(sampleData, supplier);
  }

  public NodeState[] getRawArray() {
    return sampleData.getRawArray();
  }

  public <T extends Collection<R>, S extends T, R> T getSampledStateIds(
      Supplier<S> supplier, Class<R> idClass) {
    try {
      return SampleUtils.getStateIdCollection(getExportArray(), supplier, idClass);
    } catch (ClassCastException e) {
      log.error(e.getMessage());
      log.error("Error casting NodeState ids to {}", idClass);
      return supplier.get();
    }
  }

  public NodeState[] getExportArray() {
    return sampleData.getExportArray();
  }

  @Override
  public String toString() {
    return "%s : %d"
        .formatted(
            NodeUtils.formatStatesToString(sampleData.getStateCollection()), sampleData.getCount());
  }

  void setCount(Integer integer) {
    sampleData.setCount(integer);
  }
}
