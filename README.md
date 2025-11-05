<div align="center">

# AR_BayesSolver

### A Java library for solving and inference on partially constrained Bayesian Networks.

![Java](https://img.shields.io/badge/Java-21+-orange)

</div>

# Overview

AR_BayesSolver is a library that enables easy Bayesian Network construction and inference without requiring the user to define the full Conditional Probability Tables (CPTs). Instead, the user defines *constraints* on the network, either marginal (e.g `P(RAIN=true) = 0.2`) or conditional (e.g `P(SPRINKLER=false | RAIN=true) = 0.95`). Using a Junction Tree Algorithm (JTA) accelerated Iterative Proportional Fitting Procedure (IPFP), a "best-fit" probability distribution is calculated that conforms to the given constraints. This is best suited in situations where the user only has partial domain knowledge (i.e. does not have the complete CPT for every node).

# Features

- Simple API for constructing Bayesian Network structures.
- Definition of marginal and conditional probability constraints.
- Support for constraints that are independent of the network's parent/child structure.
- CPT estimation from a partially-constrained network using JTA/IPFP algorithm.
- Perform direct probabilistic inference to query for marginal and joint probabilities.
- Generate random samples using a Weighted Likelihood sampling algorithm.

# Installation

- Planned release on Maven Central soon.

# Quick Start 

<figure>
	<img src="https://i.imgur.com/p7yt72o.png" width="512" alt="Simple Bayesian Network diagram">
	<figcaption>
		<p><i>Fig. 1 - Simple Bayesian Network, <a href="https://commons.wikimedia.org/wiki/File:SimpleBayesNet.svg">src. Wikipedia</a></i></p>
	</figcaption>
</figure>
<br>
This demonstration will show how to build the Rain - Sprinkler - Wet Grass Bayesian Network (Fig. 1). We will create the network, define its structure, add partial constraints, and then perform inference and sampling.

### 1. Create a Network

Most interactions use the `BayesianNetwork` interface.

```Java
BayesianNetwork network = BayesianNetwork.newNetwork("RAIN_SPRINKLER_WET_GRASS");
```

### 2. Adding Nodes

Add nodes with their states, either manually or using the API.

```Java 
// Manual Creation 

Node rain = new Node("RAIN",List.of("RAIN:TRUE","RAIN:FALSE"));
network.addNode(rain);

// Using the BayesianNetwork instance (recommended)

network.addNode("SPRINKLER",List.of("SPRINKLER:TRUE","SPRINKLER:FALSE"))
	   .addNode("WET_GRASS",List.of("WET_GRASS:TRUE","WET_GRASS:FALSE"));

```

**Note:** All node IDs and node state IDs must be unique! It is recommended to prefix states with the node name (e.g. `"RAIN:TRUE"`) if manually defining them using Strings. Node and State IDs may be any object, but it is *highly* recommended to keep all Node IDs/State IDs as the same type. 

### 3. Defining the graph structure:

Define parent/child relationships using the node IDs. 

```Java
network.addParent("SPRINKLER","RAIN")
       .addParents("WET_GRASS",List.of("RAIN","SPRINKLER"));
```

### 4. Adding Constraints

Constraints are built using the node state IDs. A key feature is that **constraints do not have to be aligned with the network structure**; for example, a marginal constraint can be defined on a node with parents, or an ancestor node's state can be conditional on a descendant node's state. 

For this demo, we will provide only *some* of the CPT entries from Fig.1. Due to the nature of IPFP, complementary constraints (e.g., `P(RAIN:FALSE) = 0.8`) are inferred automatically and do not need to be defined.

```Java
// Marginal Constraint
network.addConstraint("RAIN:TRUE", 0.2)
// Conditional Constraints
       .addConstraint("SPRINKLER:TRUE", List.of("RAIN:TRUE"), 0.01)
       .addConstraint("SPRINKLER:TRUE", List.of("RAIN:FALSE"), 0.4)
       .addConstraint("WET_GRASS:TRUE", List.of("RAIN:TRUE", "SPRINKLER:TRUE"), 0.99)
       .addConstraint("WET_GRASS:TRUE", List.of("RAIN:TRUE", "SPRINKLER:FALSE"), 0.9)
       .addConstraint("WET_GRASS:TRUE", List.of("RAIN:FALSE", "SPRINKLER:TRUE"), 0.9)
       .addConstraint("WET_GRASS:TRUE", List.of("RAIN:FALSE", "SPRINKLER:FALSE"), 0.0);
```

### 5. Solving and Setting Evidence

We will now run the JTA/IPFP algorithm to find the best-fit probability distribution that honours all given constraints.
```Java
network.solveNetwork();
```
Once solved, we can set the evidence (observations) for inference.
```Java
//Sets the inference engine to always observe "WET_GRASS:TRUE" 
network.observeNetwork(List.of("WET_GRASS:TRUE")); 
```
Further queries will be conditional on this evidence. To clear observations, simply observe the marginals:
```Java
network.observeMarginals(); 
```
For the next steps, we will keep the evidence `WET_GRASS:TRUE`.

### 6. Printing the CPTs and Marginals

You can print the network's solved CPTs and the current observed marginals to a .txt file.
```Java
// Configure the printer
PrinterConfigs printerConfigs = network.getPrinterConfigs();
printerConfigs.setProbDecimalPlaces(3);
printerConfigs.setPrintToConsole(false); // If set to true, no files will be written
printerConfigs.setOpenFileOnCreation(true);
```
By default, the printer will save files to the directory ```$user_home$/AR_Tools/bayes_solver/output/```
```Java
network.printNetwork()   // Prints the full, solved CPTs
       .printObserved(); // Prints marginals, conditional on current evidence
```
This should automatically open two files, which will look like this: 
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
[...]
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
[...]
```
### 7. Generating Random Samples 

Generate samples based on the current set of observations.  
```Java
int numberOfSamples = 10;
// Samples will be conditional on "WET_GRASS:TRUE"
List<List<String>> samples = network.generateSamples(numberOfSamples,String.class);

/* Potential Samples: 
{"RAIN:TRUE", "SPRINKLER:FALSE", "WET_GRASS:TRUE"}, 
{"RAIN:TRUE", "SPRINKLER:FALSE", "WET_GRASS:TRUE"}, 
[...]
{"RAIN:FALSE", "SPRINKLER:TRUE", "WET_GRASS:TRUE"}, 
{"RAIN:FALSE", "SPRINKLER:TRUE", "WET_GRASS:TRUE"}
*/
```
You can also specify which nodes to include in the sample list:
```Java
List<String> includedNodeIDs = List.of("RAIN");
List<List<String>> samples = network.generateSamples(includedNodeIDs,numberOfSamples,String.class);

/* Potential Samples:
{"RAIN:TRUE"},
{"RAIN:TRUE"},
[...]
{"RAIN:FALSE"}, 
{"RAIN:FALSE"}
*/
```

### 8. Using ProbabilityTables from the network. 
You can extract the raw probability tables for use in your application.

```Java
// Get a solved CPT (a "Network Table")
ProbabilityTable wetGrassCPT = network.getNetworkTable("WET_GRASS");
List<String> cptIDs = List.of("WET_GRASS:TRUE","SPRINKLER:FALSE","RAIN:TRUE");
double cptProb = wetGrassCPT.getProbability(cptIDs);
// cptProb == 0.9

// Get an observed marginal table
MarginalTable wetGrassObserved = network.getObservedTable("WET_GRASS");
double marginalProb = wetGrassObserved.getProbability("WET_GRASS:TRUE");
// marginalProb == 1.0 (because it was our evidence)
```

# API

// TODO.

# How It Works

// TODO.

