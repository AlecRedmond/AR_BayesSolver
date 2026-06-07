package io.github.alecredmond.internal.method.inference.junctiontree;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.probabilitytables.NetworkTable;
import io.github.alecredmond.export.application.probabilitytables.ObservedTable;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.method.inference.InferenceEngine.InferenceType;
import io.github.alecredmond.internal.application.inference.SolverConfigs;
import io.github.alecredmond.internal.application.inference.junctiontree.Clique;
import io.github.alecredmond.internal.application.inference.junctiontree.JunctionTreeData;
import io.github.alecredmond.internal.application.inference.junctiontree.Separator;
import io.github.alecredmond.internal.application.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.method.constraints.ConstraintRegistry;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintSolver;
import io.github.alecredmond.internal.method.inference.junctiontree.separators.CliqueJoiner;
import io.github.alecredmond.internal.method.probabilitytables.tablebuilders.ObservedTableBuilder;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.factory.TransferIteratorFactory;
import java.util.*;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@NoArgsConstructor
public class JTADataBuilder {
  public JunctionTreeData buildNewSolverConfiguration(
      BayesianNetworkData bayesianNetworkData, SolverConfigs configs) {
    JunctionTreeData junctionTreeData = new JunctionTreeData();
    junctionTreeData.setSolverConfig(true);
    junctionTreeData.setSolverType(configs.getSolverType());
    buildCommon(junctionTreeData, bayesianNetworkData);
    buildSolversPerClique(junctionTreeData, bayesianNetworkData);
    log.info("JUNCTION TREE DATA INITIALIZED IN SOLVER CONFIGURATION");
    return junctionTreeData;
  }

  private void buildCommon(
      JunctionTreeData junctionTreeData, BayesianNetworkData bayesianNetworkData) {
    junctionTreeData.setNetworkData(bayesianNetworkData);
    new JTACliqueBuilder().buildCliques(junctionTreeData);
    buildInternalMessagePassers(junctionTreeData);
    buildExternalMessagePassers(junctionTreeData, bayesianNetworkData);
  }

  private void buildSolversPerClique(JunctionTreeData jtd, BayesianNetworkData bnd) {
    List<ProbabilityConstraint> constraints = bnd.getConstraints();
    Map<Clique, List<ConstraintSolver>> map =
        Arrays.stream(jtd.getCliques())
            .map(clique -> Map.entry(clique, matchConstraints(clique, constraints)))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    jtd.setSolversPerClique(map);
  }

  private void buildInternalMessagePassers(JunctionTreeData jtd) {
    CliqueJoiner.join(jtd);

    // TODO - CLIQUE JOINER SHOULD ALSO PERFORM THE REST OF THIS METHOD
    Separator[] separators =
        Arrays.stream(jtd.getCliques())
            .flatMap(clique -> clique.getSeparatorMap().values().stream())
            .distinct()
            .toArray(Separator[]::new);

    jtd.setSeparators(separators);
  }

  private void buildExternalMessagePassers(JunctionTreeData jtd, BayesianNetworkData bnd) {
    boolean writeBackToNetwork = jtd.isSolverConfig();
    Clique[] cliques = jtd.getCliques();

    TransferIteratorFactory builder = new TransferIteratorFactory();

    Map<Node, NetworkTable> networkTables = bnd.getNetworkTablesMap();
    Map<Node, ObservedTable> observedTables = jtd.getObservedTablesMap();

    for (Node node : bnd.getNodes()) {
      ProbabilityTable networkTable = networkTables.get(node);

      Clique bestClique = getContainsScope(cliques, networkTable.getNodes());
      JunctionTreeTable cliqueTable = bestClique.getTable();

      bestClique.getWriteFromCPTs().add(builder.buildMultiplyInTransfer(networkTable, cliqueTable));

      if (writeBackToNetwork) {
        bestClique.getWriteToCPTs().add(builder.buildMarginalTransfer(cliqueTable, networkTable));
        continue;
      }

      bestClique
          .getWriteToObserved()
          .add(builder.buildMarginalTransfer(cliqueTable, observedTables.get(node)));
    }
  }

  private List<ConstraintSolver> matchConstraints(
      Clique clique, List<ProbabilityConstraint> constraints) {
    return constraints.stream()
        .filter(constraint -> clique.getNodes().containsAll(constraint.getAllNodes()))
        .map(constraint -> buildConstraintHandler(constraint, clique))
        .toList();
  }

  private Clique getContainsScope(Clique[] cliques, Set<Node> nodesInScope) {
    return Arrays.stream(cliques)
        .filter(c -> c.getNodes().containsAll(nodesInScope))
        .min(Comparator.comparing(clique -> clique.getNodes().size()))
        .orElseThrow();
  }

  @SuppressWarnings("unchecked")
  private <T extends ProbabilityConstraint> ConstraintSolver buildConstraintHandler(
      @NonNull T constraint, Clique clique) {
    return ConstraintRegistry.getStrategy((Class<T>) constraint.getClass())
        .buildSolverHandler(clique.getHandler(), constraint);
  }

  public JunctionTreeData buildNewInferenceConfiguration(
      BayesianNetworkData bayesianNetworkData, InferenceType inferenceType) {
    JunctionTreeData junctionTreeData = new JunctionTreeData();
    buildInferenceConfiguration(junctionTreeData, bayesianNetworkData, inferenceType);
    return junctionTreeData;
  }

  public void buildInferenceConfiguration(
      JunctionTreeData jtd, BayesianNetworkData bnd, InferenceType inferenceType) {
    jtd.setSolverConfig(false);
    jtd.setInferenceType(inferenceType);
    buildObserved(jtd, bnd);
    buildCommon(jtd, bnd);
    log.info("JUNCTION TREE DATA INITIALIZED IN INFERENCE CONFIGURATION");
  }

  private void buildObserved(JunctionTreeData jtd, BayesianNetworkData bnd) {
    ObservedTableBuilder builder = new ObservedTableBuilder();
    jtd.setObservedEvidence(new HashMap<>());
    jtd.setObservedTablesMap(
        bnd.getNodes().stream()
            .map(node -> Map.entry(node, builder.buildTable(node)))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
  }
}
