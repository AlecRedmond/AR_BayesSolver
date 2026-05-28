package io.github.alecredmond.export.serialization.constraint;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import java.io.Serializable;

public interface SerializedProbabilityConstraint<T extends ProbabilityConstraint>
    extends Serializable {

  Class<T> getConstraintClass();
}
