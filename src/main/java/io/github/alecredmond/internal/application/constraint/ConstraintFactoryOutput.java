package io.github.alecredmond.internal.application.constraint;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.internal.method.constraints.strategy.ValidatedConstraint;

public record ConstraintFactoryOutput(
    ValidatedConstraint<?> validated, ConstraintValidationException optException) {}
