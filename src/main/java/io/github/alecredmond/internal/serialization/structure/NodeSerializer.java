package io.github.alecredmond.internal.serialization.structure;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.serialization.node.SerializedNode;
import io.github.alecredmond.internal.serialization.SerializationData;
import io.github.alecredmond.internal.serialization.SerializerUtils;

import java.util.ArrayList;

public class NodeSerializer {

  public Node createNewBase(SerializedNode sto) {
    return new Node(sto.getId(), sto.getStateIds());
  }

  public SerializedNode serialize(Node node) {
    return new SerializedNode(
        node.getId(),
        SerializerUtils.serializeNodeStates(node.getNodeStates()),
        SerializerUtils.serializeNodes(node.getParents()),
        SerializerUtils.serializeNodes(node.getChildren()));
  }

  public Node deSerialize(SerializedNode sto, SerializationData data) {
    Node node = data.getNodeIdMap().get(sto.getId());
    node.setParents(SerializerUtils.deSerializeNodes(sto.getParentIds(), ArrayList::new, data));
    node.setChildren(SerializerUtils.deSerializeNodes(sto.getChildIds(), ArrayList::new, data));
    return node;
  }
}
