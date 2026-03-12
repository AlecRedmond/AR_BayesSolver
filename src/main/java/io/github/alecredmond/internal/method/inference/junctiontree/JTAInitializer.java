package io.github.alecredmond.internal.method.inference.junctiontree;

import io.github.alecredmond.export.application.constraints.ConditionalConstraint;
import io.github.alecredmond.export.application.constraints.MarginalConstraint;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.internal.application.inference.junctiontree.Clique;
import io.github.alecredmond.internal.application.inference.junctiontree.JunctionTreeData;
import io.github.alecredmond.internal.application.inference.junctiontree.Separator;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.probabilitytables.MarginalTable;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.application.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.method.probabilitytables.transfer.TransferIteratorBuilder;
import io.github.alecredmond.internal.method.inference.junctiontree.handlers.JTAConstraintHandler;
import io.github.alecredmond.internal.method.inference.junctiontree.handlers.JTAConstraintHandlerConditional;
import io.github.alecredmond.internal.method.inference.junctiontree.handlers.JTAConstraintHandlerMarginal;
import io.github.alecredmond.internal.method.inference.junctiontree.handlers.JTATableHandler;
import io.github.alecredmond.internal.method.inference.junctiontree.separators.CliqueJoiner;

import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JTAInitializer {

  private JTAInitializer() {}

  public static JunctionTreeData buildSolverConfiguration(BayesianNetworkData bayesianNetworkData) {
    JunctionTreeData junctionTreeData = new JunctionTreeData();
    junctionTreeData.setSolverConfig(true);
    buildCommon(junctionTreeData, bayesianNetworkData);
    buildConstraintHandlers(junctionTreeData, bayesianNetworkData);
    log.info("JUNCTION TREE DATA INITIALIZED IN SOLVER CONFIGURATION");
    return junctionTreeData;
  }

  private static void buildCommon(
      JunctionTreeData junctionTreeData, BayesianNetworkData bayesianNetworkData) {
    junctionTreeData.setBayesianNetworkData(bayesianNetworkData);
    JTACliqueBuilder.buildCliques(junctionTreeData);
    buildInternalMessagePassers(junctionTreeData);
    buildExternalMessagePassers(junctionTreeData, bayesianNetworkData);
  }

  private static void buildConstraintHandlers(JunctionTreeData jtd, BayesianNetworkData bnd) {
    List<ProbabilityConstraint> constraints = bnd.getConstraints();
    Map<Clique, List<JTAConstraintHandler>> map =
        Arrays.stream(jtd.getCliques())
            .map(clique -> Map.entry(clique, matchConstraints(clique, constraints)))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    jtd.setConstraintHandlersMap(map);
  }

  private static void buildInternalMessagePassers(JunctionTreeData jtd) {
    CliqueJoiner.join(jtd);

    Separator[] separators =
        Arrays.stream(jtd.getCliques())
            .flatMap(clique -> clique.getSeparatorMap().values().stream())
            .distinct()
            .toArray(Separator[]::new);

    jtd.setSeparators(separators);
  }

  private static void buildExternalMessagePassers(JunctionTreeData jtd, BayesianNetworkData bnd) {
    boolean writeBackToCPTs = jtd.isSolverConfig();
    Clique[] cliques = jtd.getCliques();

    TransferIteratorBuilder builder = new TransferIteratorBuilder();

    for (Node node : bnd.getNodes()) {
      ProbabilityTable networkTable = bnd.getNetworkTablesMap().get(node);
      MarginalTable observedTable = bnd.getObservationMap().get(node);

      Clique bestClique = getContainsScope(cliques, networkTable.getNodes());
      JunctionTreeTable cliqueTable = bestClique.getTable();

      bestClique
          .getWriteFromCPTs()
          .add(builder.buildMultiplyTransferIterator(networkTable, cliqueTable));

      bestClique
          .getWriteToObserved()
          .add(builder.buildMarginalTransferIterator(cliqueTable, observedTable));

      if (writeBackToCPTs) {
        bestClique
            .getWriteToCPTs()
            .add(builder.buildMarginalTransferIterator(cliqueTable, networkTable));
      }
    }
  }

  private static List<JTAConstraintHandler> matchConstraints(
      Clique clique, List<ProbabilityConstraint> constraints) {
    return constraints.stream()
        .filter(constraint -> clique.getNodes().containsAll(constraint.getAllNodes()))
        .map(constraint -> buildConstraintHandler(constraint, clique))
        .toList();
  }

  private static Clique getContainsScope(Clique[] cliques, Set<Node> nodesInScope) {
    return Arrays.stream(cliques)
        .filter(c -> c.getNodes().containsAll(nodesInScope))
        .min(Comparator.comparing(clique -> clique.getNodes().size()))
        .orElseThrow();
  }

  private static JTAConstraintHandler buildConstraintHandler(
      ProbabilityConstraint constraint, Clique clique) {
    JTATableHandler jtaTableHandler = clique.getHandler();

    if (Objects.requireNonNull(constraint) instanceof MarginalConstraint mc) {
      return new JTAConstraintHandlerMarginal(jtaTableHandler, mc);
    } else if (constraint instanceof ConditionalConstraint cc) {
      return new JTAConstraintHandlerConditional(jtaTableHandler, cc);
    }
    throw new IllegalStateException("Unexpected value: " + constraint);
  }

  public static JunctionTreeData buildInferenceConfiguration(
      BayesianNetworkData bayesianNetworkData) {
    JunctionTreeData junctionTreeData = new JunctionTreeData();
    junctionTreeData.setSolverConfig(false);
    buildCommon(junctionTreeData, bayesianNetworkData);
    junctionTreeData.setConstraintHandlersMap(new HashMap<>());
    log.info("JUNCTION TREE DATA INITIALIZED IN INFERENCE CONFIGURATION");
    return junctionTreeData;
  }
}
