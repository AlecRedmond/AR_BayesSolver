module BayesSolver {
  requires java.datatransfer;
  requires static lombok;
  requires org.slf4j;
  requires java.desktop;
  requires java.sql;
  requires java.rmi;

  exports io.github.alecredmond.export.application.constraints;
  exports io.github.alecredmond.export.application.network;
  exports io.github.alecredmond.export.application.node;
  exports io.github.alecredmond.export.application.probabilitytables;
  exports io.github.alecredmond.export.application.probabilitytables.probabilityvector;
  exports io.github.alecredmond.export.application.sampler;
  exports io.github.alecredmond.exceptions;
  exports io.github.alecredmond.export.method.network;
  exports io.github.alecredmond.export.method.sampler;
}
