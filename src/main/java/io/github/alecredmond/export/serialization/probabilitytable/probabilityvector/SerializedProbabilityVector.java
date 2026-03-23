package io.github.alecredmond.export.serialization.probabilitytable.probabilityvector;

import java.io.Serializable;
import lombok.Data;

@Data
public class SerializedProbabilityVector implements Serializable {
  private Serializable[] nodeIdArray;
  private int[] numberOfStates;
  private int[] stepMultiplier;
  private double[] probabilities;
}
