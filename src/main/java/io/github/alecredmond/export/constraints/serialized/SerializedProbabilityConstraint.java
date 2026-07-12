package io.github.alecredmond.export.constraints.serialized;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import java.io.Serializable;

public interface SerializedProbabilityConstraint<T extends ProbabilityConstraint>
    extends Serializable {

  Class<T> getConstraintClass();

  String getConstraintType();
}
