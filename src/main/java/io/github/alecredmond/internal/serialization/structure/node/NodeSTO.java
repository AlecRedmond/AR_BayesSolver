package io.github.alecredmond.internal.serialization.structure.node;

import static io.github.alecredmond.internal.serialization.mapper.SerializerUtils.*;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.internal.serialization.mapper.SerializationData;
import io.github.alecredmond.internal.serialization.mapper.SerializationTransferObject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NodeSTO implements SerializationTransferObject<Node> {
  private Serializable id;
  private List<Serializable> stateIds;
  private List<Serializable> parentIds;
  private List<Serializable> childIds;

  public Node createNewBase() {
    return new Node(id, stateIds);
  }

  @Override
  public NodeSTO serialize(Node node) {
    this.id = node.getId();
    this.stateIds = serializeCollection(node.getNodeStates(), NodeState::getId, ArrayList::new);
    this.parentIds = serializeCollection(node.getParents(), Node::getId, ArrayList::new);
    this.childIds = serializeCollection(node.getChildren(), Node::getId, ArrayList::new);
    return this;
  }

  @Override
  public Node deSerialize(SerializationData data) {
    Map<Serializable, Node> nodeIdMap = data.getNodeIdMap();
    Node node = nodeIdMap.get(id);
    node.setParents(deSerializeCollection(this.parentIds, nodeIdMap::get, ArrayList::new));
    node.setChildren(deSerializeCollection(this.childIds, nodeIdMap::get, ArrayList::new));
    return node;
  }
}
