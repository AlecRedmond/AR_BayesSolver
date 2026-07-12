package io.github.alecredmond.internal.serialization.structure;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.export.probabilitytables.serialized.SerializedProbabilityVector;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.ProbabilityVectorFactory;
import io.github.alecredmond.internal.serialization.SerializationData;
import io.github.alecredmond.internal.serialization.SerializerUtils;
import java.io.Serializable;
import java.util.Map;

public class ProbabilityVectorSerializer {

  public SerializedProbabilityVector serialize(ProbabilityVector vector) {
    SerializedProbabilityVector spv = new SerializedProbabilityVector();
    spv.setNodeIdArray(SerializerUtils.serializeArray(vector.getNodeArray(), Node::getId));
    spv.setNumberOfStates(vector.getNumberOfStates());
    spv.setStrideLengths(vector.getStrideLengths());
    spv.setProbabilities(vector.getProbabilities());
    return spv;
  }

  public ProbabilityVector deSerialize(SerializedProbabilityVector spv, SerializationData data) {
    Map<Serializable, Node> nodeIdMap = data.getNodeIdMap();
    Node[] nodeArray =
        SerializerUtils.deserializeArray(spv.getNodeIdArray(), nodeIdMap::get, Node[]::new);
    NodeState[][] stateArray = ProbabilityVectorFactory.buildStateArrays(nodeArray);
    return new ProbabilityVector(
        nodeArray,
        stateArray,
        spv.getNumberOfStates(),
        spv.getStrideLengths(),
        spv.getProbabilities(),
        NodeUtils.buildNodeIndexMap(nodeArray),
        NodeUtils.buildStateIndexMap(nodeArray));
  }
}
