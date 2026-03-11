package io.github.alecredmond.method.inference.junctiontree;

import io.github.alecredmond.application.inference.SolverConfigs;
import io.github.alecredmond.application.inference.junctiontree.Clique;
import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.probabilitytables.internal.JunctionTreeTable;
import io.github.alecredmond.method.inference.InferenceEngine;
import io.github.alecredmond.method.inference.junctiontree.handlers.JTAConstraintHandler;
import io.github.alecredmond.method.printer.NetworkPrinter;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.DoubleAdder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JTASolver {
  private static final boolean DEBUG_PRINT_CLIQUE_TABLES = true;

  private JTASolver() {}

  public static void solveNetwork(InferenceEngine engine) {
    SolverConfigs configs = new SolverConfigs();
    Instant start = Instant.now();
    boolean writeLogs = configs.isLogSolverProgress();

    if (writeLogs) {
      log.info("STARTING SOLVER");
    }

    JunctionTreeAlgorithm jta = buildJTA(engine.getNetworkData());

    double lastError;
    double error = Double.MAX_VALUE;
    double converge = Double.MAX_VALUE;

    long now = Instant.now().getEpochSecond();
    long endTime = now + configs.getTimeLimitSeconds();
    long nextLogTime = now + configs.getLogIntervalSeconds();

    Map<Clique, List<JTAConstraintHandler>> constraintMap =
        jta.getData().getConstraintHandlersMap();

    boolean thresholdReached = false;
    boolean timeLimitReached;
    int cycle;

    for (cycle = 0; cycle < configs.getCyclesLimit(); cycle++) {
      lastError = error;
      error = runSolverCycleAndReturnError(jta, constraintMap);
      converge = Math.abs(error - lastError);

      now = Instant.now().getEpochSecond();
      thresholdReached = converge <= configs.getConvergeThreshold();
      timeLimitReached = now >= endTime;

      if (thresholdReached || timeLimitReached) {
        break;
      }

      if (writeLogs && now >= nextLogTime) {
        nextLogTime = now + configs.getLogIntervalSeconds();
        logCycleComplete(cycle, converge, error);
      }
    }

    if (writeLogs) {
      logEndStatement(thresholdReached, start, Instant.now());
      logCycleComplete(cycle, converge, error);
    }

    jta.sumTransfer(jta.getData().getRootCliques()[0]);
    jta.writeTablesToNetwork();

    if (DEBUG_PRINT_CLIQUE_TABLES) {
      NetworkPrinter printer = new NetworkPrinter(jta.getData().getBayesianNetworkData());
      List<JunctionTreeTable> tables =
          Arrays.stream(jta.getData().getCliques()).map(Clique::getTable).toList();
      printer.printTables(tables, "JUNCTION TREE TABLES");
      printer.printNetwork();
      printer.printObserved();
    }
  }

  private static JunctionTreeAlgorithm buildJTA(BayesianNetworkData networkData) {
    JunctionTreeAlgorithm jta =
        new JunctionTreeAlgorithm(JTAInitializer.buildSolverConfiguration(networkData));
    jta.marginalizeTables();
    return jta;
  }

  private static double runSolverCycleAndReturnError(
      JunctionTreeAlgorithm jta, Map<Clique, List<JTAConstraintHandler>> constraintHandlers) {

    DoubleAdder error = new DoubleAdder();

    constraintHandlers.forEach(
        (clique, handlers) ->
            handlers.forEach(
                h -> {
                  error.add(h.adjustAndReturnError());
                  jta.sumTransfer(clique);
                }));

    return error.sum();
  }

  private static void logCycleComplete(int cycle, double loss, double error) {
    log.info(String.format("CYCLE %d : LOSS = %1.2e : ERROR = %1.2e", cycle, loss, error));
  }

  private static void logEndStatement(boolean thresholdReached, Instant start, Instant end) {
    long msDuration = end.toEpochMilli() - start.toEpochMilli();
    if (thresholdReached) {
      log.info("SOLVER FOUND A SOLUTION IN {} ms", msDuration);
    } else {
      log.info("SOLVER TIMED OUT AFTER {} ms", msDuration);
    }
  }
}
