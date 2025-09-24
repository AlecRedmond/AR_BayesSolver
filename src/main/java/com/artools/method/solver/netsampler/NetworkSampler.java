package com.artools.method.solver.netsampler;

import com.artools.application.constraints.ParameterConstraint;
import com.artools.application.node.Node;
import com.artools.application.node.NodeState;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public interface NetworkSampler {

  double adjustAndReturnError(ParameterConstraint constraint);

  void sampleNetwork(Map<Node, NodeState> evidence);

  void sampleNetwork();

  default Map<Node, NodeState> convertToEvidence(Collection<NodeState> evidenceStates) {
    Map<Node, NodeState> evidence = new HashMap<>();
    evidenceStates.forEach(
        state -> {
          checkNoDuplicates(evidence, state);
          evidence.put(state.getParentNode(), state);
        });
    return evidence;
  }

  private static void checkNoDuplicates(Map<Node, NodeState> evidence, NodeState state) {
    if (evidence.containsKey(state.getParentNode())) {
      throw new IllegalArgumentException("Tried to observe multiple NodeStates on the same node!");
    }
  }

}
