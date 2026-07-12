package io.github.alecredmond.export.node.serialized;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SerializedNode implements Serializable {
  private Serializable id;
  private List<Serializable> stateIds;
  private List<Serializable> parentIds;
  private List<Serializable> childIds;
}
