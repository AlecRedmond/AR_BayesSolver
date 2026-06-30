package io.github.alecredmond.internal.method.inference.solver.cptmapper;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.exceptions.CptDirectMappingException;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.NetworkTable;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import io.github.alecredmond.internal.method.constraints.strategies.CPTConstraintValidator;
import io.github.alecredmond.internal.method.utils.DoublePrecision;
import io.github.alecredmond.internal.method.vectoriterator.VectorIterator;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.resetlogictypes.BaseOdometerResetLogic;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.updatelogictypes.StateUpdater;
import java.util.*;
import java.util.function.Predicate;

public abstract class CptMapperIterator<T extends NetworkTable, P extends ProbabilityConstraint>
    implements BaseOdometerResetLogic, StateUpdater {
  protected final T networkTable;
  protected final List<P> constraints;
  protected final VectorIterator<VectorOdometer> iterator;
  protected final CPTConstraintValidator<P> validator;

  protected CptMapperIterator(
      T networkTable, Collection<P> constraints, CPTConstraintValidator<P> validator) {
    this.networkTable = networkTable;
    this.validator = validator;
    this.constraints = radixSortConstraints(networkTable, constraints);
    this.iterator = new VectorIterator<>(new VectorOdometer(networkTable.getVector()), this);
  }

  private List<P> radixSortConstraints(T networkTable, Collection<P> constraints) {
    ProbabilityVector vector = networkTable.getVector();
    Map<NodeState, Integer> stateValueMap = vector.getStateValueMap();
    Comparator<P> comparator =
        Arrays.stream(vector.getNodeArray())
            .map(node -> compareByStateIndexInNode(node, stateValueMap))
            .reduce(Comparator::thenComparing)
            .orElseThrow();
    return constraints.stream().distinct().sorted(comparator).toList();
  }

  private Comparator<P> compareByStateIndexInNode(
      Node node, Map<NodeState, Integer> stateValueMap) {
    return Comparator.comparingInt(
        (P constraint) ->
            constraint.getAllStates().stream()
                .filter(state -> state.getNode().equals(node))
                .findAny()
                .map(stateValueMap::get)
                .orElseThrow());
  }

  public List<P> directMapCPTs() {
    MissingEntryCheck entryCheck = new MissingEntryCheck(constraints);
    Map<P, Integer> constraintIndexMap = new LinkedHashMap<>();
    List<P> addedConstraints = new ArrayList<>();
    iterator.iterateOuter(
        () -> {
          entryCheck.setNewRow();
          iterator.iterateInner((o, i) -> checkRowEntry(o, i, entryCheck));
          validateRowAndBuildMissing(entryCheck, addedConstraints);
          constraintIndexMap.putAll(entryCheck.indexMap);
        });
    double[] probs = networkTable.getProbabilities();
    constraintIndexMap.forEach((constraint, index) -> probs[index] = constraint.getProbability());
    return addedConstraints;
  }

  private void checkRowEntry(VectorOdometer odometer, int index, MissingEntryCheck entryCheck) {
    NodeState[] states = odometer.getStates();
    Optional<P> constraintOpt = getNextConstraintIfEntryMatches(entryCheck, states);
    if (constraintOpt.isPresent()) {
      P constraint = constraintOpt.get();
      double probability = constraint.getProbability();
      validateProbability(probability, constraint);
      entryCheck.sum -= probability;
      entryCheck.indexMap.put(constraint, index);
    } else {
      entryCheck.missingInRow++;
      entryCheck.missing.addAll(Arrays.asList(states));
      entryCheck.missingIndex = index;
    }
  }

  private void validateRowAndBuildMissing(MissingEntryCheck entryCheck, List<P> addedConstraints) {
    switch (entryCheck.missingInRow) {
      case 0 -> checkProbabilitiesValid(entryCheck);
      case 1 -> fillMissingEvent(entryCheck, addedConstraints);
      default -> cannotPerformDirectMap(entryCheck);
    }
  }

  private Optional<P> getNextConstraintIfEntryMatches(
      MissingEntryCheck entryCheck, NodeState[] states) {
    return Optional.ofNullable(entryCheck.constraintQueue.peek())
        .filter(c -> Arrays.stream(states).allMatch(c.getAllStates()::contains))
        .map(c -> entryCheck.constraintQueue.poll());
  }

  private void validateProbability(double probability, P constraint) {
    if (probability > 1.0 || probability < 0.0) {
      throw new ConstraintValidationException(
          "Constraint %s had an illegal probability!".formatted(constraint));
    }
  }

  private void checkProbabilitiesValid(MissingEntryCheck entryCheck) {
    if (DoublePrecision.fuzzyEquals(entryCheck.sum, 0)) return;
    throw new IllegalStateException(getIllegalSumString(entryCheck));
  }

  private void fillMissingEvent(MissingEntryCheck entryCheck, List<P> addedConstraints) {
    P fillInConstraint = buildMissingFromRow(entryCheck);
    validator.validateCPTConstraint(fillInConstraint);
    addedConstraints.add(fillInConstraint);
    entryCheck.indexMap.put(fillInConstraint, entryCheck.missingIndex);
  }

  private void cannotPerformDirectMap(MissingEntryCheck entryCheck) {
    throw new CptDirectMappingException(
        "Cannot direct impute CPTs in %s, %d missing from row"
            .formatted(networkTable.getTableName(), entryCheck.missingInRow));
  }

  protected abstract String getIllegalSumString(MissingEntryCheck entryCheck);

  protected abstract P buildMissingFromRow(MissingEntryCheck entryCheck);

  @Override
  public Predicate<Node> checkLockOuter() {
    return networkTable.getEvents()::contains;
  }

  @Override
  public Predicate<Node> checkLockInner() {
    return networkTable.getConditions()::contains;
  }

  protected class MissingEntryCheck {
    protected final Queue<P> constraintQueue;
    protected double sum;
    protected Map<P, Integer> indexMap;
    protected Set<NodeState> missing;
    protected int missingIndex;
    protected int missingInRow;

    protected MissingEntryCheck(List<P> constraints) {
      constraintQueue = new ArrayDeque<>(constraints);
      missing = new HashSet<>();
      indexMap = new HashMap<>();
    }

    protected void setNewRow() {
      sum = 1.0;
      missingInRow = 0;
      missing.clear();
      indexMap.clear();
    }
  }
}
