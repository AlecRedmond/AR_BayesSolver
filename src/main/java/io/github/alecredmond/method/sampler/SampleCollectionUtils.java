package io.github.alecredmond.method.sampler;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.sampler.SampleCollectionData;
import io.github.alecredmond.method.sampler.export.SampleCollection;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SampleCollectionUtils {
  private SampleCollectionUtils() {}

  public static void limitToNodes(SampleCollectionData data, Collection<Node> nodes) {
      Set<Node> nodeSet = new HashSet<>(nodes);
      int[] indexes =
  }
}
