package io.github.alecredmond.internal.serialization.structure;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.export.serialization.probabilitytable.probabilityvector.SerializedProbabilityVector;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.serialization.SerializationData;
import io.github.alecredmond.internal.serialization.SerializerUtils;
import java.io.Serializable;
import java.util.Map;

public class ProbabilityVectorSerializer {

  public SerializedProbabilityVector serialize(ProbabilityVector vector) {
    SerializedProbabilityVector spv = new SerializedProbabilityVector();
    spv.setNodeIdArray(SerializerUtils.serializeArray(vector.getNodeArray(), Node::getId));
    spv.setNumberOfStates(vector.getNumberOfStates());
    spv.setStepMultiplier(vector.getStepMultiplier());
    spv.setProbabilities(vector.getProbabilities());
    return spv;
  }

  public ProbabilityVector deSerialize(SerializedProbabilityVector spv, SerializationData data) {
    Map<Serializable, Node> nodeIdMap = data.getNodeIdMap();
    Node[] nodeArray =
        SerializerUtils.deserializeArray(spv.getNodeIdArray(), nodeIdMap::get, Node[]::new);
    return new ProbabilityVector(
        nodeArray,
        spv.getNumberOfStates(),
        spv.getStepMultiplier(),
        spv.getProbabilities(),
        NodeUtils.buildNodeIndexMap(nodeArray),
        NodeUtils.buildStateIndexMap(nodeArray));
  }
}
