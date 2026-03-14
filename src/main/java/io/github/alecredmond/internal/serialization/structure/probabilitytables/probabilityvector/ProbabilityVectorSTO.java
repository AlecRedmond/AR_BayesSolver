package io.github.alecredmond.internal.serialization.structure.probabilitytables.probabilityvector;

import static io.github.alecredmond.internal.serialization.mapper.SerializerUtils.*;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.ProbabilityVectorFactory;
import io.github.alecredmond.internal.serialization.mapper.SerializationData;
import io.github.alecredmond.internal.serialization.mapper.SerializationTransferObject;
import java.io.Serializable;
import java.util.Map;

public class ProbabilityVectorSTO implements SerializationTransferObject<ProbabilityVector> {
  private Serializable[] nodeIdArray;
  private int[] numberOfStates;
  private int[] stepMultiplier;
  private double[] probabilities;

  @Override
  public ProbabilityVectorSTO serialize(ProbabilityVector vector) {
    this.nodeIdArray = serializeArray(vector.getNodeArray(), Node::getId);
    this.numberOfStates = vector.getNumberOfStates();
    this.stepMultiplier = vector.getStepMultiplier();
    this.probabilities = vector.getProbabilities();
    return this;
  }

  @Override
  public ProbabilityVector deSerialize(SerializationData data) {
    Map<Serializable, Node> nodeIdMap = data.getNodeIdMap();
    Node[] nodeArray = deserializeArray(nodeIdArray, nodeIdMap::get, Node[]::new);
    ProbabilityVectorFactory factory = new ProbabilityVectorFactory();
    return new ProbabilityVector(
        nodeArray,
        numberOfStates,
        stepMultiplier,
        probabilities,
        factory.buildNodeIndexMap(nodeArray),
        factory.buildStateValueMap(nodeArray));
  }
}
