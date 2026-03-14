package io.github.alecredmond.internal.serialization.structure.constraints;

import io.github.alecredmond.export.application.constraints.MarginalConstraint;
import io.github.alecredmond.internal.serialization.mapper.SerializationData;

public class MarginalConstraintSTO extends ProbabilityConstraintSTO<MarginalConstraint> {

  @Override
  public MarginalConstraintSTO serialize(MarginalConstraint marginalConstraint) {
    eventStateId = marginalConstraint.getEventState().getId();
    probability = marginalConstraint.getProbability();
    return this;
  }

  @Override
  public MarginalConstraint deSerialize(SerializationData data) {
    return new MarginalConstraint(data.getNodeStateIdMap().get(eventStateId), probability);
  }
}
