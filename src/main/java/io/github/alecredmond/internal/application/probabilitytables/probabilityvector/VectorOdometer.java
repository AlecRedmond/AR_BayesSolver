package io.github.alecredmond.internal.application.probabilitytables.probabilityvector;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import lombok.Data;

@Data
public class VectorOdometer {
  private double[] probabilities;
  private Node[] nodeArray;
  private NodeState[][] stateArrays;
  private int[] numberOfStates;
  private int[] stepMultiplier;
  private int[] stateIndexes;
  private NodeState[] states;
  private boolean[] outerIteratorLocks; //IS EVENT - ITERATE CONDITIONS
  private boolean[] innerIteratorLocks; //IS CONDITION - ITERATE EVENTS
  private boolean[][] nodeStateEvidenceArray;
}
