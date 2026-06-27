package io.github.alecredmond.internal.method.inference.solver.cptmapper;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.exceptions.CptDirectMappingException;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.NetworkTable;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.application.constraint.ConstraintBuilderData;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintValidator;
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
  protected final ConstraintValidator<P> validator;
  protected final BayesianNetworkData networkData;

  protected CptMapperIterator(
      T networkTable,
      Collection<P> constraints,
      ConstraintValidator<P> validator,
      BayesianNetworkData networkData) {
    this.networkTable = networkTable;
    this.validator = validator;
    this.networkData = networkData;
    this.constraints = radixSortConstraints(networkTable, constraints);
    this.iterator = new VectorIterator<>(new VectorOdometer(networkTable.getVector()), this);
  }

  private List<P> radixSortConstraints(T networkTable, Collection<P> constraints) {
    ProbabilityVector vector = networkTable.getVector();
    Map<NodeState, Integer> stateValueMap = vector.getStateValueMap();
    Comparator<P> comparator =
        Arrays.stream(vector.getNodeArray())
            .map(
                node ->
                    Comparator.comparingInt(
                        (P c) ->
                            c.getAllStates().stream()
                                .filter(s -> s.getNode().equals(node))
                                .findAny()
                                .map(stateValueMap::get)
                                .orElseThrow()))
            .reduce(Comparator::thenComparing)
            .orElseThrow();
    return constraints.stream().distinct().sorted(comparator).toList();
  }

  public List<P> directMapCPTs() {
    MissingEntryCheck entryCheck = new MissingEntryCheck(constraints);
    Map<P, Integer> constraintIndexMap = new LinkedHashMap<>();
    List<P> addedConstraints = new ArrayList<>();
    iterator.iterateOuter(
        () -> {
          entryCheck.setNewRow();
          iterator.iterateInner((o, i) -> checkRowEntry(o, i, entryCheck));
          validateRowAndBuildMissing(entryCheck,addedConstraints);
          constraintIndexMap.putAll(entryCheck.indexMap);
        });
    double[] probs = networkTable.getProbabilities();
    constraintIndexMap.forEach((constraint, index) -> probs[index] = constraint.getProbability());
    return addedConstraints;
  }

  private void checkRowEntry(VectorOdometer odometer, int index, MissingEntryCheck entryCheck) {
    NodeState[] states = odometer.getStates();
    P constraint = entryCheck.constraintQueue.peek();
    boolean constraintMatches =
        constraint != null && Arrays.stream(states).allMatch(constraint.getAllStates()::contains);
    if (constraintMatches) {
      double probability = constraint.getProbability();
      if (probability > 1.0 || probability < 0.0) {
        throwProbabilityError(constraint);
      }
      entryCheck.sum -= probability;
      entryCheck.constraintQueue.poll();
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
      case 1 -> fillMissingEvent(entryCheck,addedConstraints);
      default ->
          throw new CptDirectMappingException(
              "Cannot direct impute CPTs in %s, %d missing from row"
                  .formatted(networkTable.getTableName(), entryCheck.missingInRow));
    }
  }

  private void throwProbabilityError(P constraint) {
    throw new ConstraintValidationException(
        "Constraint %s had an illegal probability!".formatted(constraint));
  }

  private void checkProbabilitiesValid(MissingEntryCheck entryCheck) {
    if (DoublePrecision.fuzzyEquals(entryCheck.sum, 0)) return;
    throw new IllegalStateException(getIllegalSumString(entryCheck));
  }

  private void fillMissingEvent(MissingEntryCheck entryCheck, List<P> addedConstraints) {
    P missing = buildMissingFromRow(entryCheck);
    validator.verifyConstraint(new ConstraintBuilderData(networkData, missing));
    addedConstraints.add(missing);
    entryCheck.indexMap.put(missing, entryCheck.missingIndex);
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
      setNewRow();
    }

    protected void setNewRow() {
      sum = 1.0;
      missingInRow = 0;
      missing.clear();
      indexMap.clear();
    }
  }
}
