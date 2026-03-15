package io.github.alecredmond.internal.serialization;

import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.internal.method.network.BayesianNetworkImpl;
import io.github.alecredmond.internal.serialization.mapper.SerializationMapper;
import io.github.alecredmond.internal.serialization.structure.network.BayesianNetworkDataSTO;
import java.io.*;
import javax.swing.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NetworkFileIO {
  private final SerializationMapper mapper;
  private final NetworkFileIoProperties properties;

  public NetworkFileIO(SerializationMapper mapper) {
    this.mapper = mapper;
    this.properties = new NetworkFileIoProperties();
  }

  public boolean saveNetwork(BayesianNetwork network) {
    JFileChooser fileChooser = new JFileChooser(properties.getDirectory());
    fileChooser.setFileFilter(properties.getFileFilter());
    if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
      return saveNetwork(network, checkAddExtension(fileChooser.getSelectedFile()));
    }
    return false;
  }

  public boolean saveNetwork(BayesianNetwork network, File selectedFile) {
    try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(selectedFile))) {
      BayesianNetworkDataSTO sto = mapper.serialize(network);
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

  public boolean saveNetwork(BayesianNetworkImpl bayesianNetwork, String filePath) {
    return saveNetwork(bayesianNetwork, new File(filePath));
  }

  public BayesianNetwork loadNetwork(String filePath) {
    return loadNetwork(new File(filePath));
  }

  public BayesianNetwork loadNetwork(File selectedFile) {
    try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(selectedFile))) {
      BayesianNetworkDataSTO sto = (BayesianNetworkDataSTO) in.readObject();
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
