package io.github.alecredmond.internal.method.probabilitytables;

import io.github.alecredmond.export.application.probabilitytables.ConditionalTable;
import io.github.alecredmond.export.application.probabilitytables.MarginalTable;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.application.probabilitytables.JunctionTreeTable;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import org.apache.commons.lang3.SerializationUtils;

public class TableCopier {

  public ProbabilityTable copyTable(ProbabilityTable table) {
    return switch (table) {
      case MarginalTable mt -> copyMarginal(mt);
      case ConditionalTable ct -> copyConditional(ct);
      case JunctionTreeTable jtt -> copyJTT(jtt);
      default -> throw new IllegalStateException("Unexpected value: " + table);
    };
  }

  public MarginalTable copyMarginal(MarginalTable mt) {
    return new MarginalTable(
        copyVector(mt.getVector()),
        SerializationUtils.clone(mt.getTableName()),
        mt.getNetworkNode(),
        Map.copyOf(mt.getNodeStateIDMap()),
        Map.copyOf(mt.getNodeIDMap()));
  }

  public ConditionalTable copyConditional(ConditionalTable ct) {
    return new ConditionalTable(
        SerializationUtils.clone(ct.getTableName()),
        copyVector(ct.getVector()),
        new LinkedHashSet<>(ct.getNodes()),
        new LinkedHashSet<>(ct.getEvents()),
        new LinkedHashSet<>(ct.getConditions()),
        ct.getNetworkNode(),
        Map.copyOf(ct.getNodeIDMap()),
        Map.copyOf(ct.getNodeStateIDMap()));
  }

  public JunctionTreeTable copyJTT(JunctionTreeTable jtt) {
    return new JunctionTreeTable(
        SerializationUtils.clone(jtt.getTableName()),
        copyVector(jtt.getVector()),
        new LinkedHashSet<>(jtt.getEvents()),
        copyVector(jtt.getBackupVector()),
        Map.copyOf(jtt.getNodeStateIDMap()),
        Map.copyOf(jtt.getNodeIDMap()));
  }

  private static <T> T[] copy(T[] array) {
    return Arrays.copyOf(array, array.length);
  }

  private static int[] copy(int[] array) {
    return Arrays.copyOf(array, array.length);
  }

  private static double[] copy(double[] array) {
    return Arrays.copyOf(array, array.length);
  }

  private ProbabilityVector copyVector(ProbabilityVector vector) {
    return new ProbabilityVector(
        copy(vector.getNodeArray()),
        copy(vector.getStateArrays()),
        copy(vector.getNumberOfStates()),
        copy(vector.getStepMultiplier()),
        copy(vector.getProbabilities()),
        Map.copyOf(vector.getNodeIndexMap()),
        Map.copyOf(vector.getStateValueMap()));
  }
}
