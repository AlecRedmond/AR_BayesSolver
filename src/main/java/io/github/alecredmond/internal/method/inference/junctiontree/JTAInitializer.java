package io.github.alecredmond.internal.method.inference.junctiontree;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.probabilitytables.MarginalTable;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.application.inference.junctiontree.Clique;
import io.github.alecredmond.internal.application.inference.junctiontree.JunctionTreeData;
import io.github.alecredmond.internal.application.inference.junctiontree.Separator;
import io.github.alecredmond.internal.application.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintSolverHandler;
import io.github.alecredmond.internal.method.constraints.ConstraintRegistry;
import io.github.alecredmond.internal.method.inference.junctiontree.handlers.*;
import io.github.alecredmond.internal.method.inference.junctiontree.separators.CliqueJoiner;
import io.github.alecredmond.internal.method.probabilitytables.TableBuilder;
import io.github.alecredmond.internal.method.probabilitytables.transfer.TransferIteratorBuilder;
import java.util.*;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JTAInitializer {

  private JTAInitializer() {}

  public static JunctionTreeData buildNewSolverConfiguration(
      BayesianNetworkData bayesianNetworkData) {
    JunctionTreeData junctionTreeData = new JunctionTreeData();
    junctionTreeData.setSolverConfig(true);
    buildCommon(junctionTreeData, bayesianNetworkData);
    buildConstraintHandlers(junctionTreeData, bayesianNetworkData);
    log.info("JUNCTION TREE DATA INITIALIZED IN SOLVER CONFIGURATION");
    return junctionTreeData;
  }

  private static void buildCommon(
      JunctionTreeData junctionTreeData, BayesianNetworkData bayesianNetworkData) {
    junctionTreeData.setNetworkData(bayesianNetworkData);
    JTACliqueBuilder.buildCliques(junctionTreeData);
    buildInternalMessagePassers(junctionTreeData);
    buildExternalMessagePassers(junctionTreeData, bayesianNetworkData);
  }

  private static void buildConstraintHandlers(JunctionTreeData jtd, BayesianNetworkData bnd) {
    List<ProbabilityConstraint> constraints = bnd.getConstraints();
    Map<Clique, List<ConstraintSolverHandler<ProbabilityConstraint>>> map =
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
    boolean writeBackToNetwork = jtd.isSolverConfig();
    Clique[] cliques = jtd.getCliques();

    TransferIteratorBuilder builder = new TransferIteratorBuilder();

    Map<Node, ProbabilityTable> networkTables = bnd.getNetworkTablesMap();
    Map<Node, MarginalTable> marginalTables = jtd.getObservedTablesMap();

    for (Node node : bnd.getNodes()) {
      ProbabilityTable networkTable = networkTables.get(node);

      Clique bestClique = getContainsScope(cliques, networkTable.getNodes());
      JunctionTreeTable cliqueTable = bestClique.getTable();

      bestClique
          .getWriteFromCPTs()
          .add(builder.buildMultiplyTransferIterator(networkTable, cliqueTable));

      if (writeBackToNetwork) {
        bestClique
            .getWriteToCPTs()
            .add(builder.buildMarginalTransferIterator(cliqueTable, networkTable));
        continue;
      }

      bestClique
          .getWriteToObserved()
          .add(builder.buildMarginalTransferIterator(cliqueTable, marginalTables.get(node)));
    }
  }

  private static List<ConstraintSolverHandler<ProbabilityConstraint>> matchConstraints(
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

  @SuppressWarnings("unchecked")
  private static <T extends ProbabilityConstraint> ConstraintSolverHandler<T> buildConstraintHandler(
      @NonNull T constraint, Clique clique) {
    JTATableHandler jtaTableHandler = clique.getHandler();
    return (ConstraintSolverHandler<T>)
        ConstraintRegistry.getStrategy(constraint.getClass())
            .buildSolverHandler(jtaTableHandler, constraint);
  }

  public static JunctionTreeData buildNewInferenceConfiguration(
      BayesianNetworkData bayesianNetworkData) {
    JunctionTreeData junctionTreeData = new JunctionTreeData();
    buildInferenceConfiguration(junctionTreeData, bayesianNetworkData);
    return junctionTreeData;
  }

  public static void buildInferenceConfiguration(JunctionTreeData jtd, BayesianNetworkData bnd) {
    jtd.setSolverConfig(false);
    buildObserved(jtd, bnd);
    buildCommon(jtd, bnd);
    log.info("JUNCTION TREE DATA INITIALIZED IN INFERENCE CONFIGURATION");
  }

  private static void buildObserved(JunctionTreeData jtd, BayesianNetworkData bnd) {
    jtd.setObservedEvidence(new HashMap<>());
    jtd.setObservedTablesMap(
        bnd.getNodes().stream()
            .map(node -> Map.entry(node, TableBuilder.buildMarginalTable(Set.of(node))))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
  }
}
