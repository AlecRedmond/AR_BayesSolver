package com.artools.application.constraints;

import com.artools.application.node.Node;
import com.artools.application.node.NodeState;

import java.util.Collection;
import java.util.HashSet;

public class SumToOneConstraint extends ParameterConstraint {

    public SumToOneConstraint(Node node) {
        super(node.getStates(), new HashSet<>(), 1.0);
    }
}
