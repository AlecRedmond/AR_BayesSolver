package io.github.alecredmond.internal.method.network.changehandlers;

import io.github.alecredmond.export.network.BayesianNetworkData;
import java.beans.PropertyChangeEvent;

public interface NetworkChangeHandler {
  void applyChange(PropertyChangeEvent evt, BayesianNetworkData networkData);
}
