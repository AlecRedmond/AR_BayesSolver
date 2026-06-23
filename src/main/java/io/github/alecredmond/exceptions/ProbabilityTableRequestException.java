package io.github.alecredmond.exceptions;

import io.github.alecredmond.export.method.probabilitytables.TableHelper;

/** Thrown if an illegal request was made to a {@link TableHelper}. */
public class ProbabilityTableRequestException extends IllegalArgumentException {
    public ProbabilityTableRequestException(String s) {
        super(s);
    }
}
