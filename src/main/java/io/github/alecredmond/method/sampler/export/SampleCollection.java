package io.github.alecredmond.method.sampler.export;

import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.sampler.Sample;
import io.github.alecredmond.application.sampler.SampleCollectionData;
import io.github.alecredmond.method.network.NetworkDataUtils;
import io.github.alecredmond.method.sampler.SampleUtils;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SampleCollection {
  private final SampleCollectionData sampleData;
  private final BayesianNetworkData networkData;

  public SampleCollection(
      int totalSamples,
      Map<Sample, Integer> sampleMap,
      Map<Node, NodeState> networkObservations,
      Node[] nodes,
      BayesianNetworkData networkData) {
    this.sampleData = new SampleCollectionData(totalSamples, sampleMap, networkObservations, nodes);
    this.networkData = networkData;
  }

  public Map<Node, NodeState> getNetworkObservations() {
    return sampleData.getNetworkObservations();
  }

  public Node[] getNodes() {
    return sampleData.getNodes();
  }

  public boolean isEmpty() {
    return size() == 0;
  }

  public int size() {
    return sampleData.getTotalSamples();
  }

  public List<Sample> getDistinctSamples() {
    return getSampleMap().keySet().stream().toList();
  }

  public Map<Sample, Integer> getSampleMap() {
    return sampleData.getSampleMap();
  }

  public <T> void setExportNodesById(Collection<T> nodeIds) {
    Set<Node> nodes = NetworkDataUtils.getNodesByID(nodeIds, networkData);
    SampleUtils.setExportSamples(this, nodes);
  }

  public void setExportNodes(Collection<Node> nodes) {
    SampleUtils.setExportSamples(this, nodes);
  }

  public void resetExportNodes() {
    SampleUtils.resetExportSamples(this);
  }

  public <T extends Collection<E>, E extends Collection<R>, U extends T, F extends E, R>
      T getNestedSampleIds(
          Supplier<U> collectionSupplier, Supplier<F> sampleSupplier, Class<R> stateIdClass) {
    try {
      return SampleUtils.getNestedSampleIds(this, collectionSupplier, sampleSupplier, stateIdClass);
    } catch (ClassCastException e) {
      log.error("{}", e.getMessage());
      log.error("Error casting NodeState ids to {}", stateIdClass);
      return collectionSupplier.get();
    }
  }

  public <T extends Collection<E>, E extends Collection<NodeState>, U extends T, F extends E>
      T getNestedSamples(Supplier<U> collectionSupplier, Supplier<F> sampleSupplier) {
    return SampleUtils.getNestedSamples(this, collectionSupplier, sampleSupplier);
  }

  public <T> int sizeIncludingStateIds(Collection<T> stateIds) {
    return sizeIncludingStates(NetworkDataUtils.getStatesByID(stateIds, networkData));
  }

  public int sizeIncludingStates(Collection<NodeState> states) {
    return SampleUtils.countIncludingStates(this, states);
  }

  public Map<Sample, Integer> samplesIncludingStates(Collection<NodeState> states) {
    return SampleUtils.samplesIncludingStates(this, states);
  }

  public <T> Map<Sample, Integer> samplesIncludingStateIds(Collection<T> stateIds) {
    return SampleUtils.samplesIncludingStates(
        this, NetworkDataUtils.getStatesByID(stateIds, networkData));
  }
}
