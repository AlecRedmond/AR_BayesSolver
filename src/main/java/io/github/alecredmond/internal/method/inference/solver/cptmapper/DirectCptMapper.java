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
import io.github.alecredmond.internal.application.network.cptmapper.CptMapperData;
import io.github.alecredmond.internal.application.network.cptmapper.DirectMapperConditionalNodeInput;
import io.github.alecredmond.internal.application.network.cptmapper.DirectMapperNodeInput;
import io.github.alecredmond.internal.application.network.cptmapper.DirectMapperRootNodeInput;
import io.github.alecredmond.internal.method.inference.solver.cptmapper.report.CptMappingReport;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("rawtypes")
@Slf4j
public class DirectCptMapper {
  private final BayesianNetworkData networkData;
  private final CptMapperData mapperData;

  public DirectCptMapper(BayesianNetworkData networkData) {
    this.networkData = networkData;
    this.mapperData = new CptMapperData(networkData);
  }

  public boolean tryDirectImpute() {
    List<CptMappingReport> reports = performDirectImpute();

    if (!onlyConditionalAndMarginalConstraintsInNetwork()) return false;
    fillMapperNodes();
    return marginalConstraintsMatchRootNodeTables()
        && conditionalConstraintsMatchConditionalTables()
        && hasNecessaryNumberOfConstraintsPerCPT()
        && directCPTMappingRanSuccessfully();
  }

    private List<CptMappingReport> performDirectImpute() {
      //TODO =THIS
    }

    private boolean onlyConditionalAndMarginalConstraintsInNetwork() {
    return mapperData.getAllConstraints().stream()
        .allMatch(p -> p instanceof MarginalConstraint || p instanceof ConditionalConstraint);
  }

  private void fillMapperNodes() {
    mapperData.setMapperNodes(
        networkData.getNetworkTablesMap().values().parallelStream()
            .map(this::buildNodeMapper)
            .map(input -> Map.entry(input.getNode(), input))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
  }

  private boolean marginalConstraintsMatchRootNodeTables() {
    Map<Node, DirectMapperNodeInput> mapperNodes = mapperData.getMapperNodes();
    for (MarginalConstraint constraint : getAllConstraints(MarginalConstraint.class)) {
      Node node = constraint.getEventNode();
      if (!(mapperNodes.get(node) instanceof DirectMapperRootNodeInput rootInput)) {
        return false;
      }
      rootInput.getValidConstraints().add(constraint);
    }
    return true;
  }

  private boolean conditionalConstraintsMatchConditionalTables() {
    Map<Node, DirectMapperNodeInput> mapperNodes = mapperData.getMapperNodes();
    for (ConditionalConstraint constraint : getAllConstraints(ConditionalConstraint.class)) {
      Node node = constraint.getEventNode();
      if (!(mapperNodes.get(node) instanceof DirectMapperConditionalNodeInput conditionalInput)) {
        return false;
      }
      ConditionalTable table = conditionalInput.getNetworkTable();
      if (!table.getConditions().equals(constraint.getConditionNodes())) return false;
      conditionalInput.getValidConstraints().add(constraint);
    }
    return true;
  }

  private boolean hasNecessaryNumberOfConstraintsPerCPT() {
    return mapperData.getMapperNodes().values().stream()
        .allMatch(this::meetsMinimumConstraintsRequired);
  }

  private boolean directCPTMappingRanSuccessfully() {
    try {
      mapperData.getMapperNodes().values().parallelStream()
          .filter(DirectMapperNodeInput::runIterator)
          .map(DirectMapperNodeInput::getAddedConstraints)
          .sequential()
          .forEach(mapperData.getAllConstraints()::addAll);
      return true;
    } catch (CptDirectMappingException e) {
      revertMappedValues();
      return false;
    } catch (Exception e) {
      revertMappedValues();
      throw e;
    }
  }

  private DirectMapperNodeInput buildNodeMapper(NetworkTable table) {
    Node node = table.getNetworkNode();
    DirectMapperNodeInput mapperNode;
    switch (table) {
      case RootNodeTable rnt -> mapperNode = buildRootNodeMapper(node, rnt);
      case ConditionalTable ct -> mapperNode = buildConditionalNodeMapper(node, ct);
      default -> throw new IllegalStateException("Unexpected value: " + table);
    }
    return mapperNode;
  }

  private <T extends ProbabilityConstraint> List<T> getAllConstraints(Class<T> tClass) {
    return mapperData.getAllConstraints().stream()
        .filter(tClass::isInstance)
        .map(tClass::cast)
        .distinct()
        .toList();
  }

  private boolean meetsMinimumConstraintsRequired(DirectMapperNodeInput mapperNode) {
    int numConstraints = mapperNode.getValidConstraints().size();
    int minimumRequired = mapperNode.getMinimumCPTEntries();
    return numConstraints >= minimumRequired;
  }

  private void revertMappedValues() {
    mapperData.getMapperNodes().values().parallelStream()
        .filter(DirectMapperNodeInput::isRunSuccess)
        .map(DirectMapperNodeInput::getNetworkTable)
        .forEach(
            table -> {
              Arrays.fill(table.getProbabilities(), 1.0);
              table.getQueryTool().normalizeTable();
            });
  }

  private DirectMapperNodeInput buildRootNodeMapper(Node node, RootNodeTable rnt) {
    return new DirectMapperRootNodeInput(node, rnt, mapperData.getMarginalValidator());
  }

  private DirectMapperNodeInput buildConditionalNodeMapper(Node node, ConditionalTable ct) {
    return new DirectMapperConditionalNodeInput(node, ct, mapperData.getConditionalValidator());
  }
}
