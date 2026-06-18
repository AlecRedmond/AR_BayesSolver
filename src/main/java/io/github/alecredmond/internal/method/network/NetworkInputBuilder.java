package io.github.alecredmond.internal.method.network;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.application.network.NetworkBuilderNode;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.ProbabilityVectorFactory;
import io.github.alecredmond.internal.method.vectoriterator.misciterators.ConstraintBuilderIterator;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NetworkInputBuilder {
  private final ProbabilityVectorFactory vectorFactory = new ProbabilityVectorFactory();
  private List<NetworkBuilderNode> nodeInputs;
  private BayesianNetwork bayesianNetwork;

  public BayesianNetwork buildNetwork(String networkName, List<NetworkBuilderNode> nodeInputs) {
    this.nodeInputs = nodeInputs;
    this.bayesianNetwork = BayesianNetwork.newNetwork(networkName);
    addAllNodes();
    parentNodes();
    createCptConstraints();
    return bayesianNetwork;
  }

  private void addAllNodes() {
    nodeInputs.forEach(
        nodeInput -> bayesianNetwork.addNewNode(nodeInput.getNodeId(), nodeInput.getStateIds()));
  }

  private void parentNodes() {
    nodeInputs.stream()
        .filter(ni -> ni.getParentNodeIds() != null)
        .forEach(ni -> bayesianNetwork.addParents(ni.getNodeId(), ni.getParentNodeIds()));
  }

  private void createCptConstraints() {
    nodeInputs.stream()
        .filter(ni -> ni.getCptValues() != null)
        .toList()
        .forEach(
            nodeInput -> {
              ConstraintBuilderIterator cbi = buildConstraintBuilderIterator(nodeInput);
              cbi.performRun();
              bayesianNetwork.addConstraints(cbi.getBuilt());
            });
  }

  private ConstraintBuilderIterator buildConstraintBuilderIterator(NetworkBuilderNode nodeInput) {
    List<? extends Serializable> cptStrideOrderIds = nodeInput.getCptNodeOrder();
    double[] cptValues = nodeInput.getCptValues();
    List<Node> nodes = convertToNodes(cptStrideOrderIds);
    ProbabilityVector vector = vectorFactory.build(nodes);
    validateArrayLengths(cptValues, vector, nodes);
    System.arraycopy(cptValues, 0, vector.getProbabilities(), 0, cptValues.length);
    Set<Node> conditions = new HashSet<>(nodes);
    Node event = bayesianNetwork.getNode(nodeInput.getNodeId());
    conditions.remove(event);
    return new ConstraintBuilderIterator(Set.of(event), conditions, vector);
  }

  private List<Node> convertToNodes(List<? extends Serializable> cptStrideOrderIds) {
    return cptStrideOrderIds.stream().map(bayesianNetwork::getNode).toList();
  }

  private static void validateArrayLengths(
      double[] cptValues, ProbabilityVector vector, List<Node> nodes) {
    int vectorLength = vector.getProbabilities().length;
    if (cptValues.length != vectorLength) {
      throw new ConstraintValidationException(
          "CPT input for %s requires array length %d, but was length %d."
              .formatted(NodeUtils.formatNodesToString(nodes), vectorLength, cptValues.length));
    }
  }
}
