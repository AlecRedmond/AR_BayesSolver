module BayesSolver {
  requires java.datatransfer;
  requires static lombok;
  requires org.slf4j;
  requires java.desktop;
  requires java.sql;
  requires java.rmi;
  requires org.apache.commons.lang3;

  exports io.github.alecredmond.export.application.constraints;
  exports io.github.alecredmond.export.application.inference;
  exports io.github.alecredmond.export.application.network;
  exports io.github.alecredmond.export.application.node;
  exports io.github.alecredmond.export.application.probabilitytables;
  exports io.github.alecredmond.export.method.inference;
  exports io.github.alecredmond.export.method.network;
  exports io.github.alecredmond.export.method.probabilitytables;
  exports io.github.alecredmond.export.method.sampler;
  exports io.github.alecredmond.export.serialization.constraint;
  exports io.github.alecredmond.export.serialization.network;
  exports io.github.alecredmond.export.serialization.node;
  exports io.github.alecredmond.export.serialization.probabilitytable;
  exports io.github.alecredmond.export.serialization.probabilitytable.probabilityvector;
  exports io.github.alecredmond.exceptions;
  exports io.github.alecredmond.export.application.probabilitytables.cptentry;
}
