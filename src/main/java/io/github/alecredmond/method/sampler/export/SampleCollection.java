package io.github.alecredmond.method.sampler.export;

import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.sampler.Sample;
import io.github.alecredmond.application.sampler.SampleCollectionData;
import io.github.alecredmond.method.sampler.SampleCollectionUtils;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    return getTotalSamples() == 0;
  }

  public int getTotalSamples() {
    return sampleData.getTotalSamples();
  }

  public int size() {
    return getTotalSamples();
  }

  public List<Sample> getDistinctSamples() {
    return getSampleMap().keySet().stream().toList();
  }

  public Map<Sample, Integer> getSampleMap() {
    return sampleData.getSampleMap();
  }

  public <T> void limitToNodesById(Collection<T> nodeIds) {
      Set<Node> nodes = new NetworkNode
    SampleCollectionUtils.limitToNodes(sampleData,nodes);
  }
}
