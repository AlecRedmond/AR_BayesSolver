package io.github.alecredmond.internal.serialization.mapper;

import static io.github.alecredmond.method.network.NetworkScenarios.*;
import static org.junit.jupiter.api.Assertions.*;

import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.internal.serialization.structure.network.BayesianNetworkDataSTO;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SerializationMapperTest {
  SerializationMapper test = new SerializationMapper();

  static Stream<Arguments> provideNetworks() {
    return Stream.of(AH_NETWORK, SIMPLE_LINEAR, RAIN_NETWORK)
        .map(Supplier::get)
        .map(BayesianNetwork::solveNetwork)
        .map(BayesianNetwork::observeMarginals)
        .map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource("provideNetworks")
  void testSerializes_shouldNotThrow(BayesianNetwork network) {
    assertDoesNotThrow(
        () -> {
          test.serialize(network.getNetworkData());
        });
  }

  @ParameterizedTest
  @MethodSource("provideNetworks")
  void testDeSerializes_shouldNotThrow(BayesianNetwork network) {
    assertDoesNotThrow(
        () -> {
          BayesianNetworkDataSTO sto = test.serialize(network.getNetworkData());
          test.deSerialize(sto);
        });
  }

  @ParameterizedTest
  @MethodSource("provideNetworks")
  void testNetworksAreEqual_shouldReturnTrue(BayesianNetwork network) {
    BayesianNetworkData before = network.getNetworkData();
    BayesianNetworkData after = test.deSerialize(test.serialize(before));
    assertEquals(before, after);
  }
}
