package io.github.alecredmond.internal.method.probabilitytables.probabilityvector.iteratorutils;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import java.util.function.Function;
import java.util.function.Predicate;

public interface OdometerResetLogic {
  default Function<Node, NodeState> initialStatePositionSetter() {
    return node -> node.getNodeStates().getFirst();
  }

  Predicate<Node> checkLockOuter();

  Predicate<Node> checkLockInner();

  default Function<Node, boolean[]> checkStateIsEvidence() {
    return node -> null;
  }

  default Runnable postUpdateLogic() {
    return () -> {};
  }
}
