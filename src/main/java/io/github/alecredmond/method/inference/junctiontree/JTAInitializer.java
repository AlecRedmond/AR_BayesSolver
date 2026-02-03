package io.github.alecredmond.method.inference.junctiontree;

import io.github.alecredmond.application.constraints.ConditionalConstraint;
import io.github.alecredmond.application.constraints.MarginalConstraint;
import io.github.alecredmond.application.constraints.ParameterConstraint;
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
import io.github.alecredmond.method.utils.MapCollector;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JTAInitializer {
  private static final MapCollector COLLECTOR = new MapCollector();

  private JTAInitializer() {}

  public static JunctionTreeData buildSolverConfiguration(BayesianNetworkData bayesianNetworkData) {
    JunctionTreeData junctionTreeData = new JunctionTreeData();
    buildCommon(junctionTreeData, bayesianNetworkData, true);
    buildConstraintCliqueMap(junctionTreeData, bayesianNetworkData);
    buildConstraintHandlers(junctionTreeData, bayesianNetworkData);
    log.info("JUNCTION TREE DATA INITIALIZED IN SOLVER CONFIGURATION");
    return junctionTreeData;
  }

  public static JunctionTreeData buildInferenceConfiguration(
      BayesianNetworkData bayesianNetworkData) {
    JunctionTreeData junctionTreeData = new JunctionTreeData();
    buildCommon(junctionTreeData, bayesianNetworkData, false);
    junctionTreeData.setConstraintCliqueMap(new HashMap<>());
    junctionTreeData.setConstraintHandlers(new HashMap<>());
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
    buildLeafCliques(junctionTreeData);
  }

  private static void buildLeafCliques(JunctionTreeData junctionTreeData) {
    Set<Clique> leafCliques =
        junctionTreeData.getCliqueSet().stream()
            .filter(c -> c.getSeparatorMap().size() <= 1)
            .collect(Collectors.toSet());
    junctionTreeData.setLeafCliques(leafCliques);
  }

  private static void buildConstraintCliqueMap(
      JunctionTreeData junctionTreeData, BayesianNetworkData networkData) {
    junctionTreeData.setConstraintCliqueMap(
        COLLECTOR.convertToMap(
            constraint -> findSmallestClique(constraint, junctionTreeData.getCliqueSet()),
            networkData.getConstraints()));
  }

  private static void buildConstraintHandlers(JunctionTreeData jtd, BayesianNetworkData bnd) {
    jtd.setConstraintHandlers(
        COLLECTOR.convertToMap(
            constraint -> getConstraintHandlerEntry(constraint, jtd.getConstraintCliqueMap()),
            bnd.getConstraints()));
  }

  private static void setJunctionTreeTablesList(JunctionTreeData jtd) {
    jtd.setJunctionTreeTables(
        jtd.getCliqueSet().stream()
            .map(Clique::getTable)
            .sorted(Comparator.comparing(table -> table.getNodes().size()))
            .toList());
  }

  private static void buildInternalMessagePassers(JunctionTreeData junctionTreeData) {
    Set<Clique> cliques = new HashSet<>(junctionTreeData.getCliqueSet());
    Clique smallest =
        cliques.stream().min(Comparator.comparing(c -> c.getNodes().size())).orElseThrow();
    cliques.remove(smallest);
    Set<Clique> joined = new HashSet<>(Set.of(smallest));
    junctionTreeData.setLeafCliques(new HashSet<>());
    recursivelyJoinCliques(
        smallest, cliques, joined, new JTATransferWriterFactory());
  }

  private static void buildExternalMessagePassers(
      JunctionTreeData jtd, BayesianNetworkData data, boolean writeBackToCPTs) {
    Set<Clique> cliques = jtd.getCliqueSet();

    JTATransferWriterFactory factory = new JTATransferWriterFactory();

    for (Node node : data.getNodes()) {
      ProbabilityTable networkTable = data.getNetworkTablesMap().get(node);
      MarginalTable observedTable = data.getObservationMap().get(node);

      Clique bestNetworkClique = getContainsScope(cliques, networkTable.getNodes());
      Clique bestObservationClique = getContainsScope(cliques, observedTable.getNodes());

      JunctionTreeTable cliqueTable = bestObservationClique.getTable();

      bestNetworkClique
          .getInitializeFrom()
          .add(factory.buildCopyInWriter(networkTable, bestNetworkClique.getTable()));

      bestObservationClique
          .getObservedWriters()
          .add(
              factory.buildCopyInWriter(
                  cliqueTable.getNodes(),
                  observedTable.getNodes(),
                  cliqueTable.getVector(),
                  observedTable.getVector()));

      if (writeBackToCPTs) {
        bestNetworkClique
            .getNetworkWriters()
            .add(factory.buildCopyInWriter(bestNetworkClique.getTable(), networkTable));
      }
    }
  }

  private static Map.Entry<ParameterConstraint, Clique> findSmallestClique(
      ParameterConstraint constraint, Set<Clique> cliques) {
    Clique smallestClique = getContainsScope(cliques, constraint.getAllNodes());
    return Map.entry(constraint, smallestClique);
  }

  private static Map.Entry<ParameterConstraint, JTAConstraintHandler> getConstraintHandlerEntry(
      ParameterConstraint constraint, Map<ParameterConstraint, Clique> constraintCliqueMap) {
    return Map.entry(
        constraint,
        buildConstraintHandler(constraint, constraintCliqueMap.get(constraint).getHandler()));
  }

  private static void recursivelyJoinCliques(
      Clique current,
      Set<Clique> available,
      Set<Clique> joined,
      JTATransferWriterFactory factory) {

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
          current.getSeparatorMap().put(nextClique, factory.build(current, nextClique));
          nextClique.getSeparatorMap().put(current, factory.build(nextClique, current));
          available.remove(nextClique);
          joined.add(nextClique);
          recursivelyJoinCliques(nextClique, available, joined, factory);
        });
  }

  private static Clique getContainsScope(Set<Clique> cliques, Set<Node> nodesInScope) {
    return cliques.stream()
        .filter(c -> c.getNodes().containsAll(nodesInScope))
        .min(Comparator.comparing(clique -> clique.getNodes().size()))
        .orElseThrow();
  }

  private static JTAConstraintHandler buildConstraintHandler(
      ParameterConstraint constraint, JTATableHandler jtaTableHandler) {
    if (Objects.requireNonNull(constraint) instanceof MarginalConstraint mc) {
      return new JTAConstraintHandlerMarginal(jtaTableHandler, mc);
    } else if (constraint instanceof ConditionalConstraint cc) {
      return new JTAConstraintHandlerConditional(jtaTableHandler, cc);
    }
    throw new IllegalStateException("Unexpected value: " + constraint);
  }

  private static Map.Entry<Clique, Integer> commonNodes(Clique clique, Clique current) {
    Set<Node> cliqueNodes = new HashSet<>(clique.getNodes());
    cliqueNodes.retainAll(current.getNodes());
    return Map.entry(clique, cliqueNodes.size());
  }
}
