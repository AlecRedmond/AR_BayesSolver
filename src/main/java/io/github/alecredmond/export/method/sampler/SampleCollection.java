package io.github.alecredmond.export.method.sampler;

import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.sampler.SampleCollectionData;
import io.github.alecredmond.internal.method.network.NetworkDataUtils;
import io.github.alecredmond.internal.method.sampler.SampleCollectionUtils;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SampleCollection {
  private final SampleCollectionData collectionData;
  private final BayesianNetworkData networkData;

  public SampleCollection(
      int totalSamples,
      List<Sample> samples,
      Map<Node, NodeState> networkObservations,
      Node[] nodes,
      BayesianNetworkData networkData) {
    this.collectionData =
        new SampleCollectionData(totalSamples, samples, networkObservations, nodes);
    this.networkData = networkData;
  }

  public Map<Node, NodeState> getNetworkObservations() {
    return collectionData.getNetworkObservations();
  }

  public Node[] getNodes() {
    return collectionData.getNodes();
  }

  public boolean isEmpty() {
    return size() == 0;
  }

  public int size() {
    return collectionData.getTotalSamples();
  }

  public <T extends Serializable> void setExportNodesById(Collection<T> nodeIds) {
    Set<Node> nodes = NetworkDataUtils.getNodesByID(nodeIds, networkData);
    setExportNodes(nodes);
  }

  public void setExportNodes(Collection<Node> nodes) {
    SampleCollectionUtils.applyToSamples(this, s -> s.setExportNodes(nodes));
  }

  public void resetExportNodes() {
    SampleCollectionUtils.applyToSamples(this, Sample::resetExportNodes);
  }

  public <R extends Collection<NodeState>> void setSupplier(Supplier<R> supplier) {
    SampleCollectionUtils.applyToSamples(this, s -> s.setSampleSupplier(supplier));
    collectionData.setSampleSupplier(supplier);
  }

  public <T extends Serializable> int countSamplesWithStateIds(Collection<T> stateIds) {
    return countSamplesWithStates(NetworkDataUtils.getStatesByID(stateIds, networkData));
  }

  public int countSamplesWithStates(Collection<NodeState> states) {
    if (states.isEmpty()) {
      return size();
    }
    return SampleCollectionUtils.countSamplesIncludingStates(this, states);
  }

  public List<Sample> getSamplesIncludingStates(Collection<NodeState> includedStates) {
    return SampleCollectionUtils.listSamplesIncludingStates(this, includedStates);
  }

  public <T extends Serializable> List<Sample> getSamplesIncludingStateIds(
      Collection<T> includedStateIds) {
    return SampleCollectionUtils.listSamplesIncludingStates(
        this, NetworkDataUtils.getStatesByID(includedStateIds, networkData));
  }

  @Override
  public String toString() {
    return getSamples().stream().map(Sample::toString).collect(Collectors.joining("\n"));
  }

  public List<Sample> getSamples() {
    return collectionData.getSamples();
  }
}
