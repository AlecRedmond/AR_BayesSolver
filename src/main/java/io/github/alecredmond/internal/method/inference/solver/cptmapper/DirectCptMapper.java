package io.github.alecredmond.internal.method.inference.solver.cptmapper;

import io.github.alecredmond.exceptions.CptDirectMappingException;
import io.github.alecredmond.export.application.constraints.ConditionalConstraint;
import io.github.alecredmond.export.application.constraints.MarginalConstraint;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.probabilitytables.ConditionalTable;
import io.github.alecredmond.export.application.probabilitytables.NetworkTable;
import io.github.alecredmond.export.application.probabilitytables.RootNodeTable;
import io.github.alecredmond.internal.application.network.CptMapperData;
import io.github.alecredmond.internal.method.constraints.types.conditionalconstraint.ConditionalConstraintValidator;
import io.github.alecredmond.internal.method.constraints.types.marginalconstraint.MarginalConstraintValidator;

import java.util.*;
import java.util.function.BiFunction;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DirectCptMapper {
  private final BayesianNetworkData networkData;
  private final CptMapperData mapperData;

  public DirectCptMapper(BayesianNetworkData networkData) {
    this.networkData = networkData;
    this.mapperData = new CptMapperData(networkData);
    fillCptTables();
  }

  private void fillCptTables() {
    mapperData.getConditionalTableMap().clear();
    mapperData.getRootNodeTableMap().clear();
    for (NetworkTable networkTable : networkData.getNetworkTablesMap().values()) {
      if (networkTable instanceof RootNodeTable rnt) {
        mapperData.getRootNodeTableMap().put(networkTable.getNetworkNode(), rnt);
      } else if (networkTable instanceof ConditionalTable ct) {
        mapperData.getConditionalTableMap().put(networkTable.getNetworkNode(), ct);
      }
    }
  }

  public boolean tryDirectImpute() {
    mapperData.reset();
    fillCptTables();
    if (!onlyConditionalAndMarginalConstraintsInNetwork()) return false;
    if (!marginalConstraintsMatchRootNodeTables()) return false;
    if (!conditionalConstraintsMatchConditionalTables()) return false;
    if (!hasRequiredNumberOfConstraintsPerCPT()) return false;
    return allCPTsMappedSuccessfully();
  }

  private boolean onlyConditionalAndMarginalConstraintsInNetwork() {
    return mapperData.getAllConstraints().stream()
        .allMatch(p -> p instanceof MarginalConstraint || p instanceof ConditionalConstraint);
  }

  private boolean marginalConstraintsMatchRootNodeTables() {
    Map<Node, List<ProbabilityConstraint>> constraintMap = mapperData.getCptConstraints();
    Map<Node, RootNodeTable> rootNodeTables = mapperData.getRootNodeTableMap();
    for (MarginalConstraint constraint : getConstraints(MarginalConstraint.class)) {
      Node node = constraint.getEventNode();
      if (!rootNodeTables.containsKey(node)) return false;
      constraintMap.computeIfAbsent(node, n -> new ArrayList<>());
      constraintMap.get(node).add(constraint);
    }
    return true;
  }

  private boolean conditionalConstraintsMatchConditionalTables() {
    Map<Node, List<ProbabilityConstraint>> constraintMap = mapperData.getCptConstraints();
    Map<Node, ConditionalTable> conditionalTableMap = mapperData.getConditionalTableMap();
    for (ConditionalConstraint constraint : getConstraints(ConditionalConstraint.class)) {
      Node node = constraint.getEventNode();
      if (!conditionalTableMap.containsKey(node)) return false;
      ConditionalTable table = conditionalTableMap.get(node);
      if (!table.getConditions().equals(constraint.getConditionNodes())) return false;
      constraintMap.computeIfAbsent(node, n -> new ArrayList<>());
      constraintMap.get(node).add(constraint);
    }
    return true;
  }

  private boolean hasRequiredNumberOfConstraintsPerCPT() {
    return networkData.getNetworkTablesMap().values().stream()
        .allMatch(this::meetsMinimumConstraintsRequired);
  }

  private boolean allCPTsMappedSuccessfully() {
    MarginalConstraintValidator mcv = new MarginalConstraintValidator();
    ConditionalConstraintValidator ccv = new ConditionalConstraintValidator();
    try {
      runMapperIterators(
          mapperData.getRootNodeTableMap(),
          MarginalConstraint.class,
          (table, constraints) -> new RootCPTMapperIterator(table, constraints, mcv, networkData));
      runMapperIterators(
          mapperData.getConditionalTableMap(),
          ConditionalConstraint.class,
          (table, constraints) ->
              new ConditionalCPTMapperIterator(table, constraints, ccv, networkData));
      return true;
    } catch (CptDirectMappingException e) {
      revertMappedValues();
      return false;
    }
  }

  private <T extends ProbabilityConstraint> List<T> getConstraints(Class<T> tClass) {
    return mapperData.getAllConstraints().stream()
        .filter(tClass::isInstance)
        .map(tClass::cast)
        .distinct()
        .toList();
  }

  private boolean meetsMinimumConstraintsRequired(NetworkTable table) {
    Node networkNode = table.getNetworkNode();
    if (!mapperData.getCptConstraints().containsKey(networkNode)) return false;
    int numConstraints = mapperData.getCptConstraints().get(networkNode).size();
    int cptEntries = table.getProbabilities().length;
    if (numConstraints == cptEntries) return true;
    int numEventStates = networkNode.getNodeStates().size();
    int minimumEntries = (cptEntries / numEventStates) * (numEventStates - 1);
    return numConstraints >= minimumEntries;
  }

  private <
          T extends NetworkTable,
          P extends ProbabilityConstraint,
          I extends CptMapperIterator<T, P>>
      void runMapperIterators(
          Map<Node, T> tableMap,
          Class<P> constraintClass,
          BiFunction<T, List<P>, I> mapperConstructor) {
    tableMap.forEach(
        (node, table) -> {
          List<P> constraints =
              mapperData.getCptConstraints().get(node).stream()
                  .filter(constraintClass::isInstance)
                  .map(constraintClass::cast)
                  .toList();

          I iterator = mapperConstructor.apply(table, constraints);
          List<P> addedConstraints = iterator.directMapCPTs();
          addedConstraints.forEach(mapperData.getAllConstraints()::add);
          mapperData.getDirectInputSuccess().add(node);
        });
  }

  private void revertMappedValues() {
    mapperData.getDirectInputSuccess().stream()
        .map(networkData.getNetworkTablesMap()::get)
        .forEach(
            table -> {
              Arrays.fill(table.getProbabilities(), 1.0);
              table.getHelper().normalizeTable();
            });
  }
}
