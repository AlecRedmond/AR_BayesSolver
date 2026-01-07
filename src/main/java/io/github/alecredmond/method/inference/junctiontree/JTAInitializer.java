package io.github.alecredmond.method.inference.junctiontree;

import io.github.alecredmond.application.constraints.ConditionalConstraint;
import io.github.alecredmond.application.constraints.MarginalConstraint;
import io.github.alecredmond.application.constraints.ParameterConstraint;
import io.github.alecredmond.application.inference.junctiontree.Clique;
import io.github.alecredmond.application.inference.junctiontree.JunctionTreeData;
import io.github.alecredmond.application.inference.junctiontree.Separator;
import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.application.probabilitytables.junctiontree.JunctionTreeTable;
import io.github.alecredmond.method.inference.junctiontree.handlers.JTAConstraintHandler;
import io.github.alecredmond.method.inference.junctiontree.handlers.JTAConstraintHandlerConditional;
import io.github.alecredmond.method.inference.junctiontree.handlers.JTAConstraintHandlerMarginal;
import io.github.alecredmond.method.inference.junctiontree.handlers.JTATableHandler;
import io.github.alecredmond.method.probabilitytables.TableBuilder;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JTAInitializer {

  private JTAInitializer() {}

  public static JunctionTreeData buildSolverConfiguration(BayesianNetworkData bayesianNetworkData) {
    JunctionTreeData data = new JunctionTreeData();
    buildCommon(data, bayesianNetworkData);
    Set<Clique> cliqueSet = JTACliqueBuilder.buildCliques(bayesianNetworkData);
    JunctionTreeData.JunctionTreeDataBuilder builder = build(bayesianNetworkData, cliqueSet);

    Map<ParameterConstraint, Clique> cliqueForConstraint =
        findCliquesForConstraints(cliqueSet, bayesianNetworkData);
    Map<ParameterConstraint, JTAConstraintHandler> constraintHandlerMap =
        buildConstraintIndexerMap(bayesianNetworkData, cliqueForConstraint);

    log.info("CONSTRAINT INDEXERS BUILT");

    return builder
        .cliqueForConstraint(cliqueForConstraint)
        .constraintHandlers(constraintHandlerMap)
        .build();
  }

  public static JunctionTreeData buildInferenceConfiguration(
      BayesianNetworkData bayesianNetworkData) {
    return build(bayesianNetworkData, JTACliqueBuilder.buildCliques(bayesianNetworkData))
        .cliqueForConstraint(new HashMap<>())
        .constraintHandlers(new HashMap<>())
        .build();
  }

  private static void buildCommon(
      JunctionTreeData junctionTreeData, BayesianNetworkData bayesianNetworkData) {

  }

  private static JunctionTreeData.JunctionTreeDataBuilder build(
      BayesianNetworkData bayesNetData, Set<Clique> cliqueSet) {
    Set<Separator> separators = buildSeparators(cliqueSet);
    Set<Clique> leafCliques = buildLeafCliques(cliqueSet);
    Map<Clique, Set<ProbabilityTable>> associatedTables =
        buildAssociatedTablesMap(cliqueSet, bayesNetData);
    List<JunctionTreeTable> junctionTreeTables = buildTreeTablesList(cliqueSet, separators);

    log.info("CLIQUES BUILT");

    return JunctionTreeData.builder()
        .bayesianNetworkData(bayesNetData)
        .cliqueSet(cliqueSet)
        .separators(separators)
        .leafCliques(leafCliques)
        .associatedTables(associatedTables)
        .junctionTreeTables(junctionTreeTables);
  }

  private static Map<ParameterConstraint, Clique> findCliquesForConstraints(
      Set<Clique> cliques, BayesianNetworkData data) {
    Map<ParameterConstraint, Clique> cfc = new HashMap<>();
    for (ParameterConstraint constraint : data.getConstraints()) {
      Clique bestClique =
          cliques.stream()
              .filter(clique -> clique.getNodes().containsAll(constraint.getAllNodes()))
              .min(Comparator.comparingInt(clique -> clique.getNodes().size()))
              .orElseThrow();

      cfc.put(constraint, bestClique);
    }
    return cfc;
  }

  private static Map<ParameterConstraint, JTAConstraintHandler> buildConstraintIndexerMap(
      BayesianNetworkData data, Map<ParameterConstraint, Clique> constraintCliqueMap) {
    return data.getConstraints().stream()
        .map(c -> Map.entry(c, buildConstraintHandler(c, constraintCliqueMap.get(c).getHandler())))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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

  private static List<JunctionTreeTable> buildTreeTablesList(
      Set<Clique> cliques, Set<Separator> separators) {
    List<JunctionTreeTable> allTables = new ArrayList<>();
    cliques.stream().map(Clique::getTable).forEach(allTables::add);
    separators.stream().map(Separator::getTable).forEach(allTables::add);
    return allTables.stream()
        .sorted(Comparator.comparingInt(table -> table.getProbabilities().length))
        .toList();
  }

  private static Map<Clique, Set<ProbabilityTable>> buildAssociatedTablesMap(
      Set<Clique> cliques, BayesianNetworkData data) {
    Map<Clique, Set<ProbabilityTable>> associated =
        cliques.stream()
            .map(c -> Map.entry(c, new HashSet<ProbabilityTable>()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    for (ProbabilityTable table : data.getNetworkTablesMap().values()) {
      Set<Node> tableNodes = table.getNodes();
      Clique containsScope =
          cliques.stream()
              .filter(c -> c.getNodes().containsAll(tableNodes))
              .findAny()
              .orElseThrow();
      associated.get(containsScope).add(table);
    }

    return associated;
  }

  private static Set<Separator> buildSeparators(Set<Clique> cliquesSet) {
    List<Clique> cliqueList = new ArrayList<>(cliquesSet);

    List<List<Clique>> sortedPairs = getSortedPairs(cliqueList);

    int separatorsNeeded = cliqueList.size() - 1;
    List<Separator> separators = new ArrayList<>();

    sortedPairs.forEach(pair -> joinBestPairs(pair, separators, separatorsNeeded));

    return new HashSet<>(separators);
  }

  private static void joinBestPairs(
      List<Clique> pair, List<Separator> separators, int separatorsNeeded) {
    if (separators.size() == separatorsNeeded) return;
    Clique cliqueA = pair.getFirst();
    Clique cliqueB = pair.getLast();

    boolean containsLoop = false;
    for (Clique clique : cliqueA.getSeparatorMap().keySet()) {
      containsLoop = checkForLoops(clique, Set.of(), new HashSet<>());
      if (containsLoop) break;
    }
    if (containsLoop) return;
    Separator s = addSeparatorForNodes(cliqueA, cliqueB);
    separators.add(s);
  }

  private static List<List<Clique>> getSortedPairs(List<Clique> cliques) {
    return getSharedNodesPerPair(cliques).entrySet().stream()
        .sorted(Map.Entry.<List<Clique>, Integer>comparingByValue().reversed())
        .map(Map.Entry::getKey)
        .toList();
  }

  private static Map<List<Clique>, Integer> getSharedNodesPerPair(List<Clique> cliques) {
    Map<List<Clique>, Integer> sharedNodesPerPair = new HashMap<>();
    for (int i = 0; i < cliques.size(); i++) {
      for (int j = i + 1; j < cliques.size(); j++) {
        Clique cliqueA = cliques.get(i);
        Clique cliqueB = cliques.get(j);
        int commonNodes =
            cliqueA.getNodes().stream()
                .filter(cliqueB.getNodes()::contains)
                .collect(Collectors.toSet())
                .size();
        sharedNodesPerPair.put(List.of(cliqueA, cliqueB), commonNodes);
      }
    }
    return sharedNodesPerPair;
  }

  private static Separator addSeparatorForNodes(Clique cliqueA, Clique cliqueB) {
    Set<Node> common = JTACliqueBuilder.intersectionOf(cliqueA.getNodes(), cliqueB.getNodes());
    Separator separator =
        new Separator(cliqueA, cliqueB, common, TableBuilder.buildJunctionTreeTable(common));

    cliqueA.getSeparatorMap().put(cliqueB, separator);
    cliqueB.getSeparatorMap().put(cliqueA, separator);
    return separator;
  }

  private static boolean checkForLoops(
      Clique clique, Set<Clique> lastClique, Set<Clique> currentChain) {
    currentChain.add(clique);

    Set<Clique> connectedCliques =
        clique.getSeparatorMap().keySet().stream()
            .filter(c -> !lastClique.contains(c))
            .collect(Collectors.toSet());

    if (connectedCliques.isEmpty()) {
      currentChain.remove(clique);
      return false;
    }

    boolean loopFound = connectedCliques.stream().anyMatch(currentChain::contains);

    if (loopFound) {
      currentChain.remove(clique);
      return true;
    }

    for (Clique connectedClique : connectedCliques) {
      loopFound = checkForLoops(connectedClique, Set.of(clique), currentChain);
      if (loopFound) break;
    }

    currentChain.remove(clique);
    return loopFound;
  }

  private static Set<Clique> buildLeafCliques(Set<Clique> cliques) {
    return cliques.stream()
        .filter(clique -> clique.getSeparators().size() <= 1)
        .collect(Collectors.toSet());
  }
}
