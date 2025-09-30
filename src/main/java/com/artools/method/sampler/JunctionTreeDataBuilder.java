package com.artools.method.sampler;

import com.artools.application.constraints.ConditionalConstraint;
import com.artools.application.constraints.MarginalConstraint;
import com.artools.application.constraints.ParameterConstraint;
import com.artools.application.network.BayesNetData;
import com.artools.application.node.Node;
import com.artools.application.probabilitytables.JunctionTreeTable;
import com.artools.application.probabilitytables.ProbabilityTable;
import com.artools.application.sampler.Clique;
import com.artools.application.sampler.JunctionTreeData;
import com.artools.application.sampler.Separator;
import com.artools.method.jtahandlers.ConditionalHandler;
import com.artools.method.jtahandlers.ConstraintHandler;
import com.artools.method.jtahandlers.JunctionTableHandler;
import com.artools.method.jtahandlers.MarginalHandler;
import com.artools.method.probabilitytables.TableBuilder;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JunctionTreeDataBuilder {

  private JunctionTreeDataBuilder() {}

  public static JunctionTreeData build(BayesNetData bayesNetData) {
    if (satisfactoryDataExists(bayesNetData)) return bayesNetData.getJunctionTreeData();

    Set<Clique> cliqueSet = CliqueBuilder.buildCliques(bayesNetData);
    Set<Separator> separators = buildSeparators(cliqueSet);
    Set<Clique> leafCliques = buildLeafCliques(cliqueSet);
    Map<Clique, Set<ProbabilityTable>> associatedTables =
        buildAssociatedTablesMap(cliqueSet, bayesNetData);
    List<JunctionTreeTable> junctionTreeTables = buildAllTablesList(cliqueSet, separators);

    log.info("CLIQUES BUILT");

    JunctionTreeData.JunctionTreeDataBuilder builder =
        JunctionTreeData.builder()
            .bayesNetData(bayesNetData)
            .cliqueSet(cliqueSet)
            .separators(separators)
            .leafCliques(leafCliques)
            .associatedTables(associatedTables)
            .junctionTreeTables(junctionTreeTables);

    return bayesNetData.isSolved()
        ? buildDataForSolved(bayesNetData, builder)
        : buildDataForUnsolved(bayesNetData, cliqueSet, builder);
  }

  private static JunctionTreeData buildDataForUnsolved(
      BayesNetData bayesNetData,
      Set<Clique> cliqueSet,
      JunctionTreeData.JunctionTreeDataBuilder builder) {
    Map<ParameterConstraint, Clique> cliqueForConstraint =
        findCliquesForConstraints(cliqueSet, bayesNetData);
    Map<ParameterConstraint, ConstraintHandler> constraintHandlerMap =
        buildConstraintIndexerMap(bayesNetData, cliqueForConstraint);

    log.info("CONSTRAINT INDEXERS BUILT");

    JunctionTreeData jtd =
        builder
            .cliqueForConstraint(cliqueForConstraint)
            .constraintHandlers(constraintHandlerMap)
            .build();

    bayesNetData.setJunctionTreeData(jtd);

    return jtd;
  }

  private static JunctionTreeData buildDataForSolved(
      BayesNetData bayesNetData, JunctionTreeData.JunctionTreeDataBuilder builder) {
    JunctionTreeData jtd =
        builder.cliqueForConstraint(new HashMap<>()).constraintHandlers(new HashMap<>()).build();

    bayesNetData.setJunctionTreeData(jtd);
    return jtd;
  }

  private static boolean satisfactoryDataExists(BayesNetData bayesNetData) {
    if (!bayesNetData.isSolved()) return false;
    return bayesNetData.getJunctionTreeData().getConstraintHandlers().isEmpty();
  }

  private static Map<ParameterConstraint, ConstraintHandler> buildConstraintIndexerMap(
      BayesNetData data, Map<ParameterConstraint, Clique> constraintCliqueMap) {
    return data.getConstraints().stream()
        .map(c -> Map.entry(c, buildConstraintHandler(c, constraintCliqueMap.get(c).getHandler())))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static ConstraintHandler buildConstraintHandler(
      ParameterConstraint constraint, JunctionTableHandler junctionTableHandler) {
    switch (constraint) {
      case MarginalConstraint mc -> {
        return new MarginalHandler(junctionTableHandler, mc);
      }
      case ConditionalConstraint cc -> {
        return new ConditionalHandler(junctionTableHandler, cc);
      }
      default -> throw new IllegalStateException("Unexpected value: " + constraint);
    }
  }

  private static Map<ParameterConstraint, Clique> findCliquesForConstraints(
      Set<Clique> cliques, BayesNetData data) {
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

  private static List<JunctionTreeTable> buildAllTablesList(
      Set<Clique> cliques, Set<Separator> separators) {
    List<JunctionTreeTable> allTables = new ArrayList<>();
    cliques.stream().map(Clique::getTable).forEach(allTables::add);
    separators.stream().map(Separator::getTable).forEach(allTables::add);
    return allTables;
  }

  private static Map<Clique, Set<ProbabilityTable>> buildAssociatedTablesMap(
      Set<Clique> cliques, BayesNetData data) {
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
    Set<Node> common = CliqueBuilder.intersectionOf(cliqueA.getNodes(), cliqueB.getNodes());
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
