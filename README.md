<div align="center">

# AR_BayesSolver

### A Java library for solving and inference on partially constrained Bayesian Networks.

![Java](https://img.shields.io/badge/Java-21+-orange)

</div>

# Overview

AR_BayesSolver is a library that enables easy constructiton of Bayesian Networks, without requiring the user to define the full Conditional Probability Tables (CPTs). Instead the user defines *constraints* on the network, either marginal ```(e.g P(RAIN=true) = 0.2)``` or conditional ```(e.g P(SPRINKLER=false | RAIN=true) = 0.95)```. These are used to perform an **Iterative Proportional Fitting Procedure (IPFP)**, accelerated by the **Junction Tree Algorithm (JTA)**, to calculate a "best-fit" probability distribution that conforms to the given constraints. This is best suited in situations where the user only has partial domain knowledge (i.e. does not have the complete CPT for every node).

# Features

- API for constructing Bayesian Network structures
- Constraint-based marginal and conditional probability inputation
- CPT estimation from a partially-constrained network using JTA/IPFP
- Perform direct probabilistic inference to query for marginal and joint probabilities.
- Generate random samples using a Weighted Likelihood sampling algorithm.

# Installation

// TODO

# Quick Start 

// TODO

# API

// TODO

# How It Works

// TODO

