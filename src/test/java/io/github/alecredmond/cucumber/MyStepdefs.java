package io.github.alecredmond.cucumber;

import static org.junit.jupiter.api.Assertions.*;

import io.cucumber.java.en.*;
import io.github.alecredmond.exceptions.NodeStateValidationException;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import java.util.List;
import java.util.Map;

public class MyStepdefs {
  private static final Map<ExceptionEnum,Class<Exception>> exceptionMap = Map.of();
  private BayesianNetwork network;

  @Given("an empty network")
  public void newEmptyNetwork() {
    this.network = BayesianNetwork.newNetwork("Empty Network");
  }

  @When("adding a stateless node {string}")
  public void addingAStatelessNodeA(String string) {
    this.network.addNewNode(string);
  }

  @Then("a node with id {string} should be added")
  public void aNodeWithIdAShouldBeAdded(String string) {
    Node node = network.getNode(string);
    assertNotNull(node);
  }

  @But("building the network will fail with a node state validation exception")
  public void buildingTheNetworkWillFailWithANodeStateValidationException() {
    assertThrows(NodeStateValidationException.class, () -> network.buildNetworkData());
  }

  @And("all defined states should be included:")
  public void allDefinedStatesShouldBeIncluded(List<String> strings) {
    for (String id : strings) {
      assertNotNull(network.getNodeState(id));
    }
  }

  @And("building the network will succeed")
  public void buildingTheNetworkWillSucceed() {
    assertDoesNotThrow(() -> network.buildNetworkData());
  }

  @Given("I add the following node with states:")
  public void addingNodeWithTheFollowingStates(Map<String, List<String>> nodeInput) {
    nodeInput.forEach((nodeId, stateIds) -> network.addNewNode(nodeId, stateIds));
  }

  @Then("I add the following nodes:")
  public void iAddTheFollowingNodes(Map<String, List<String>> nodeInput) {
    nodeInput.forEach((nodeId, stateIds) -> network.addNewNode(nodeId, stateIds));
  }

  @Then("And define each node's parent structure:")
  public void andDefineEachNodeSParentStructure(Map<String, String> parentMap) {
    parentMap.forEach((childId, parentId) -> network.addParents(childId, parentId));
  }

  @And("after connecting {string} to its parent {string}")
  public void afterConnectingToItsParent(String childId, String parentId) {
    network.addParents(childId, parentId);
  }
}
