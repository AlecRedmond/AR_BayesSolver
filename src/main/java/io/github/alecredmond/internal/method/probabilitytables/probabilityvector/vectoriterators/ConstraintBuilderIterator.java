package io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators;

import io.github.alecredmond.export.application.constraints.ConditionalConstraint;
import io.github.alecredmond.export.application.constraints.MarginalConstraint;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.VectorOdometer;
import java.util.*;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class ConstraintBuilderIterator extends BaseVectorIterator implements VectorIterator {
  private List<ProbabilityConstraint> built;

  public ConstraintBuilderIterator(VectorOdometer vectorOdometer) {
    super(vectorOdometer);
    performRun();
  }

  @Override
  public void performRun() {
    built = new ArrayList<>();
    double[] p = vectorOdometer.getProbabilities();
    NodeState[] states = vectorOdometer.getStates();
    int condsLength = states.length - 1;
    Runnable function =
        condsLength == 0 ? createConditionals(condsLength, p, states) : createMarginals(p, states);
    iterateOuter(function);
  }

  private Runnable createConditionals(int condsLength, double[] p, NodeState[] states) {
    return () -> {
      Set<NodeState> conds = getConds(states, condsLength);
      iterateInner(
          (o, i) -> {
            double prob = p[i];
            if (prob == 1.0) return; // As tables are initialized to 1.0
            NodeState event = states[condsLength];
            built.add(new ConditionalConstraint(event, conds, prob));
          });
    };
  }

  private Runnable createMarginals(double[] p, NodeState[] states) {
    return () ->
        iterateInner(
            (o, i) -> {
              double prob = p[i];
              if (prob == 1.0) return;
              NodeState event = states[0];
              built.add(new MarginalConstraint(event, prob));
            });
  }

  private Set<NodeState> getConds(NodeState[] states, int condsLength) {
    return Arrays.stream(states, 0, condsLength).collect(Collectors.toSet());
  }
}
