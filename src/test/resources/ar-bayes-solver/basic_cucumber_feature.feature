Feature: Adding nodes to the network
  In this feature nodes are appended to the network

  Background: I have several nodes in the network
    Given an empty network
    Then I add the following nodes:
      | A | A+ | A- |
      | B | B+ | B- |
      | C | C+ | C- |
      | D | D+ | D- |
    Then And define each node's parent structure:
      | B | A |
      | C | B |
      | D | C |


  Scenario Outline: Adding a new, stateless node to the network
    When adding a stateless node "<node_id>"
    Then a node with id "<node_id>" should be added
    But building the network will fail with a node state validation exception
    Examples:
      | node_id |
      | G       |

  Scenario Outline: Adding a new node with non-conflicting states, connected to the network
    Given I add the following node with states:
      | <node_id> | <state_id_1> | <state_id_2> |
    And after connecting "<node_id>" to its parent "<parent_node_id>"
    Then a node with id "<node_id>" should be added
    And all defined states should be included:
      | <state_id_1> |
      | <state_id_2> |
    And building the network will succeed
    Examples:
      | node_id | state_id_1 | state_id_2 | parent_node_id |
      | G       | G+         | G-         | D              |

  Scenario Outline: Adding a new node with non-conflicting states, connected to the network
    Given I add the following node with states:
      | <node_id> | <state_id_1> | <state_id_2> |
    And after connecting "<node_id>" to its parent "<parent_node_id>"
    Then a node with id "<node_id>" should be added
    And all defined states should be included:
      | <state_id_1> |
      | <state_id_2> |
    And building the network will succeed
    Examples:
      | node_id | state_id_1 | state_id_2 | parent_node_id |
      | G       | G+         | G-         | D              |


