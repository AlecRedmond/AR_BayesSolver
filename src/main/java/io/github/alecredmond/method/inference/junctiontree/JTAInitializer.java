package io.github.alecredmond.method.inference.junctiontree;

import io.github.alecredmond.application.constraints.ConditionalConstraint;
import io.github.alecredmond.application.constraints.MarginalConstraint;
import io.github.alecredmond.application.constraints.Constraint;
import io.github.alecredmond.application.inference.junctiontree.Clique;
import io.github.alecredmond.application.inference.junctiontree.JunctionTreeData;
import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.application.probabilitytables.MarginalTable;
import io.github.alecredmond.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.method.inference.junctiontree.handlers.JTAConstraintHandler;
import io.github.alecredmond.method.inference.junctiontree.handlers.JTAConstraintHandlerConditional;
import io.github.alecredmond.method.inference.junctiontree.handlers.JTAConstraintHandlerMarginal;
import io.github.alecredmond.method.inference.junctiontree.handlers.JTATableHandler;
import io.github.alecredmond.method.inference.junctiontree.handlers.readwrite.JTATransferWriterFactory;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JTAInitializer {

  private JTAInitializer() {}

  public static JunctionTreeData buildSolverConfiguration(BayesianNetworkData bayesianNetworkData) {
    JunctionTreeData junctionTreeData = new JunctionTreeData();
    buildCommon(junctionTreeData, bayesianNetworkData, true);
    buildConstraintHandlers(junctionTreeData, bayesianNetworkData);
    log.info("JUNCTION TREE DATA INITIALIZED IN SOLVER CONFIGURATION");
    return junctionTreeData;
  }

  public static JunctionTreeData buildInferenceConfiguration(
      BayesianNetworkData bayesianNetworkData) {
    JunctionTreeData junctionTreeData = new JunctionTreeData();
    buildCommon(junctionTreeData, bayesianNetworkData, false);
    junctionTreeData.setConstraintHandlers(new ArrayList<>());
    log.info("JUNCTION TREE DATA INITIALIZED IN INFERENCE CONFIGURATION");
    return junctionTreeData;
  }

  private static void buildCommon(
      JunctionTreeData junctionTreeData,
      BayesianNetworkData bayesianNetworkData,
      boolean writeBackToCPTs) {
    junctionTreeData.setBayesianNetworkData(bayesianNetworkData);
    JTACliqueBuilder.buildCliques(junctionTreeData, bayesianNetworkData);
    setJunctionTreeTablesList(junctionTreeData);
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

  private static void setJunctionTreeTablesList(JunctionTreeData jtd) {
    jtd.setJunctionTreeTables(
        Arrays.stream(jtd.getCliques())
            .map(Clique::getTable)
            .sorted(Comparator.comparing(table -> table.getNodes().size()))
            .toList());
  }

  private static void buildInternalMessagePassers(JunctionTreeData jtd) {
    Set<Clique> cliques =
        Arrays.stream(jtd.getCliques()).collect(Collectors.toCollection(HashSet::new));
    Clique smallest =
        cliques.stream().min(Comparator.comparing(c -> c.getNodes().size())).orElseThrow();
    cliques.remove(smallest);
    Set<Clique> joined = new HashSet<>(Set.of(smallest));
    recursivelyJoinCliques(smallest, cliques, joined, new JTATransferWriterFactory());
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

  private static Clique getContainsScope(Clique[] cliques, Set<Node> nodesInScope) {
    return Arrays.stream(cliques)
        .filter(c -> c.getNodes().containsAll(nodesInScope))
        .min(Comparator.comparing(clique -> clique.getNodes().size()))
        .orElseThrow();
  }

  private static JTAConstraintHandler buildConstraintHandler(
          Constraint constraint, JunctionTreeData jtd) {
    JTATableHandler jtaTableHandler =
        getContainsScope(jtd.getCliques(), constraint.getAllNodes()).getHandler();

    if (Objects.requireNonNull(constraint) instanceof MarginalConstraint mc) {
      return new JTAConstraintHandlerMarginal(jtaTableHandler, mc);
    } else if (constraint instanceof ConditionalConstraint cc) {
      return new JTAConstraintHandlerConditional(jtaTableHandler, cc);
    }
    throw new IllegalStateException("Unexpected value: " + constraint);
  }

  private static void recursivelyJoinCliques(
      Clique current, Set<Clique> available, Set<Clique> joined, JTATransferWriterFactory factory) {

    List<Clique> orderedCandidates =
        available.stream()
            .map(clique -> commonNodes(clique, current))
            .filter(connectionSize -> connectionSize.getValue() > 0)
            .sorted(Map.Entry.<Clique, Integer>comparingByValue().reversed())
            .map(Map.Entry::getKey)
            .toList();

    if (orderedCandidates.isEmpty()) {
      available.remove(current);
      return;
    }

    orderedCandidates.forEach(
        nextClique -> {
          if (joined.contains(nextClique)) {
            return;
          }
          current
              .getSeparatorMap()
              .put(nextClique, factory.buildMessagePassWriter(current, nextClique));
          nextClique
              .getSeparatorMap()
              .put(current, factory.buildMessagePassWriter(nextClique, current));
          available.remove(nextClique);
          joined.add(nextClique);
          recursivelyJoinCliques(nextClique, available, joined, factory);
        });
  }

  private static Map.Entry<Clique, Integer> commonNodes(Clique clique, Clique current) {
    Set<Node> cliqueNodes = new HashSet<>(clique.getNodes());
    cliqueNodes.retainAll(current.getNodes());
    return Map.entry(clique, cliqueNodes.size());
  }
}
