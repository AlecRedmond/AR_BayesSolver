package com.artools.method.network;

import static org.junit.jupiter.api.Assertions.*;

import com.artools.application.node.Node;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;

class BayesianNetworkTest {
  BayesianNetwork test;

  @Test
  void newNetwork() {
    assertDoesNotThrow(() -> test = BayesianNetwork.newNetwork());
  }

  @Test
  void testNewNetwork() {
    assertDoesNotThrow(() -> test = BayesianNetwork.newNetwork("test"));
  }

  @Test
  void addNode() {
    test = BayesianNetwork.newNetwork("test").addNode("NODE");
    Node node = test.getNetworkData().getNodeIDsMap().values().stream().findAny().orElseThrow();
    assertEquals("NODE", node.toString());
    assertThrows(IllegalArgumentException.class, () -> test.addNode("NODE"));
  }

  @Test
  void testAddNode() {
    test = BayesianNetwork.newNetwork("test").addNode("NODE", List.of("NODE:TRUE", "NODE:FALSE"));
    Node node = test.getNetworkData().getNodeIDsMap().values().stream().findAny().orElseThrow();
    assertEquals("NODE", node.toString());
    assertThrows(
        IllegalArgumentException.class,
        () -> test.addNode("OTHER", List.of("NODE:TRUE", "NODE:FALSE")));
  }

  @Test
  void removeNode() {
    test = BayesianNetwork.newNetwork("test").addNode("NODE");
    assertDoesNotThrow(() -> test.removeNode("NODE"));
    assertTrue(test.getNetworkData().getNodeIDsMap().values().isEmpty());
  }

  @Test
  void removeAllNodes() {
    test = BayesianNetwork.newNetwork("test").addNode("NODE").addNode("NODE_TWO").removeAllNodes();
    assertTrue(test.getNetworkData().getNodeIDsMap().values().isEmpty());
  }

  @Test
  void testSolves() {
    test = BayesianNetwork.newNetwork("RAIN_SPRINKLER_GRASS");

    assertDoesNotThrow(
        () ->
            test.addNode("RAIN", List.of("RAIN:TRUE", "RAIN:FALSE"))
                .addNode("SPRINKLER", List.of("SPRINKLER:TRUE", "SPRINKLER:FALSE"))
                .addNode("WET_GRASS", List.of("WET_GRASS:TRUE", "WET_GRASS:FALSE"))
                .addParent("SPRINKLER", "RAIN")
                .addParents("WET_GRASS", List.of("SPRINKLER", "RAIN"))
                .addConstraint("RAIN:TRUE", 0.2)
                .addConstraint("SPRINKLER:TRUE", List.of("RAIN:TRUE"), 0.01)
                .addConstraint("SPRINKLER:TRUE", List.of("RAIN:FALSE"), 0.4)
                .addConstraint("WET_GRASS:TRUE", List.of("RAIN:TRUE", "SPRINKLER:TRUE"), 0.99)
                .addConstraint("WET_GRASS:TRUE", List.of("RAIN:FALSE", "SPRINKLER:TRUE"), 0.9)
                .addConstraint("WET_GRASS:TRUE", List.of("RAIN:FALSE", "SPRINKLER:FALSE"), 0.0)
                .addConstraint("WET_GRASS:TRUE", List.of("RAIN:TRUE", "SPRINKLER:FALSE"), 0.9)
                .solveNetwork()
                .printNetwork(true)
                .observeMarginals()
                .printObserved(true)
                .observeNetwork(List.of("WET_GRASS:TRUE"))
                .printObserved(true));

    int numOfSamples = 1_000_000;
    String testState = "RAIN:TRUE";
    String includedNode = "RAIN";

    generateSamples(numOfSamples, includedNode, testState, false);
  }

  private List<List<String>> generateSamples(
      int numOfSamples,
      String includedNode,
      String testState,
      boolean printAllToConsole) {

    double expected = test.observeProbability(List.of(testState)) * numOfSamples;
    double delta = Math.sqrt(numOfSamples);
    long lowerBound = (long) (expected - delta);
    long upperBound = (long) (expected + delta);

    List<List<String>> samples =
        test.generateSamples(
            numOfSamples, List.of(), List.of(includedNode), String.class);

    long count = samples.stream().flatMap(Collection::stream).filter(testState::equals).count();

    if (printAllToConsole) getLines(samples).forEach(System.out::println);

    System.out.println(count);

    assertTrue(count >= lowerBound && count <= upperBound);

    return samples;
  }

  private List<String> getLines(List<List<String>> samples) {
    List<String> lines = new ArrayList<>();
    for (List<String> sample : samples) {
      StringBuilder sb = new StringBuilder();
      for (String s : sample) {
        sb.append(s).append(", ");
      }

      lines.add(sb.toString());
    }
    return lines;
  }

  @Test
  void testNetworkAH() {
    // aa
    assertDoesNotThrow(
        () ->
            test =
                BayesianNetwork.newNetwork("A_TO_H")
                    .addNode("A", List.of("A+", "A-"))
                    .addNode("B", List.of("B+", "B-"))
                    .addNode("C", List.of("C+", "C-"))
                    .addNode("D", List.of("D+", "D-"))
                    .addNode("E", List.of("E+", "E-"))
                    .addNode("F", List.of("F+", "F-"))
                    .addNode("G", List.of("G+", "G-"))
                    .addNode("H", List.of("H+", "H-"))
                    .addParents("D", List.of("A", "B"))
                    .addParents("E", List.of("B", "C"))
                    .addParents("F", List.of("D"))
                    .addParents("G", List.of("D", "E"))
                    .addParents("H", List.of("E"))
                    .addConstraint("D+", List.of("A+"), 0.33)
                    .addConstraint("D-", List.of("B+"), 0.57)
                    .addConstraint("E+", List.of("B+"), 0.61)
                    .addConstraint("E-", List.of("C+"), 0.37)
                    .addConstraint("F+", List.of("D+"), 0.26)
                    .addConstraint("F-", List.of("D-"), 0.92)
                    .addConstraint("G+", List.of("D+"), 0.87)
                    .addConstraint("G-", List.of("E+"), 0.50)
                    .addConstraint("H+", List.of("E+"), 0.43)
                    .addConstraint("H-", List.of("E-"), 0.18)
                    .addConstraint("A+", List.of("H+"), 0.25)
                    .solveNetwork()
                    .printNetwork(false)
                    .observeMarginals()
                    .printObserved(false));
  }

  @Test
  void testFantasyGraph() {
    test = BayesianNetwork.newNetwork("FANTASY_ELECTION");

    assertDoesNotThrow(
        () ->
            test.addNode(
                    "DISTRICT_TYPE",
                    List.of(
                        "DISTRICT_TYPE:URBAN",
                        "DISTRICT_TYPE:SUBURBAN",
                        "DISTRICT_TYPE:RURAL",
                        "DISTRICT_TYPE:FRONTIER"))
                .addNode(
                    "DISTRICT",
                    List.of(
                        "DISTRICT:CAPITAL_CITY",
                        "DISTRICT:CITY_SUBURBS",
                        "DISTRICT:FARM_TOWN",
                        "DISTRICT:MINING_OUTPOST",
                        "DISTRICT:OTHER"))
                .addNode(
                    "RACE",
                    List.of(
                        "RACE:HUMAN",
                        "RACE:ANK",
                        "RACE:ORC",
                        "RACE:GOBLIN",
                        "RACE:DWARF",
                        "RACE:ELF"))
                .addNode(
                    "AGE", List.of("AGE:CHILD", "AGE:YOUNG_ADULT", "AGE:MIDDLE_AGE", "AGE:ELDERLY"))
                .addNode(
                    "WEALTH",
                    List.of(
                        "WEALTH:MARGINAL",
                        "WEALTH:LOW",
                        "WEALTH:MIDDLE",
                        "WEALTH:HIGH",
                        "WEALTH:ULTRA"))
                .addNode(
                    "OUTLOOK",
                    List.of(
                        "OUTLOOK:REVOLUTIONARY",
                        "OUTLOOK:PROGRESSIVE",
                        "OUTLOOK:MODERATE",
                        "OUTLOOK:CONSERVATIVE",
                        "OUTLOOK:REACTIONARY",
                        "OUTLOOK:APATHY"))
                .addNode(
                    "VOTE",
                    List.of(
                        "VOTE:SDP",
                        "VOTE:VNG",
                        "VOTE:CPK",
                        "VOTE:UNF",
                        "VOTE:SND",
                        "VOTE:FPK",
                        "VOTE:KNC",
                        "VOTE:CSD",
                        "VOTE:OTH",
                        "VOTE:NONE"))
                .addParents("DISTRICT_TYPE", List.of())
                .addParents("DISTRICT", List.of("DISTRICT_TYPE"))
                .addParents("RACE", List.of("DISTRICT"))
                .addParents("AGE", List.of("RACE", "DISTRICT"))
                .addParents("WEALTH", List.of("RACE", "DISTRICT"))
                .addParents("OUTLOOK", List.of("WEALTH", "AGE", "DISTRICT"))
                .addParents("VOTE", List.of("RACE", "AGE", "DISTRICT", "OUTLOOK"))
                // Known marginals
                // DISTRICT
                .addConstraint("DISTRICT:CAPITAL_CITY", 600E3 / 320E6)
                .addConstraint("DISTRICT:CITY_SUBURBS", 600E3 / 320E6)
                .addConstraint("DISTRICT:FARM_TOWN", 600E3 / 320E6)
                .addConstraint("DISTRICT:MINING_OUTPOST", 600E3 / 320E6)
                // RACE
                .addConstraint("RACE:HUMAN", 0.65)
                .addConstraint("RACE:ANK", 0.14)
                .addConstraint("RACE:ORC", 0.07)
                .addConstraint("RACE:GOBLIN", 0.06)
                .addConstraint("RACE:DWARF", 0.05)
                .addConstraint("RACE:ELF", 0.03)
                // VOTE
                .addConstraint("VOTE:SDP", 0.1090)
                .addConstraint("VOTE:VNG", 0.0941)
                .addConstraint("VOTE:CPK", 0.0834)
                .addConstraint("VOTE:UNF", 0.0685)
                .addConstraint("VOTE:SND", 0.0214)
                .addConstraint("VOTE:FPK", 0.0173)
                .addConstraint("VOTE:KNC", 0.0066)
                .addConstraint("VOTE:CSD", 0.0057)
                .addConstraint("VOTE:OTH", 0.0066)
                // WEALTH
                .addConstraint("WEALTH:MARGINAL", 0.287)
                .addConstraint("WEALTH:LOW", 0.325)
                .addConstraint("WEALTH:HIGH", 0.12)
                .addConstraint("WEALTH:ULTRA", 0.001)
                // AGE
                .addConstraint("AGE:CHILD", 0.388)
                .addConstraint("AGE:YOUNG_ADULT", 0.318)
                .addConstraint("AGE:MIDDLE_AGE", 0.209)
                .addConstraint("AGE:ELDERLY", 0.085)
                // DISTRICT_TYPE
                .addConstraint("DISTRICT_TYPE:URBAN", 0.35)
                .addConstraint("DISTRICT_TYPE:SUBURBAN", 0.25)
                .addConstraint("DISTRICT_TYPE:RURAL", 0.30)
                .addConstraint("DISTRICT_TYPE:FRONTIER", 0.10)
                // ---
                // CONDITIONALS
                // DISTRICT_TYPE | DISTRICT
                .addConstraint("DISTRICT_TYPE:URBAN", List.of("DISTRICT:CAPITAL_CITY"), 1.0)
                .addConstraint("DISTRICT_TYPE:SUBURBAN", List.of("DISTRICT:CITY_SUBURBS"), 1.0)
                .addConstraint("DISTRICT_TYPE:RURAL", List.of("DISTRICT:FARM_TOWN"), 1.0)
                .addConstraint("DISTRICT_TYPE:FRONTIER", List.of("DISTRICT:MINING_OUTPOST"), 1.0)
                // Age|Race
                .addConstraint("AGE:MIDDLE_AGE", List.of("RACE:ORC"), 0.177)
                .addConstraint("AGE:YOUNG_ADULT", List.of("RACE:ANK"), 0.335)
                .addConstraint("AGE:CHILD", List.of("RACE:DWARF"), 0.303)
                .addConstraint("AGE:CHILD", List.of("RACE:GOBLIN"), 0.410)
                .addConstraint("AGE:ELDERLY", List.of("RACE:ELF"), 0.13)
                .addConstraint("AGE:MIDDLE_AGE", List.of("RACE:ELF"), 0.23)
                // Race|DISTRICT_TYPE
                .addConstraint("RACE:HUMAN", List.of("DISTRICT_TYPE:URBAN"), 0.98 * 0.65)
                .addConstraint("RACE:HUMAN", List.of("DISTRICT_TYPE:SUBURBAN"), 1.05 * 0.65)
                .addConstraint("RACE:HUMAN", List.of("DISTRICT_TYPE:RURAL"), 1.03 * 0.65)
                .addConstraint("RACE:ANK", List.of("DISTRICT_TYPE:URBAN"), 1.08 * 0.14)
                .addConstraint("RACE:ANK", List.of("DISTRICT_TYPE:SUBURBAN"), 0.78 * 0.14)
                .addConstraint("RACE:ANK", List.of("DISTRICT_TYPE:RURAL"), 0.92 * 0.14)
                .addConstraint("RACE:ORC", List.of("DISTRICT_TYPE:URBAN"), 1.05 * 0.07)
                .addConstraint("RACE:ORC", List.of("DISTRICT_TYPE:SUBURBAN"), 0.73 * 0.07)
                .addConstraint("RACE:ORC", List.of("DISTRICT_TYPE:RURAL"), 1.01 * 0.07)
                .addConstraint("RACE:GOBLIN", List.of("DISTRICT_TYPE:URBAN"), 1.03 * 0.06)
                .addConstraint("RACE:GOBLIN", List.of("DISTRICT_TYPE:SUBURBAN"), 0.70 * 0.06)
                .addConstraint("RACE:GOBLIN", List.of("DISTRICT_TYPE:RURAL"), 0.92 * 0.06)
                .addConstraint("RACE:DWARF", List.of("DISTRICT_TYPE:URBAN"), 1.08 * 0.05)
                .addConstraint("RACE:DWARF", List.of("DISTRICT_TYPE:SUBURBAN"), 1.01 * 0.05)
                .addConstraint("RACE:DWARF", List.of("DISTRICT_TYPE:FRONTIER"), 1.1 * 0.05)
                .addConstraint("RACE:ELF", List.of("DISTRICT_TYPE:URBAN"), 0.98 * 0.03)
                .addConstraint("RACE:ELF", List.of("DISTRICT_TYPE:SUBURBAN"), 1.03 * 0.03)
                .addConstraint("RACE:ELF", List.of("DISTRICT_TYPE:RURAL"), 1.02 * 0.03)

                // Wealth|Race,DISTRICT_TYPE
                .addConstraint(
                    "WEALTH:MARGINAL", List.of("RACE:HUMAN", "DISTRICT_TYPE:URBAN"), 0.25)
                .addConstraint("WEALTH:LOW", List.of("RACE:HUMAN", "DISTRICT_TYPE:URBAN"), 0.36)
                .addConstraint("WEALTH:HIGH", List.of("RACE:HUMAN", "DISTRICT_TYPE:URBAN"), 0.13)
                .addConstraint("WEALTH:ULTRA", List.of("RACE:HUMAN", "DISTRICT_TYPE:URBAN"), 0.005)
                .addConstraint(
                    "WEALTH:MARGINAL", List.of("RACE:HUMAN", "DISTRICT_TYPE:RURAL"), 0.23)
                .addConstraint("WEALTH:ULTRA", List.of("RACE:HUMAN", "DISTRICT_TYPE:RURAL"), 0.001)
                .addConstraint(
                    "WEALTH:MARGINAL", List.of("RACE:HUMAN", "DISTRICT_TYPE:SUBURBAN"), 0.10)
                .addConstraint(
                    "WEALTH:ULTRA", List.of("RACE:HUMAN", "DISTRICT_TYPE:SUBURBAN"), 0.003)
                .addConstraint(
                    "WEALTH:MARGINAL", List.of("RACE:HUMAN", "DISTRICT_TYPE:FRONTIER"), 0.29)
                .addConstraint(
                    "WEALTH:ULTRA", List.of("RACE:HUMAN", "DISTRICT_TYPE:FRONTIER"), 0.000)
                // Outlook|Race,Wealth,Age,DISTRICT_TYPE
                .addConstraint(
                    "OUTLOOK:REACTIONARY",
                    List.of(
                        "RACE:HUMAN", "WEALTH:MIDDLE", "AGE:YOUNG_ADULT", "DISTRICT_TYPE:SUBURBAN"),
                    0.42)
                .addConstraint(
                    "OUTLOOK:REVOLUTIONARY",
                    List.of(
                        "RACE:ANK", "WEALTH:MARGINAL", "AGE:YOUNG_ADULT", "DISTRICT_TYPE:URBAN"),
                    0.66)
                .addConstraint(
                    "OUTLOOK:CONSERVATIVE",
                    List.of("RACE:HUMAN", "WEALTH:LOW", "AGE:MIDDLE_AGE", "DISTRICT_TYPE:RURAL"),
                    0.387)
                .addConstraint(
                    "OUTLOOK:PROGRESSIVE",
                    List.of(
                        "RACE:DWARF", "WEALTH:MIDDLE", "AGE:YOUNG_ADULT", "DISTRICT_TYPE:URBAN"),
                    0.42)
                .addConstraint(
                    "OUTLOOK:MODERATE",
                    List.of(
                        "RACE:ELF", "WEALTH:MIDDLE", "AGE:MIDDLE_AGE", "DISTRICT_TYPE:SUBURBAN"),
                    0.41)
                // NON-LOCAL CONDITIONALS
                // VOTE | DISTRICT, OUTLOOK
                .addConstraint(
                    "VOTE:CPK", List.of("OUTLOOK:REVOLUTIONARY", "DISTRICT:CAPITAL_CITY"), 0.8)
                .addConstraint(
                    "VOTE:CPK", List.of("OUTLOOK:REVOLUTIONARY", "DISTRICT:FARM_TOWN"), 0.62)
                .addConstraint(
                    "VOTE:FPK", List.of("OUTLOOK:CONSERVATIVE", "DISTRICT:CAPITAL_CITY"), 0.25)
                .addConstraint(
                    "VOTE:FPK", List.of("OUTLOOK:CONSERVATIVE", "DISTRICT:FARM_TOWN"), 0.05)
                .addConstraint(
                    "VOTE:VNG", List.of("OUTLOOK:CONSERVATIVE", "DISTRICT:CAPITAL_CITY"), 0.21)
                .addConstraint(
                    "VOTE:VNG", List.of("OUTLOOK:CONSERVATIVE", "DISTRICT:FARM_TOWN"), 0.45)
                .addConstraint(
                    "VOTE:SDP", List.of("OUTLOOK:PROGRESSIVE", "DISTRICT:CAPITAL_CITY"), 0.51)
                .addConstraint(
                    "VOTE:SDP", List.of("OUTLOOK:PROGRESSIVE", "DISTRICT:FARM_TOWN"), 0.28)
                // DISTRICT
                .addConstraint("OUTLOOK:REVOLUTIONARY", List.of("DISTRICT:CITY_SUBURBS"), 0.05)
                .addConstraint("OUTLOOK:REACTIONARY", List.of("DISTRICT:CITY_SUBURBS"), 0.258)
                .addConstraint("AGE:CHILD", List.of("DISTRICT:CITY_SUBURBS"), 0.345)
                .addConstraint("OUTLOOK:REACTIONARY", List.of("DISTRICT:CAPITAL_CITY"), 0.1)
                .addConstraint("OUTLOOK:REVOLUTIONARY", List.of("DISTRICT:CAPITAL_CITY"), 0.275)
                .addConstraint("OUTLOOK:CONSERVATIVE", List.of("DISTRICT:FARM_TOWN"), 0.300)
                .addConstraint("OUTLOOK:REVOLUTIONARY", List.of("DISTRICT:FARM_TOWN"), 0.08)
                .addConstraint("OUTLOOK:REVOLUTIONARY", List.of("DISTRICT:MINING_OUTPOST"), 0.15)
                // OTHERS
                .addConstraint("VOTE:NONE", List.of("AGE:CHILD"), 1.0)
                .addConstraint("OUTLOOK:APATHY", List.of("VOTE:NONE"), 0.75)
                .addConstraint("OUTLOOK:REVOLUTIONARY", List.of("WEALTH:MARGINAL"), 0.44)
                .addConstraint("OUTLOOK:REVOLUTIONARY", List.of("WEALTH:ULTRA"), 0.02)
                .addConstraint("OUTLOOK:REVOLUTIONARY", List.of("WEALTH:HIGH"), 0.05)
                .addConstraint("OUTLOOK:REVOLUTIONARY", List.of("VOTE:CPK"), 0.65)
                .addConstraint("OUTLOOK:REVOLUTIONARY", List.of("VOTE:SND"), 0.55)
                .addConstraint("OUTLOOK:REVOLUTIONARY", List.of("VOTE:SDP"), 0.20)
                .addConstraint("OUTLOOK:PROGRESSIVE", List.of("RACE:DWARF"), 0.46)
                .addConstraint("OUTLOOK:PROGRESSIVE", List.of("VOTE:SDP"), 0.48)
                .addConstraint("OUTLOOK:PROGRESSIVE", List.of("VOTE:CPK"), 0.20)
                .addConstraint("OUTLOOK:MODERATE", List.of("VOTE:VNG"), 0.20)
                .addConstraint("OUTLOOK:MODERATE", List.of("VOTE:FPK"), 0.41)
                .addConstraint("OUTLOOK:MODERATE", List.of("VOTE:SDP"), 0.25)
                .addConstraint("OUTLOOK:CONSERVATIVE", List.of("WEALTH:HIGH"), 0.48)
                .addConstraint("OUTLOOK:CONSERVATIVE", List.of("VOTE:FPK"), 0.55)
                .addConstraint("OUTLOOK:CONSERVATIVE", List.of("VOTE:VNG"), 0.42)
                .addConstraint("OUTLOOK:CONSERVATIVE", List.of("VOTE:UNF"), 0.35)
                .addConstraint("OUTLOOK:REACTIONARY", List.of("VOTE:UNF"), 0.50)
                .addConstraint("OUTLOOK:REACTIONARY", List.of("VOTE:CSD"), 0.44)
                .addConstraint("VOTE:UNF", List.of("RACE:ANK"), 0.02)
                .addConstraint("VOTE:UNF", List.of("RACE:DWARF"), 0.02)
                .addConstraint("VOTE:UNF", List.of("RACE:GOBLIN"), 0.03)
                .addConstraint("VOTE:UNF", List.of("RACE:ORC"), 0.08)
                .addConstraint("RACE:ORC", List.of("VOTE:KNC"), 0.59)
                .addConstraint("WEALTH:ULTRA", List.of("RACE:ANK"), 0.0005)
                .addConstraint("WEALTH:HIGH", List.of("RACE:ANK"), 0.008)
                .addConstraint("WEALTH:MIDDLE", List.of("RACE:ANK"), 0.0726)
                .addConstraint("WEALTH:ULTRA", List.of("RACE:ORC"), 0.0005)
                .addConstraint("WEALTH:HIGH", List.of("RACE:ORC"), 0.008)
                .addConstraint("WEALTH:MIDDLE", List.of("RACE:ORC"), 0.0726)
                .addConstraint("WEALTH:ULTRA", List.of("RACE:GOBLIN"), 0.0005)
                .addConstraint("WEALTH:HIGH", List.of("RACE:GOBLIN"), 0.008)
                .addConstraint("WEALTH:MIDDLE", List.of("RACE:GOBLIN"), 0.0726)
                .solveNetwork()
                .observeMarginals()
                .printObserved(false)
                .observeNetwork(List.of("DISTRICT:CAPITAL_CITY"))
                .observeNetwork(List.of("RACE:ANK", "AGE:YOUNG_ADULT"))
                .printObserved(false));

    int numOfSamples = 1_000_000;
    String testState = "VOTE:CPK";
    String includedNode = "VOTE";

    generateSamples(numOfSamples, includedNode, testState, false);
  }
}
