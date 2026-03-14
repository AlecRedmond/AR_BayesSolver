package io.github.alecredmond.internal.serialization.mapper;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class SerializationData {
  private Map<Serializable, Node> nodeIdMap = new HashMap<>();
  private Map<Serializable, NodeState> nodeStateIdMap = new HashMap<>();
}
