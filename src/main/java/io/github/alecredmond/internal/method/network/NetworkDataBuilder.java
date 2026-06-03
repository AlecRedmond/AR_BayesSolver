package io.github.alecredmond.internal.method.network;

import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.NetworkTable;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.method.probabilitytables.TableHelper;
import io.github.alecredmond.internal.method.probabilitytables.TableBuilder;
import io.github.alecredmond.internal.method.utils.MapUtils;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.Data;

@Data
public class NetworkDataBuilder {
  private final BayesianNetworkData networkData;

  public void build() {
    Map<Node, Integer> layerMap = orderNodes();
    rebuildIdMaps(networkData.getNodes());
    buildNetworkTablesMap(layerMap);
    marginalizeAllTables();
  }

  public Map<Node, Integer> orderNodes() {
    Map<Node, Integer> layerMap = new HashMap<>();
    networkData.getNodeIDsMap().values().forEach(node -> calculateNodeLayer(node, layerMap));

    List<Node> nodesOrdered =
        layerMap.entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .toList();

    networkData.setNodes(nodesOrdered);
    return layerMap;
  }

  public static int calculateNodeLayer(Node node, Map<Node, Integer> layerMap) {
    if (layerMap.containsKey(node)) return layerMap.get(node);

    int layer =
        node.getParents().stream()
            .mapToInt(parent -> calculateNodeLayer(parent, layerMap) + 1)
            .max()
            .orElse(0);

    layerMap.put(node, layer);
    return layer;
  }

  public void rebuildIdMaps(Collection<Node> nodes) {
    new NetworkIdValidator(networkData).validateRebuild(nodes);
    networkData.setNodeIDsMap(createNodeIdMap(nodes));
    networkData.setNodeStateIDsMap(createNodeStateIdMap(nodes));
  }

  public static Map<Serializable, Node> createNodeIdMap(Collection<Node> nodes) {
    return MapUtils.mapFromInput(nodes, Node::getId, Function.identity());
  }

  public static Map<Serializable, NodeState> createNodeStateIdMap(Collection<Node> nodes) {
    return nodes.stream()
        .flatMap(n -> n.getNodeStates().stream())
        .map(ns -> Map.entry(ns.getId(), ns))
        .collect(MapUtils.collectMap());
  }

  public void marginalizeAllTables() {
    networkData.getNetworkTablesMap().values().stream()
        .map(ProbabilityTable::getHelper)
        .forEach(TableHelper::marginalizeTable);
  }

  public void buildNetworkTablesMap(Map<Node, Integer> layerMap) {
    networkData.setNetworkTablesMap(new HashMap<>());
    networkData
        .getNodes()
        .forEach(
            node -> {
              List<Node> events = List.of(node);
              List<Node> conditions = orderConditions(node.getParents(), layerMap);
              NetworkTable table = TableBuilder.buildNetworkTable(events, conditions);
              table.getHelper().marginalizeTable();
              networkData.getNetworkTablesMap().put(node, table);
            });
  }

  public List<Node> orderConditions(List<Node> parents, Map<Node, Integer> layerMap) {
    return parents.stream()
        .map(node -> Map.entry(node, layerMap.get(node)))
        .sorted(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .toList();
  }
}
