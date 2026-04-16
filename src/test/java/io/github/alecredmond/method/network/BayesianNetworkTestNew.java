package io.github.alecredmond.method.network;

import static org.junit.jupiter.api.Assertions.*;

import io.github.alecredmond.exceptions.BayesNetIDException;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BayesianNetworkTestNew {
  static final List<Serializable> NODE_IDS = List.of("A", "B", "C", "D");
  BayesianNetwork test;

  @BeforeEach
  void setUp() {
    test = BayesianNetwork.newNetwork("TestNetwork");
    NODE_IDS.forEach(id -> test.addNewNode(id, List.of(id + "+", id + "-")));
  }

  @Nested
  class NodeCreationTests {

    static Stream<Arguments> nodeInputArgs_successExpected() {
      return Stream.of(
          Arguments.of("E", List.of("E+", "E-")),
          Arguments.of("F", List.of("F+", "F-")),
          Arguments.of("G", List.of("G+", "G-")),
          Arguments.of("H", List.of()),
          Arguments.of('A', List.of('B', 'C')), // <- char 'A' != string "A"
          Arguments.of(-1, List.of(2.0, (long) 3)),
          Arguments.of('E', List.of("Q", 7)),
          Arguments.of(new SerializableClass(), List.of("HELLO", "WORLD")));
    }

    static Stream<Arguments> nodeInputArgs_allErrors() {
      return Stream.concat(nodeInputArgs_nodeIdErrors(), nodeInputArgs_stateIdErrors());
    }

    static Stream<Arguments> nodeInputArgs_nodeIdErrors() {
      return Stream.of(
          Arguments.of("A", List.of("A+", "A-")),
          Arguments.of("B", List.of("G+", "G-")),
          Arguments.of(null, List.of("G+", "G-")));
    }

    static Stream<Arguments> nodeInputArgs_stateIdErrors() {
      return Stream.of(
          Arguments.of("F", List.of("A+", "F-")),
          Arguments.of("H", List.of("H+", "H+", "H-")),
          Arguments.of("G", listWithNull(List.of("G+", "G-"))));
    }

    private static List<String> listWithNull(List<String> strings) {
      List<String> s = new ArrayList<>();
      s.add(null);
      s.addAll(strings);
      return s;
    }

    @ParameterizedTest
    @MethodSource("nodeInputArgs_successExpected")
    void addNode_fromNodeObject_shouldSucceed(Serializable id, List<Serializable> stateIds) {
      assertDoesNotThrow(
          () -> {
            Node node = new Node(id, stateIds);
            test.addNode(node);
          });
      validateExistence(id, stateIds);
    }

    private void validateExistence(Serializable id, List<Serializable> stateIds) {
      Node node = test.getNode(id);
      assertNotNull(node);
      Set<Serializable> idSet = new HashSet<>(stateIds);
      node.getNodeStates().forEach(state -> assertTrue(idSet.contains(state.getId())));
    }

    @ParameterizedTest
    @MethodSource("nodeInputArgs_allErrors")
    void addNode_fromNodeObject_shouldThrowError(Serializable id, List<Serializable> stateIds) {
      assertThrows(
          BayesNetIDException.class,
          () -> {
            Node node = new Node(id, stateIds);
            test.addNode(node);
          });
    }

    @ParameterizedTest
    @MethodSource("nodeInputArgs_successExpected")
    void addNewNode_fromNodeArgs_shouldSucceed(Serializable id, List<Serializable> stateIds) {
      assertDoesNotThrow(
          () -> {
            test.addNewNode(id, stateIds);
          });
      validateExistence(id, stateIds);
    }

    @ParameterizedTest
    @MethodSource("nodeInputArgs_allErrors")
    void addNewNode_fromNodeArgs_shouldThrowError(Serializable id, List<Serializable> stateIds) {
      assertThrows(BayesNetIDException.class, () -> test.addNewNode(id, stateIds));
    }

    @ParameterizedTest
    @MethodSource("nodeInputArgs_successExpected")
    void addNewNode_onlyId_shouldSucceed(Serializable id) {
      assertDoesNotThrow(() -> test.addNewNode(id));
      assertNotNull(test.getNode(id));
    }

    @ParameterizedTest
    @MethodSource("nodeInputArgs_nodeIdErrors")
    void addNewNode_onlyId_shouldThrowError(Serializable id) {
      assertThrows(BayesNetIDException.class, () -> test.addNewNode(id));
    }

    @NoArgsConstructor
    static class SerializableClass implements Serializable {}
  }

  @Nested
  class NodeRemovalTests {

    static Stream<Arguments> nodeRemovalArgs() {
      Stream<Arguments> expectedTrue = NODE_IDS.stream().map(id -> Arguments.of(id, true));
      Stream<Arguments> expectedFalse =
          Stream.of(
              Arguments.of("E", false),
              Arguments.of("F", false),
              Arguments.of('A', false),
              Arguments.of(3, false));
      return Stream.concat(expectedTrue, expectedFalse);
    }

    @ParameterizedTest
    @MethodSource("nodeRemovalArgs")
    void removeById_shouldSucceed(Serializable id, boolean expected) {
      assertEquals(expected, test.removeNodeByID(id));
    }

    @ParameterizedTest
    @MethodSource("nodeRemovalArgs")
    void removeByNode_shouldSucceed(Serializable id, boolean expected) {
      assertEquals(expected, test.removeNode(test.getNode(id)));
    }

    @Test
    void removeAllNodes_shouldSucceed() {
      assertTrue(test.removeAllNodes());
    }

    @Test
    void removeAllNodes_emptyNetwork_shouldReturnFalse() {
      test.removeAllNodes();
      assertFalse(test.removeAllNodes());
    }
  }
}
