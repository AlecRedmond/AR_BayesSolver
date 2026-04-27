package io.github.alecredmond.internal.application.constraint;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintValidator;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.util.Set;
import lombok.Data;

@Data
public class ConstraintBuilderData {
  private final Set<NodeState> eventStates;
  private final Set<Node> eventNodes;
  private final Set<NodeState> conditionStates;
  private final Set<Node> conditionNodes;
  private final double probability;
  private final BayesianNetworkData networkData;
  private ProbabilityConstraint constraint = null;
  private Class<? extends ProbabilityConstraint> constraintClass = null;
  private ConstraintValidator<? extends ProbabilityConstraint> validator = null;
  private ConstraintValidationException exception = null;

  public ConstraintBuilderData(
      BayesianNetworkData networkData,
      Set<NodeState> eventStates,
      Set<NodeState> conditionStates,
      double probability) {
    this.networkData = networkData;
    this.eventStates = eventStates;
    this.conditionStates = conditionStates;
    this.probability = probability;
    this.eventNodes = NodeUtils.getNodes(eventStates);
    this.conditionNodes = NodeUtils.getNodes(conditionStates);
  }

  public ConstraintBuilderData(
      BayesianNetworkData networkData,
      ProbabilityConstraint constraint,
      ConstraintValidator<? extends ProbabilityConstraint> validator) {
    this.constraint = constraint;
    this.eventStates = constraint.getEventStates();
    this.conditionStates = constraint.getConditionStates();
    this.probability = constraint.getProbability();
    this.networkData = networkData;
    this.validator = validator;
    this.eventNodes = NodeUtils.getNodes(eventStates);
    this.conditionNodes = NodeUtils.getNodes(conditionStates);
  }

  public ConstraintBuilderData(
      BayesianNetworkData networkData,
      ProbabilityConstraint constraint,
      ConstraintValidationException exception) {
    this.eventStates = constraint.getEventStates();
    this.conditionStates = constraint.getConditionStates();
    this.probability = constraint.getProbability();
    this.networkData = networkData;
    this.exception = exception;
    this.eventNodes = NodeUtils.getNodes(eventStates);
    this.conditionNodes = NodeUtils.getNodes(conditionStates);
  }
}
