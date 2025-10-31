<div align="center">

# AR_BayesSolver

### A Java library for solving and inference on partially constrained Bayesian Networks.

![Java](https://img.shields.io/badge/Java-21+-orange)

</div>

# Overview

AR_BayesSolver is a library that enables easy Bayesian Network constructiton and inference without requiring the user to define the full Conditional Probability Tables (CPTs). Instead the user defines *constraints* on the network, either marginal ```(e.g P(RAIN=true) = 0.2)``` or conditional ```(e.g P(SPRINKLER=false | RAIN=true) = 0.95)```. Using a Junction Tree Algorithm (JTA) accelerated Iterative Proportional Fitting Procedure (IPFP), a "best-fit" probability distribution is calculated that conforms to the given constraints. This is best suited in situations where the user only has partial domain knowledge (i.e. does not have the complete CPT for every node).

# Features

- API for constructing Bayesian Network structures
- Imputation for marginal and conditional constraints, including constraints independent of the network structure.
- CPT estimation from a partially-constrained network using JTA/IPFP
- Perform direct probabilistic inference to query for marginal and joint probabilities.
- Generate random samples using a Weighted Likelihood sampling algorithm.

# Installation

// TODO

# Quick Start 

//TODO - ADD FIGURE 1

This demonstration will show how to build the Rain - Sprinkler - Wet Grass Bayesian Network as seen in figure 1. It will walk through creating the network; adding nodes, defining parent/child relationships, and adding constraints from known evidence. After this, it will demonstrate how to perform inference on the network and obtain random samples.

### 1. Create a Network

Most of the user's interaction with this library will be through the BayesianNetwork interface, which can be instantiated as follows:

```Java
BayesianNetwork network = BayesianNetwork.newNetwork("RAIN_SPRINKLER_WET_GRASS");
```

### 2. Adding Nodes

There are two ways to add nodes to the network, either by defining the Node and its states manually, or using the BayesianNetwork instance itself: 

```Java 
// Manual Creation 

Node rain = new Node("RAIN",List.of("RAIN:TRUE","RAIN:FALSE"));
network.addNode(rain);

// Using BayesianNetwork

network.addNode("SPRINKLER",List.of("SPRINKLER:TRUE","SPRINKLER:FALSE"))
	   .addNode("WET_GRASS",List.of("WET_GRASS:TRUE","WET_GRASS:FALSE"));

```

**Note that all node IDs and node state IDs must be unique!**
If you are defining your Nodes/NodeStates using strings in the manner shown here, it is recommended to pre-append each state with the name of the node.

### 3. Defining the graph structure:

To define the parent/child relationship, we can then call the ```.addParent()``` or ```.addParents()``` method, which will add a connecting node to the graph by using to the IDs of the child/parent node(s):

```Java
network.addParent("SPRINKLER","RAIN")
       .addParents("WET_GRASS",List.of("RAIN","SPRINKLER"));
```

### 4. Adding Constraints

//TODO

# API

// TODO

# How It Works

// TODO

