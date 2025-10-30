<div align="center">

# AR_BayesSolver

### A Java library for solving and inference on partially constrained Bayesian Networks.

![Java](https://img.shields.io/badge/Java-21+-orange)

</div>

# Overview

AR_BayesSolver is a library that enables easy constructiton of Bayesian Networks, without requiring the user to define the full Conditional Probability Tables (CPTs). Instead the user defines *constraints* on the network, either marginal (e.g P(RAIN=true) = 0.2) or conditional (e.g P(SPRINKLER=false | RAIN=true) = 0.95). These are used to perform an **Iterative Proportional Fitting Procedure (IPFP)**, accelerated by the **Junction Tree Algorithm (JTA)**, to calculate a "best-fit" probability distribution according to the constraints. This is ideal in situations where the user only has partial domain knowledge (i.e. does not have the complete CPT for every node). This library also has functionality for performing inference, both directly (using the JTA) and indirectly (via weighted likelihood sampling). 

