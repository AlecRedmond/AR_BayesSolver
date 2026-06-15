package io.github.alecredmond.export.method.probabilitytables;

import static io.github.alecredmond.TestConfigs.DOUBLE_EQUALITY;
import static io.github.alecredmond.TestConfigs.SOLVE_LONG_TESTS;
import static io.github.alecredmond.export.method.network.NetworkScenario.FANTASY_GRAPH;
import static org.junit.jupiter.api.Assertions.*;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ConditionalTable;
import io.github.alecredmond.export.application.probabilitytables.NetworkTable;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.export.method.network.NetworkScenario;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.vectoriterator.misciterators.StateCombinationGenerator;
import java.io.Serializable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
class TableHelperTest {

  public static Stream<Arguments> provideNormalizeTableArgs() {
    return provideArgs(
        (t, n) -> Stream.of(Arguments.of(t)), s -> s.get().buildNetworkData(), t -> true);
  }

  static Stream<Arguments> provideArgs(
      BiFunction<NetworkTable, BayesianNetwork, Stream<Arguments>> function,
      Function<NetworkScenario, BayesianNetwork> networkInit,
      Predicate<? super NetworkTable> tableFilter) {
    return Arrays.stream(NetworkScenario.values())
        .filter(networkScenario -> !FANTASY_GRAPH.equals(networkScenario) || SOLVE_LONG_TESTS)
        .map(networkInit)
        .flatMap(
            network ->
                network.getNetworkData().getNetworkTablesMap().values().stream()
                    .filter(tableFilter)
                    .map(t -> Optional.ofNullable(function.apply(t, network)))
                    .filter(Optional::isPresent)
                    .flatMap(Optional::get));
  }

  public static Stream<Arguments> provideProbabilitySetMapTables() {
    return provideTableOnly(ConditionalTable.class::isInstance);
  }

  static Stream<Arguments> provideTableOnly(Predicate<? super NetworkTable> tableFilter) {
    return provideSolvedArgs((table, network) -> Stream.of(Arguments.of(table)), tableFilter);
  }

  static Stream<Arguments> provideSolvedArgs(
      BiFunction<NetworkTable, BayesianNetwork, Stream<Arguments>> function,
      Predicate<? super NetworkTable> tableFilter) {
    return provideArgs(function, scenario -> scenario.get().solveNetwork(), tableFilter);
  }

  public static Stream<Arguments> provideSafeModeArgs() {
    return provideTableOnly(ConditionalTable.class::isInstance);
  }

  public static Stream<Arguments> provideCopyTableArgs() {
    return provideTableOnly(t -> true);
  }

  public static Stream<Arguments> provideGetConditionProbArgs() {
    return provideSolvedArgs(TableHelperTest::allConditionsForTable, t -> true);
  }

  private static Stream<Arguments> allConditionsForTable(NetworkTable t, BayesianNetwork n) {
    List<ProbabilityConstraint> eventStateConstraints =
        n.getNetworkData().getConstraints().stream()
            .filter(c -> c.getEventNodes().equals(t.getEvents()))
            .filter(c -> c.getEventStates().size() == 1)
            .filter(c -> c.getConditionNodes().equals(t.getConditions()))
            .toList();

    if (eventStateConstraints.isEmpty()) return null;

    List<Collection<NodeState>> conditions =
        new StateCombinationGenerator(t).generateCombos(t.getConditions(), ArrayList::new);

    if (conditions.isEmpty()) {
      Map<NodeState, ProbabilityConstraint> map = new HashMap<>();
      eventStateConstraints.forEach(
          c -> map.put(c.getEventStates().stream().findFirst().orElseThrow(), c));
      return Stream.of(Arguments.of(t, map, List.of()));
    }
    return conditions.stream()
        .map(
            conditionStates -> {
              Map<NodeState, ProbabilityConstraint> map = new HashMap<>();
              eventStateConstraints.stream()
                  .filter(c -> c.getConditionStates().equals(new HashSet<>(conditionStates)))
                  .forEach(
                      constraint ->
                          map.put(
                              constraint.getEventStates().stream().findFirst().orElseThrow(),
                              constraint));
              return map.isEmpty()
                  ? Optional.empty()
                  : Optional.of(Arguments.of(t, map, conditionStates));
            })
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(Arguments.class::cast);
  }

  static Stream<Arguments> provideGetProbArgs() {
    return provideSolvedArgs(TableHelperTest::buildGetProbArgs, t -> true);
  }

  static Stream<Arguments> buildGetProbArgs(NetworkTable table, BayesianNetwork network) {
    Set<Node> eventNodes = table.getEvents();
    Set<Node> conditionNodes = table.getConditions();
    return network.getNetworkData().getConstraints().stream()
        .filter(c -> c.getEventNodes().equals(eventNodes))
        .filter(c -> c.getEventStates().size() == 1)
        .filter(c -> c.getConditionNodes().equals(conditionNodes))
        .map(c -> Arguments.of(table, c));
  }

  @ParameterizedTest
  @MethodSource("provideGetProbArgs")
  void getProbability_shouldSucceed(NetworkTable table, ProbabilityConstraint constraint) {
    Set<NodeState> allStates = constraint.getAllStates();
    TableHelper<?> helper = table.getHelper();
    assertEquals(constraint.getProbability(), helper.getProbability(allStates), DOUBLE_EQUALITY);
  }

  @ParameterizedTest
  @MethodSource("provideGetProbArgs")
  void getProbabilityFromIDs_shouldSucceed(NetworkTable table, ProbabilityConstraint constraint) {
    List<Serializable> stateIds = NodeUtils.getNodeStateIds(constraint.getAllStates());
    TableHelper<?> helper = table.getHelper();
    assertEquals(
        constraint.getProbability(), helper.getProbabilityFromIDs(stateIds), DOUBLE_EQUALITY);
  }

  @ParameterizedTest
  @MethodSource("provideCopyTableArgs")
  void copyTable(NetworkTable networkTable) {
    NetworkTableHelper<?> helper = networkTable.getHelper();
    NetworkTable copied = helper.copyTable();
    assertNotSame(copied, networkTable);
    assertEquals(copied, networkTable);
  }

  @ParameterizedTest
  @MethodSource("provideNormalizeTableArgs")
  void normalizeTable(NetworkTable networkTable) {
    TableHelper<?> helper = networkTable.getHelper();
    double[] probs = networkTable.getVector().getProbabilities();
    Arrays.fill(probs, 1.0);
    helper.normalizeTable();
    int numOfConditionCombos =
        networkTable.getConditions().stream()
            .mapToInt(c -> c.getNodeStates().size())
            .reduce((x, y) -> x * y)
            .orElse(1);
    assertEquals(numOfConditionCombos, Arrays.stream(probs).sum(), DOUBLE_EQUALITY);
  }

  @ParameterizedTest
  @MethodSource("provideGetConditionProbArgs")
  void getConditionalProb(
      NetworkTable networkTable,
      Map<NodeState, ProbabilityConstraint> constraintMap,
      Collection<NodeState> conditionStates) {
    NetworkTableHelper<?> helper = networkTable.getHelper();
    Map<NodeState, Double> probs = helper.getConditionalProb(conditionStates);
    constraintMap.forEach(
        (event, constraint) ->
            assertEquals(constraint.getProbability(), probs.get(event), DOUBLE_EQUALITY));
  }

  @ParameterizedTest
  @MethodSource("provideGetConditionProbArgs")
  void getConditionalProbByIds(
      NetworkTable networkTable,
      Map<NodeState, ProbabilityConstraint> constraintMap,
      Collection<NodeState> conditionStates) {
    NetworkTableHelper<?> helper = networkTable.getHelper();
    Map<NodeState, Double> probs =
        helper.getConditionalProbByIds(NodeUtils.getNodeStateIds(conditionStates));
    constraintMap.forEach(
        (event, constraint) ->
            assertEquals(constraint.getProbability(), probs.get(event), DOUBLE_EQUALITY));
  }

  @ParameterizedTest
  @MethodSource("provideSafeModeArgs")
  void setSafeMode(NetworkTable networkTable) {
    NetworkTableHelper<?> helper = networkTable.getHelper();
    Node networkNode = networkTable.getNetworkNode();
    double[] probs = networkTable.getVector().getProbabilities();
    List<NodeState> eventStates = networkNode.getNodeStates();
    eventStates.forEach(
        state -> {
          assertTrue(helper.getConditionalProb(List.of(state)).isEmpty());
          assertNull(helper.getProbability(List.of(state)));
        });
    helper.setSafeMode(false);
    IntStream.range(0, eventStates.size())
        .forEach(
            i -> {
              NodeState state = eventStates.get(i);
              assertEquals(probs[i], helper.getProbability(List.of(state)), DOUBLE_EQUALITY);
            });
  }

  @ParameterizedTest
  @MethodSource("provideProbabilitySetMapTables")
  void buildProbabilitySetMap(ConditionalTable conditionalTable) {
    ConditionalTableHelper helper = conditionalTable.getHelper();
    Map<Set<NodeState>, Double> map = helper.buildProbabilitySetMap();
    double[] probs = conditionalTable.getVector().getProbabilities();
    int index = 0;
    for (Double p : map.values()) {
      assertEquals(probs[index], p, DOUBLE_EQUALITY);
      index++;
    }
  }
}
