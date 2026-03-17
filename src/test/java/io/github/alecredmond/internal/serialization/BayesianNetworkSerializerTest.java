package io.github.alecredmond.internal.serialization;

import static io.github.alecredmond.method.network.NetworkScenarios.*;
import static org.junit.jupiter.api.Assertions.*;

import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.export.serialization.network.SerializedBayesNetData;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BayesianNetworkSerializerTest {
  static final boolean DEBUG_SOLVE_LENGTHY_TESTS = false;
  static List<Supplier<BayesianNetwork>> networkSuppliers;
  BayesianNetworkSerializer test = new BayesianNetworkSerializer();

  static Stream<Arguments> provideNetworks() {
    return networkSuppliers.stream()
        .map(Supplier::get)
        .map(BayesianNetwork::solveNetwork)
        .map(Arguments::of);
  }

  @BeforeAll
  static void init() {
    networkSuppliers = new ArrayList<>();
    networkSuppliers.add(AH_NETWORK);
    networkSuppliers.add(SIMPLE_LINEAR);
    networkSuppliers.add(RAIN_NETWORK);
    if (!DEBUG_SOLVE_LENGTHY_TESTS) return;
    networkSuppliers.add(FANTASY_GRAPH);
  }

  @ParameterizedTest
  @MethodSource("provideNetworks")
  void testSerializes_shouldNotThrow(BayesianNetwork network) {
    assertDoesNotThrow(
        () -> {
          test.serialize(network);
        });
  }

  @ParameterizedTest
  @MethodSource("provideNetworks")
  void testDeSerializes_shouldNotThrow(BayesianNetwork network) {
    assertDoesNotThrow(
        () -> {
          SerializedBayesNetData sto = test.serialize(network);
          test.deSerialize(sto);
        });
  }

  @ParameterizedTest
  @MethodSource("provideNetworks")
  void testNetworksAreEqual_shouldReturnTrue(BayesianNetwork network) {
    BayesianNetworkData before = network.getNetworkData();
    BayesianNetworkData after = test.deSerialize(test.serialize(network)).getNetworkData();
    assertEquals(before, after);
  }
}
