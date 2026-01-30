package io.github.alecredmond.method.inference.junctiontree.handlers;

import io.github.alecredmond.application.constraints.MarginalConstraint;
import io.github.alecredmond.method.probabilitytables.TableUtils;

public class JTAConstraintHandlerMarginal extends JTAConstraintHandler {
  private final TableUtils utils;

  public JTAConstraintHandlerMarginal(
      JTATableHandler jtaTableHandler, MarginalConstraint constraint) {
    super(jtaTableHandler, constraint);
    this.utils = tableHandler.getTable().getUtils();
  }

  @Override
  protected double calculateEventProbability() {
    return utils.sumProbabilities(eventKey);
  }
}
