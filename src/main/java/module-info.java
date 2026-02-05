module BayesSolver {
  requires java.datatransfer;
  requires static lombok;
  requires org.slf4j;
  requires java.desktop;
    requires java.sql;
    requires java.rmi;

    exports io.github.alecredmond.application.constraints;
  exports io.github.alecredmond.application.inference;
  exports io.github.alecredmond.application.network;
  exports io.github.alecredmond.application.node;
  exports io.github.alecredmond.application.printer;
  exports io.github.alecredmond.application.probabilitytables;
  exports io.github.alecredmond.exceptions;
  exports io.github.alecredmond.method.network;
  exports io.github.alecredmond.application.probabilitytables.probabilityvector;
  exports io.github.alecredmond.method.probabilitytables;
  exports io.github.alecredmond.method.probabilitytables.probabilityvector;
}
