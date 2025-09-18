package com.artools.method.network;

import java.util.Collection;

public interface BayesianNetwork {

    static BayesianNetwork newNetwork(){
        return new BayesNet();
    }

    <T> BayesianNetwork addNode(T nodeID);

    <T, E> BayesianNetwork addNode(T nodeID, Collection<E> nodeStateIDs);

    <T> BayesianNetwork removeNode(T nodeID);

    BayesianNetwork removeAllNodes();

    <T, E> BayesianNetwork addNodeStates(T nodeID, Collection<E> nodeStateIDs);

    <T, E> BayesianNetwork addNodeState(T nodeID, E nodeStateID);

    <T> BayesianNetwork removeNodeStates(T nodeID);

    <T, E> BayesianNetwork removeNodeState(T nodeID, E nodeStateID);

    <T, E> BayesianNetwork addParents(T childID, Collection<E> parentIDs);

    <T, E> BayesianNetwork addParent(T childID, E parentID);

    <T, E> BayesianNetwork removeParent(T childID, E parentID);

    <T> BayesianNetwork removeParents(T childID);

    <T,E> BayesianNetwork addEvidence(T eventStateID, Collection<E> conditionStateIDs, double probability);

    <T> BayesianNetwork addEvidence(T eventStateID, double probability);

    <T,E> BayesianNetwork addEvidence(Collection<T> eventStateIDs,Collection<E> conditionStateIDs,double probability);

    BayesianNetwork solverCyclesLimit(int cyclesLimit);

    BayesianNetwork solverTimeLimit(int timeLimitSeconds);

    BayesianNetwork solverStepSize(double stepSize);

    BayesianNetwork solverConvergeThreshold(double threshold);

    BayesianNetwork solveNetwork();

    void printNetwork();



}
