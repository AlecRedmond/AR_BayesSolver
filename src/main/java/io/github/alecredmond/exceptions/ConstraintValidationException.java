package io.github.alecredmond.exceptions;

public class ConstraintValidationException extends IllegalArgumentException {
    public ConstraintValidationException(String s) {
        super(s);
    }
}
