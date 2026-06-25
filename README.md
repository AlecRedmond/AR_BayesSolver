<div align="center">

# AR_BayesSolver

### A Java library for solving and inference on partially constrained Bayesian Networks.

![Java](https://img.shields.io/badge/Java-21+-orange)

</div>

# Overview

AR_BayesSolver is a Java library providing a high-level toolset for working with Bayesian Networks. Bayesian Networks
can be constructed from either full or partial domain knowledge of the network's Conditional Probability Tables (CPTs),
solved using an Iterative Proportional Fitting Procedure (IPFP), and queried with direct inference or Monte Carlo
sampling. The solving and inference processes are accelerated using the Junction Tree Algorithm, and high performance
can be expected for Bayesian Networks with fewer than ~200 Nodes. 

# Features

- Simple API for constructing Bayesian Network structures.
- Allows full CPT imputation or CPT estimation from a partially-constrained network.
- Support for probability constraints that are independent of the network's parent/child structure.
- Perform direct probabilistic inference to query prior or posterior probabilities.
- Generate random samples, with or without fixed observations.

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

This demonstration will show how to build the Rain - Sprinkler - Wet Grass Bayesian Network (Fig. 1). We will create the
network, define its structure, add partial constraints, and then perform inference and sampling.

### 1. Create a Network

Most interactions use the `BayesianNetwork` interface.

```Java
BayesianNetwork wetGrassNetwork = BayesianNetwork.newNetwork("WET GRASS NETWORK");
```

### 2. Adding Nodes

Add nodes with their states using the desired identifiers.

```Java 
wetGrassNetwork
        .addNewNode("RAIN",List.of("RAIN:TRUE", "RAIN:FALSE"))
        .addNewNode("SPRINKLER",List.of("SPRINKLER:TRUE","SPRINKLER:FALSE"))
        .addNewNode("WET_GRASS",List.of("WET_GRASS:TRUE","WET_GRASS:FALSE"));
```

**Note:** All node and node states may use any Serializable type as their identifier, but each identifier must be
unique in the network. If using descriptive Strings, as in the above example, it is advisable to prefix the state
identifiers with the node name (e.g.`"RAIN:TRUE"`).

### 3. Defining the graph structure:

Define parent/child relationships using the node identifiers.

```Java
wetGrassNetwork
        .addParents("SPRINKLER","RAIN")
        .addParents("WET_GRASS",List.of("SPRINKLER","RAIN"));
```

### 4. Adding Constraints

Constraints are built using the node state identifiers. **Constraints do not have to be aligned with the
network structure**; for example, a marginal constraint can be defined on a node with parents, or an ancestor node's
state can be conditional on a descendant node's state. 

For this demo, we will CPT entries within the graph, but we will provide only *some* of the CPT entries from Fig.1. Due to the nature of IPFP, complementary
constraints (e.g., `P(RAIN:FALSE) = 0.8`) are inferred automatically and do not need to be defined.

```Java
wetGrassNetwork
        // Non-conditional probability P(event) = p
        .addConstraint("RAIN:TRUE", 0.2)
        // Conditional Probabilities P(event|conditions) = p
        .addConstraint("SPRINKLER:TRUE", List.of("RAIN:TRUE"), 0.01)
        .addConstraint("SPRINKLER:TRUE", List.of("RAIN:FALSE"), 0.4)
        .addConstraint("WET_GRASS:TRUE", List.of("RAIN:TRUE", "SPRINKLER:TRUE"), 0.99)
        .addConstraint("WET_GRASS:TRUE", List.of("RAIN:TRUE", "SPRINKLER:FALSE"), 0.9)
        .addConstraint("WET_GRASS:TRUE", List.of("RAIN:FALSE", "SPRINKLER:TRUE"), 0.9)
        .addConstraint("WET_GRASS:TRUE", List.of("RAIN:FALSE", "SPRINKLER:FALSE"), 0.0);
```

### 5. Solving and Direct Inference

We will now run the JTA/IPFP algorithm to find the best-fit probability distribution that honours all given constraints.

```Java
wetGrassNetwork.solveNetwork();
```

Once solved, we can perform direct inference by building an inference engine from the solved network.

```Java 
InferenceEngine engine = wetGrassNetwork.buildInferenceEngine();
```

This can be used to query for specific posterior or prior probabilities.
For example, let's set the engine to observe `WET_GRASS:TRUE`. 

```Java 
engine.observeNetworkFromIds("WET_GRASS:TRUE");
```

Further queries will measure the posterior probability, conditional on `WET_GRASS:TRUE`. In other words, all 
measurements now assume the `WET_GRASS` node is locked in the `TRUE` position. Observations can be cleared with the 
following call:

```Java
engine.resetObservations(); 
```

For the next steps, we will keep the observation `WET_GRASS:TRUE`.

If we now want to know the probability of `RAIN:TRUE`, conditional on `WET_GRASS:TRUE`, we can call the following method:

```java
double posteriorRainTrue = engine.getPosteriorProbabilityById("RAIN:TRUE");
System.out.printf("P(RAIN:TRUE|WET_GRASS:TRUE) = %.3f", posteriorRainTrue);

>> P(RAIN:TRUE|WET_GRASS:TRUE) = 0.385
```

### 6. Printing the CPTs and Posterior Probabilities

You can print the network's solved CPTs or the inference engine's observed probability tables to a .txt file. 
By default, the printer will save files to the directory ```$user_home$/AR_Tools/bayes_solver/output/```

```Java
network.printNetwork();   // Prints the solved CPTs
engine.printObserved();   // Prints the posterior probabilities over each node
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

Settings for the printer can be modified within `app.properties`.

### 7. Generating Random Samples

Build a sampler from the BayesianNetwork instance:

```Java
Sampler sampler = wetGrassNetwork.buildSampler();
```

You can apply the observations from the inference engine to generate samples with certain fixed states:

```java
int numberOfSamples = 1000;
SampleCollection sampleCollection = sampler.generateSamples(engine, numberOfSamples);
int samplesWithWetGrassTrue = sampleCollection.countSamplesIncludingStateIds("WET_GRASS:TRUE");
System.out.println(samplesWithWetGrassTrue);

>> 1000
```

Samples contain a specific combination of node states and the frequency of its occurrence in the sampler run.

```java
List<Sample> samples = sampleCollection.getSamples();
Sample firstSample = samples.getFirst();
System.out.println(firstSample);

>> RAIN:TRUE, SPRINKLER:TRUE, WET_GRASS:TRUE : 4

List<NodeState> sampledStates = firstSample.getDisplayedStates(ArrayList::new);
double directInferenceProb = engine.getPosteriorProbability(sampledStates);
System.out.printf("%.2f", directInferenceProb * numberOfSamples);

// P(RAIN:TRUE,SPRINKLER:TRUE|WET_GRASS:TRUE) * 1000
>> 4.23 
```

### 8. Using ProbabilityTables from the network.

You can extract the raw probability tables for use in your application.

```Java
// Extract and query a CPT from the BayesianNetwork
NetworkTable wetGrassCpt = wetGrassNetwork.getNetworkTable("WET_GRASS");
List<String> cptRequestIds = List.of("RAIN:TRUE","SPRINKLER:FALSE","WET_GRASS:TRUE");
double cptRequestProb = wetGrassCpt.getHelper().getProbabilityFromIDs(cptRequestIds);
System.out.printf("%.2f",cptRequestProb);

// P(WET_GRASS:TRUE|RAIN:TRUE, SPRINKLER:FALSE)
>> 0.90

// Extract and query a posterior table from the InferenceEngine.
ObservedTable rainObservedTable = engine.getObservedTableById("RAIN");
double rainFalsePosterior = rainObservedTable.getHelper().getProbabilityById("RAIN:FALSE");
System.out.printf("%.2f", rainFalsePosterior);

// P(RAIN:FALSE|WET_GRASS:TRUE)
>> 0.62
```

# API

// TODO

# How It Works

// TODO

