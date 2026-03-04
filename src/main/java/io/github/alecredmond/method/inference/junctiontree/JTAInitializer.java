package io.github.alecredmond.method.inference.junctiontree;

import io.github.alecredmond.application.constraints.ConditionalConstraint;
import io.github.alecredmond.application.constraints.MarginalConstraint;
import io.github.alecredmond.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.application.inference.junctiontree.Clique;
import io.github.alecredmond.application.inference.junctiontree.JunctionTreeData;
import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.probabilitytables.export.MarginalTable;
import io.github.alecredmond.application.probabilitytables.export.ProbabilityTable;
import io.github.alecredmond.application.probabilitytables.internal.JunctionTreeTable;
import io.github.alecredmond.method.inference.junctiontree.handlers.JTAConstraintHandler;
import io.github.alecredmond.method.inference.junctiontree.handlers.JTAConstraintHandlerConditional;
import io.github.alecredmond.method.inference.junctiontree.handlers.JTAConstraintHandlerMarginal;
import io.github.alecredmond.method.inference.junctiontree.handlers.JTATableHandler;
import io.github.alecredmond.method.inference.junctiontree.handlers.readwrite.JTATransferWriterFactory;
import io.github.alecredmond.method.inference.junctiontree.separators.CliqueJoiner;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JTAInitializer {

  private JTAInitializer() {}

  public static JunctionTreeData buildSolverConfiguration(BayesianNetworkData bayesianNetworkData) {
    JunctionTreeData junctionTreeData = new JunctionTreeData();
    junctionTreeData.setInferenceConfiguration(false);
    buildCommon(junctionTreeData, bayesianNetworkData, true);
    buildConstraintHandlers(junctionTreeData, bayesianNetworkData);
    log.info("JUNCTION TREE DATA INITIALIZED IN SOLVER CONFIGURATION");
    return junctionTreeData;
  }

  private static void buildCommon(
      JunctionTreeData junctionTreeData,
      BayesianNetworkData bayesianNetworkData,
      boolean writeBackToCPTs) {
    junctionTreeData.setBayesianNetworkData(bayesianNetworkData);
    JTACliqueBuilder.buildCliques(junctionTreeData, bayesianNetworkData);
    buildInternalMessagePassers(junctionTreeData);
    buildExternalMessagePassers(junctionTreeData, bayesianNetworkData, writeBackToCPTs);
  }

  private static void buildConstraintHandlers(JunctionTreeData jtd, BayesianNetworkData bnd) {
    List<JTAConstraintHandler> list =
        bnd.getConstraints().stream()
            .map(constraint -> buildConstraintHandler(constraint, jtd))
            .toList();
    jtd.setConstraintHandlers(list);
  }

  private static void buildInternalMessagePassers(JunctionTreeData jtd) {
    CliqueJoiner.join(jtd);
  }

  private static void buildExternalMessagePassers(
      JunctionTreeData jtd, BayesianNetworkData bnd, boolean writeBackToCPTs) {
    Clique[] cliques = jtd.getCliques();

    JTATransferWriterFactory factory = new JTATransferWriterFactory();

    for (Node node : bnd.getNodes()) {
      ProbabilityTable networkTable = bnd.getNetworkTablesMap().get(node);
      MarginalTable observedTable = bnd.getObservationMap().get(node);

      Clique bestNetworkClique = getContainsScope(cliques, networkTable.getNodes());
      Clique bestObservationClique = getContainsScope(cliques, observedTable.getNodes());

      JunctionTreeTable cliqueTable = bestObservationClique.getTable();

      bestNetworkClique
          .getInitializeFrom()
          .add(factory.buildMultiplyInWriter(networkTable, bestNetworkClique.getTable()));

      bestObservationClique
          .getObservedWriters()
          .add(
              factory.buildMultiplyInWriter(
                  cliqueTable.getNodes(),
                  observedTable.getNodes(),
                  cliqueTable.getVector(),
                  observedTable.getVector()));

      if (writeBackToCPTs) {
        bestNetworkClique
            .getNetworkWriters()
            .add(factory.buildMultiplyInWriter(bestNetworkClique.getTable(), networkTable));
      }
    }
  }

  private static JTAConstraintHandler buildConstraintHandler(
      ProbabilityConstraint constraint, JunctionTreeData jtd) {
    JTATableHandler jtaTableHandler =
        getContainsScope(jtd.getCliques(), constraint.getAllNodes()).getHandler();

    if (Objects.requireNonNull(constraint) instanceof MarginalConstraint mc) {
      return new JTAConstraintHandlerMarginal(jtaTableHandler, mc);
    } else if (constraint instanceof ConditionalConstraint cc) {
      return new JTAConstraintHandlerConditional(jtaTableHandler, cc);
    }
    throw new IllegalStateException("Unexpected value: " + constraint);
  }

  private static Clique getContainsScope(Clique[] cliques, Set<Node> nodesInScope) {
    return Arrays.stream(cliques)
        .filter(c -> c.getNodes().containsAll(nodesInScope))
        .min(Comparator.comparing(clique -> clique.getNodes().size()))
        .orElseThrow();
  }

  public static JunctionTreeData buildInferenceConfiguration(
      BayesianNetworkData bayesianNetworkData) {
    JunctionTreeData junctionTreeData = new JunctionTreeData();
    junctionTreeData.setInferenceConfiguration(true);
    buildCommon(junctionTreeData, bayesianNetworkData, false);
    junctionTreeData.setConstraintHandlers(new ArrayList<>());
    log.info("JUNCTION TREE DATA INITIALIZED IN INFERENCE CONFIGURATION");
    return junctionTreeData;
  }
}
