package io.github.alecredmond.internal.application.network.cptmapper;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.internal.method.constraints.types.conditionalconstraint.ConditionalConstraintValidator;
import io.github.alecredmond.internal.method.constraints.types.marginalconstraint.MarginalConstraintValidator;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
@SuppressWarnings("rawtypes")
public class CptMapperData {
  private final ConditionalConstraintValidator conditionalValidator;
  private final MarginalConstraintValidator marginalValidator;
  private final List<ProbabilityConstraint> allConstraints;
  private Map<Node, DirectMapperNodeInput> mapperNodes;

  public CptMapperData(BayesianNetworkData networkData) {
    this.conditionalValidator = new ConditionalConstraintValidator();
    this.marginalValidator = new MarginalConstraintValidator();
    this.allConstraints = networkData.getConstraints();
  }
}
