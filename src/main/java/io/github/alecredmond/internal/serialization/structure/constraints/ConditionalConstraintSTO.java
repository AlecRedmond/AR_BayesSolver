package io.github.alecredmond.internal.serialization.structure.constraints;

import static io.github.alecredmond.internal.serialization.mapper.SerializerUtils.*;

import io.github.alecredmond.export.application.constraints.ConditionalConstraint;
import io.github.alecredmond.internal.serialization.mapper.SerializationData;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ConditionalConstraintSTO extends ProbabilityConstraintSTO<ConditionalConstraint> {
  private List<Serializable> conditionStateIds;

  @Override
  public ConditionalConstraintSTO serialize(ConditionalConstraint constraint) {
    this.eventStateId = constraint.getEventState().getId();
    this.conditionStateIds = serializeNodeStates(constraint.getConditionStates());
    this.probability = constraint.getProbability();
    return this;
  }

  @Override
  public ConditionalConstraint deSerialize(SerializationData data) {
    return new ConditionalConstraint(
        data.getNodeStateIdMap().get(eventStateId),
        deSerializeNodeStates(conditionStateIds, ArrayList::new, data),
        probability);
  }
}
