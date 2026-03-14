package io.github.alecredmond.internal.method.utils;

import io.github.alecredmond.export.application.network.BayesianNetworkData;
import java.io.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NetworkSerializer {
  public void serializeDataOut(BayesianNetworkData data, String filePath) {
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
      oos.writeObject(data);
    } catch (IOException e) {
      log.error("Exception attempting to serialize data:\n{}", e.getMessage());
    }
  }

  public BayesianNetworkData serializeDataIn(String filePath) {
    BayesianNetworkData data;
    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
      data = (BayesianNetworkData) ois.readObject();
    } catch (IOException | ClassNotFoundException e) {
      data = null;
      log.error("Exception attempting to deserialize data:\n{}", e.getMessage());
    }
    return data;
  }
}
