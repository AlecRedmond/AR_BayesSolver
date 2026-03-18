package io.github.alecredmond.internal.fileio;

import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.internal.serialization.BayesianNetworkSerializer;
import io.github.alecredmond.export.serialization.network.SerializedBayesianNetwork;
import java.io.*;
import javax.swing.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NetworkFileIO {
  private final BayesianNetworkSerializer mapper;
  private final NetworkFileIoProperties properties;

  public NetworkFileIO(BayesianNetworkSerializer mapper) {
    this.mapper = mapper;
    this.properties = new NetworkFileIoProperties();
  }

  public boolean saveNetwork(BayesianNetwork network) {
    JFileChooser fileChooser = new JFileChooser(properties.getDirectory());
    fileChooser.setFileFilter(properties.getFileFilter());
    if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
      return saveNetwork(network, fileChooser.getSelectedFile());
    }
    return false;
  }

  public boolean saveNetwork(BayesianNetwork network, File selectedFile) {
    selectedFile = checkAddExtension(selectedFile);
    try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(selectedFile))) {
      SerializedBayesianNetwork sto = mapper.serialize(network);
      out.writeObject(sto);
      log.info("Network saved to {}", selectedFile.getPath());
    } catch (IOException e) {
      log.error("Exception attempting to save network to file: {}", e.getMessage());
      return false;
    }
    return true;
  }

  private File checkAddExtension(File file) {
    String fileName = file.getAbsolutePath();
    String extension = properties.getExtension();
    return fileName.endsWith(extension) ? file : new File(fileName + extension);
  }

  public boolean saveNetwork(BayesianNetwork bayesianNetwork, String filePath) {
    return saveNetwork(bayesianNetwork, new File(filePath));
  }

  public BayesianNetwork loadNetwork(String filePath) {
    return loadNetwork(new File(filePath));
  }

  public BayesianNetwork loadNetwork(File selectedFile) {
    try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(selectedFile))) {
      SerializedBayesianNetwork sto = (SerializedBayesianNetwork) in.readObject();
      return mapper.deSerialize(sto);
    } catch (IOException e) {
      log.error("IOException attempting to load network {}", e.getMessage());
    } catch (ClassNotFoundException e) {
      log.error(e.getMessage());
      log.error("Selected file was not a recognised instance of a Bayesian Network");
    }
    return null;
  }

  public BayesianNetwork loadNetwork() {
    JFileChooser fileChooser = new JFileChooser(properties.getDirectory());
    fileChooser.setFileFilter(properties.getFileFilter());
    if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
      return loadNetwork(fileChooser.getSelectedFile());
    }
    return null;
  }
}
