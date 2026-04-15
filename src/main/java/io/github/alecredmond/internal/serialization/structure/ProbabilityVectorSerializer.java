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
    SerializedProbabilityVector sto = new SerializedProbabilityVector();
    sto.setNodeIdArray(SerializerUtils.serializeArray(vector.getNodeArray(), Node::getId));
    sto.setNumberOfStates(vector.getNumberOfStates());
    sto.setStepMultiplier(vector.getStepMultiplier());
    sto.setProbabilities(vector.getProbabilities());
    return sto;
  }

  public ProbabilityVector deSerialize(SerializedProbabilityVector sto, SerializationData data) {
    Map<Serializable, Node> nodeIdMap = data.getNodeIdMap();
    Node[] nodeArray = SerializerUtils.deserializeArray(sto.getNodeIdArray(), nodeIdMap::get, Node[]::new);
    return new ProbabilityVector(
        nodeArray,
        sto.getNumberOfStates(),
        sto.getStepMultiplier(),
        sto.getProbabilities(),
        NodeUtils.buildNodeIndexMap(nodeArray),
        NodeUtils.buildStateIndexMap(nodeArray));
  }
}
