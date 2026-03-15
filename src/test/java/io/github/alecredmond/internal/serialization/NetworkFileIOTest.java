package io.github.alecredmond.internal.serialization;

import static io.github.alecredmond.internal.method.utils.AppProperty.*;
import static io.github.alecredmond.method.network.NetworkScenarios.*;
import static io.github.alecredmond.method.network.NetworkScenarios.FANTASY_GRAPH;
import static org.junit.jupiter.api.Assertions.*;

import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.internal.method.utils.PropertiesLoader;
import io.github.alecredmond.internal.serialization.mapper.SerializationMapper;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
class NetworkFileIOTest {
  static final boolean SKIP_SAVE_TESTS = true;
  static final boolean SKIP_J_FILE_CHOOSER = true;
  static final boolean DEBUG_SOLVE_LENGTHY_TESTS = false;
  static final PropertiesLoader LOADER = new PropertiesLoader();
  static final String DIRECTORY = LOADER.loadDirectory(DIRECTORY_ROOT, DIRECTORY_SAVE);
  static final String EXTENSION = LOADER.loadString(EXTENSION_FILE_TYPE);
  static List<Supplier<BayesianNetwork>> networkSuppliers;
  NetworkFileIO test = new NetworkFileIO(new SerializationMapper());

  static Stream<Arguments> provideNetworks() {
    return networkSuppliers.stream()
        .map(Supplier::get)
        .map(BayesianNetwork::solveNetwork)
        .map(BayesianNetwork::observeMarginals)
        .map(Arguments::of);
  }

  @BeforeAll
  static void init() {
    networkSuppliers = new ArrayList<>();
    networkSuppliers.add(RAIN_NETWORK);
    networkSuppliers.add(AH_NETWORK);
    networkSuppliers.add(SIMPLE_LINEAR);
    if (!DEBUG_SOLVE_LENGTHY_TESTS) return;
    networkSuppliers.add(FANTASY_GRAPH);
  }

  @ParameterizedTest
  @MethodSource("provideNetworks")
  void saveNetworkFileChooser(BayesianNetwork network) {
    if (SKIP_J_FILE_CHOOSER || SKIP_SAVE_TESTS) {
      assertTrue(true);
      return;
    }
    log.info("SAVE {}", network.getNetworkData().getNetworkName());
    assertTrue(test.saveNetwork(network));
  }

  @ParameterizedTest
  @MethodSource("provideNetworks")
  void loadNetworkFileChooser(BayesianNetwork network) {
    if (SKIP_J_FILE_CHOOSER || SKIP_SAVE_TESTS) {
      assertTrue(true);
      return;
    }
    log.info("LOAD NETWORK : {}", network.getNetworkData().getNetworkName());
    BayesianNetwork newNet = test.loadNetwork();
    assertEquals(network.getNetworkData(), newNet.getNetworkData());
  }

  @ParameterizedTest
  @MethodSource("provideNetworks")
  void saveNetwork(BayesianNetwork network) {
    if (SKIP_SAVE_TESTS) {
      assertTrue(true);
      return;
    }
    String netName = network.getNetworkData().getNetworkName();
    String totalPath = DIRECTORY + netName;
    assertTrue(test.saveNetwork(network, totalPath));
    assertTrue(test.saveNetwork(network, new File(totalPath)));
  }

  @ParameterizedTest
  @MethodSource("provideNetworks")
  void loadNetwork(BayesianNetwork network) {
      if (SKIP_SAVE_TESTS) {
          assertTrue(true);
          return;
      }
    String netName = network.getNetworkData().getNetworkName();
    String totalPath = DIRECTORY + netName + EXTENSION;
    BayesianNetwork loaded = test.loadNetwork(totalPath);
    assertEquals(network.getNetworkData(), loaded.getNetworkData());
  }
}
