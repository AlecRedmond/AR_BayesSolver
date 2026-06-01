package io.github.alecredmond.internal.method.sampler;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.sampler.Sample;
import io.github.alecredmond.internal.application.sampler.SampleData;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.util.Collection;
import java.util.function.Supplier;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class SampleImpl implements Sample {
  private final SampleData sampleData;

  public SampleImpl(NodeState[] rawArray) {
    this.sampleData = new SampleData(rawArray);
  }

  public int count() {
    return sampleData.getCount();
  }

  public NodeState[] getAllStates() {
    return sampleData.getRawStateArray();
  }

  public NodeState[] getDisplayedStates() {
    return sampleData.getExportStateArray();
  }

  public <T extends Collection<NodeState>, S extends T> T getDisplayedStates(Supplier<S> supplier) {
    return SampleUtils.getSampleCollection(sampleData, supplier);
  }

  public void displayAllNodes() {
    SampleUtils.resetExportArray(sampleData);
    SampleUtils.rebuildStateCollection(sampleData, sampleData.getSupplier());
  }

  public void setDisplayedNodes(Collection<Node> nodes) {
    SampleUtils.setExportArrayFromNodes(nodes, sampleData);
    SampleUtils.rebuildStateCollection(sampleData, sampleData.getSupplier());
  }

  @Override
  public String toString() {
    return "%s : %d"
        .formatted(
            NodeUtils.formatStatesToString(sampleData.getStateCollection()), sampleData.getCount());
  }
}
