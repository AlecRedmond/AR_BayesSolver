package io.github.alecredmond.internal.serialization.structure;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.serialized.SerializedNode;
import io.github.alecredmond.internal.serialization.SerializationData;
import io.github.alecredmond.internal.serialization.SerializerUtils;

import java.util.ArrayList;

public class NodeSerializer {

  public Node createNewBase(SerializedNode sto) {
    return new Node(sto.id(), sto.stateIds());
  }

  public SerializedNode serialize(Node node) {
    return new SerializedNode(
        node.getId(),
        SerializerUtils.serializeNodeStates(node.getNodeStates()),
        SerializerUtils.serializeNodes(node.getParents()),
        SerializerUtils.serializeNodes(node.getChildren()));
  }

  public Node deSerialize(SerializedNode sto, SerializationData data) {
    Node node = data.getNodeIdMap().get(sto.id());
    node.setParents(SerializerUtils.deSerializeNodes(sto.parentIds(), ArrayList::new, data));
    node.setChildren(SerializerUtils.deSerializeNodes(sto.childIds(), ArrayList::new, data));
    return node;
  }
}
