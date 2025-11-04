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

<figure>
	<img src="https://i.imgur.com/p7yt72o.png" width="512">
	<figcaption>
		<p><i>Fig. 1 - Simple Bayesian Network, <a href="https://commons.wikimedia.org/wiki/File:SimpleBayesNet.svg">src. Wikipedia</a></i></p>
	</figcaption>
</figure>
<br>
This demonstration will show how to build the Rain - Sprinkler - Wet Grass Bayesian Network as seen in Fig. 1. It will walk through creating the network; adding nodes, defining parent/child relationships, and adding constraints from known evidence. After this, it will demonstrate how to perform inference on the network and obtain random samples.

### 1. Create a Network

Most of the user's interaction with this library will be through the BayesianNetwork interface, where a new network can be instantiated as follows:

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

Constraints are built using the ID of the Node and State, as with the graph structure. Additionally, constraints *do not have to be aligned* with the network structure; for example, a marginal constraint can be defined on a node with parents, or an ancestor node state can be conditional on a descendant state. For the sake of this demonstration we will simply copy half of the constraints from Fig.1:

```Java
network.addConstraint("RAIN:TRUE", 0.2)
       .addConstraint("SPRINKLER:TRUE", List.of("RAIN:TRUE"), 0.01)
       .addConstraint("SPRINKLER:TRUE", List.of("RAIN:FALSE"), 0.4)
       .addConstraint("WET_GRASS:TRUE", List.of("RAIN:TRUE", "SPRINKLER:TRUE"), 0.99)
       .addConstraint("WET_GRASS:TRUE", List.of("RAIN:TRUE", "SPRINKLER:FALSE"), 0.9)
       .addConstraint("WET_GRASS:TRUE", List.of("RAIN:FALSE", "SPRINKLER:TRUE"), 0.9)
       .addConstraint("WET_GRASS:TRUE", List.of("RAIN:FALSE", "SPRINKLER:FALSE"), 0.0);
```
Marginal constraints take only the Node State ID and the probability for the input, while Conditional constraints take the *event* node state, a Collection of *condition* node states, and the probability.

Note that due to the nature of IPFP, the complementary constraints (e.g. P(RAIN:FALSE = 0.8)) do not need to be defined. 

### 5. Solving and Setting Evidence

The solver can be run using the method call:
```Java
network.solveNetwork();
```
Which will solve the network's probability tables such that they honour the given constraints. 

Evidence can then be fixed as being "observed" in the network, e.g:
```Java
network.observeNetwork("RAIN:TRUE"); //Sets the inference engine to always observe "RAIN:TRUE" 
```
Further Inference calls will account for the current observations applied to the network. To clear observations, simply observe the marginals:
```Java
network.observeMarginals(); 
```
For now, we will set the former, with "RAIN:TRUE".

### 6. Printing the CPTs and Marginals

The API contains a means of printing the network and the results of the inference engine either to a .txt file or to the console. For this example, we will print to .txt files.
The first thing we need to do is configure the printer using the PrinterConfigs class accessible from the network:
```Java
PrinterConfigs printerConfigs = network.getPrinterConfigs();
printerConfigs.setProbDecimalPlaces(3);
printerConfigs.setPrintToConsole(false);
printerConfigs.setOpenFileOnCreation(true);
```
By default, the printer will save .txt files to the directory ```$user_home$/AR_Tools/bayes_solver/output/```
```Java
network.printNetwork()   //Prints Network Tables (the solved CPTs)
       .printObserved(); //Prints the Marginal Tables for each node, conditional on the current observations 
```
This should automatically open two text files, which will look like this: 
```
NETWORK TABLES:

P(RAIN)
-------------------------
|  RAIN:TRUE| RAIN:FALSE|
|      0.200|      0.800|
-------------------------

P(SPRINKLER|RAIN)
-----------------------------------------------
|           |  SPRINKLER:TRUE| SPRINKLER:FALSE|
|RAIN:TRUE  |           0.010|           0.990|
|RAIN:FALSE |           0.400|           0.600|
-----------------------------------------------

P(WET_GRASS|RAIN,SPRINKLER)
---------------------------------------------------------------------
|                                 |  WET_GRASS:TRUE| WET_GRASS:FALSE|
|RAIN:TRUE       |SPRINKLER:TRUE  |           0.990|           0.010|
|RAIN:TRUE       |SPRINKLER:FALSE |           0.900|           0.100|
|RAIN:FALSE      |SPRINKLER:TRUE  |           0.900|           0.100|
|RAIN:FALSE      |SPRINKLER:FALSE |           0.000|           1.000|
---------------------------------------------------------------------
```
```
OBSERVED TABLES:

P(RAIN|WET_GRASS:TRUE)
-------------------------
|  RAIN:TRUE| RAIN:FALSE|
|      0.385|      0.615|
-------------------------

P(SPRINKLER|WET_GRASS:TRUE)
-----------------------------------
|  SPRINKLER:TRUE| SPRINKLER:FALSE|
|           0.619|           0.381|
-----------------------------------

P(WET_GRASS|WET_GRASS:TRUE)
-----------------------------------
|  WET_GRASS:TRUE| WET_GRASS:FALSE|
|           1.000|           0.000|
-----------------------------------
```


# API

// TODO

# How It Works

// TODO

