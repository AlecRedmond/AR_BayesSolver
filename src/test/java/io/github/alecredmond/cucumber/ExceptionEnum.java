package io.github.alecredmond.cucumber;

import io.github.alecredmond.exceptions.BayesNetIDException;
import io.github.alecredmond.exceptions.NetworkStructureException;

public enum ExceptionEnum {
  BAYES_NET_ID_EXCEPTION(BayesNetIDException.class),
  NETWORK_STRUCTURE_EXCEPTION(NetworkStructureException.class);

  ExceptionEnum(Class<? extends Exception> bayesNetIDExceptionClass) {}
}
