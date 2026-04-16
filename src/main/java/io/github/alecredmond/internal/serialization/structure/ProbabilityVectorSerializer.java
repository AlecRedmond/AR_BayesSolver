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
    SerializedProbabilityVector sv = new SerializedProbabilityVector();
    sv.setNodeIdArray(SerializerUtils.serializeArray(vector.getNodeArray(), Node::getId));
    sv.setNumberOfStates(vector.getNumberOfStates());
    sv.setStepMultiplier(vector.getStepMultiplier());
    sv.setProbabilities(vector.getProbabilities());
    return sv;
  }

  public ProbabilityVector deSerialize(SerializedProbabilityVector sv, SerializationData data) {
    Map<Serializable, Node> nodeIdMap = data.getNodeIdMap();
    Node[] nodeArray =
        SerializerUtils.deserializeArray(sv.getNodeIdArray(), nodeIdMap::get, Node[]::new);
    return new ProbabilityVector(
        nodeArray,
        sv.getNumberOfStates(),
        sv.getStepMultiplier(),
        sv.getProbabilities(),
        NodeUtils.buildNodeIndexMap(nodeArray),
        NodeUtils.buildStateIndexMap(nodeArray));
  }
}
