package io.github.alecredmond.method.sampler.export;

import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.sampler.SampleCollectionData;
import io.github.alecredmond.method.network.NetworkDataUtils;
import io.github.alecredmond.method.sampler.internal.SampleCollectionUtils;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SampleCollection {
  private final SampleCollectionData collectionData;
  private final BayesianNetworkData networkData;

  public SampleCollection(
      int totalSamples,
      Map<Sample, Integer> sampleMap,
      Map<Node, NodeState> networkObservations,
      Node[] nodes,
      BayesianNetworkData networkData) {
    this.collectionData =
        new SampleCollectionData(totalSamples, sampleMap, networkObservations, nodes);
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

  public List<Sample> getDistinctSamples() {
    return getSampleMap().keySet().stream().toList();
  }

  public Map<Sample, Integer> getSampleMap() {
    return collectionData.getSampleMap();
  }

  public <T> void setExportNodesById(Collection<T> nodeIds) {
    Set<Node> nodes = NetworkDataUtils.getNodesByID(nodeIds, networkData);
    SampleCollectionUtils.setExportSamples(this, nodes);
  }

  public void setExportNodes(Collection<Node> nodes) {
    SampleCollectionUtils.setExportSamples(this, nodes);
  }

  public void resetExportNodes() {
    SampleCollectionUtils.resetExportNodes(this);
  }

  public <T> int countSamplesWithStateIds(Collection<T> stateIds) {
    return countSamplesWithStates(NetworkDataUtils.getStatesByID(stateIds, networkData));
  }

  public int countSamplesWithStates(Collection<NodeState> states) {
    return SampleCollectionUtils.countSamplesIncludingStates(this, states);
  }

  public Map<Sample, Integer> getSamplesIncludingStates(Collection<NodeState> includedStates) {
    return SampleCollectionUtils.buildSampleMapIncludingStates(this, includedStates);
  }

  public <T> Map<Sample, Integer> getSamplesIncludingStateIds(Collection<T> includedStateIds) {
    return SampleCollectionUtils.buildSampleMapIncludingStates(
        this, NetworkDataUtils.getStatesByID(includedStateIds, networkData));
  }
}
