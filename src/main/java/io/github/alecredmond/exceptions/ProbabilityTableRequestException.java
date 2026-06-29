package io.github.alecredmond.exceptions;

import io.github.alecredmond.export.method.probabilitytables.TableQueryTool;

/** Thrown if an illegal request was made to a {@link TableQueryTool}. */
public class ProbabilityTableRequestException extends IllegalArgumentException {
    public ProbabilityTableRequestException(String s) {
        super(s);
    }
}
