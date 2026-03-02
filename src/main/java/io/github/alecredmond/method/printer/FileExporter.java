package io.github.alecredmond.method.printer;

import io.github.alecredmond.application.printer.PrinterConfigs;
import io.github.alecredmond.exceptions.NetworkPrinterException;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileExporter {
  private static final String TXT_EXTENSION = ".txt";
  private final PrinterConfigs configs;

  public FileExporter(PrinterConfigs configs) {
    this.configs = configs;
  }

  public void exportLinesToFile(List<String> outputLines, String fileSuffix, String networkName) {
    String filePath = buildFilePath(networkName, fileSuffix);
    printWriteToFilePath(filePath, outputLines);
    if (configs.isOpenFileOnCreation()) {
      openCreatedFile(filePath);
    }
    if (configs.isOpenFolderOnCreation()) {
      openFolder(filePath);
    }
  }

  private String buildFilePath(String networkName, String fileSuffix) {
    String filePath =
        "%s%s-%s_%s%s"
            .formatted(
                configs.getSaveDirectory(),
                generateDateTimeString(),
                networkName,
                fileSuffix,
                TXT_EXTENSION)
            .replace(' ', '_');

    try {
      File file = new File(filePath);
      File directory = file.getParentFile();
      if (directory.mkdir()) {
        log.info("Directory {} created", directory.getPath());
      }
      if (file.createNewFile()) {
        return filePath;
      }
      return "";
    } catch (IOException | SecurityException e) {
      log.error("Error attempting to create file {}!", filePath);
      throw new NetworkPrinterException(e);
    }
  }

  private void printWriteToFilePath(String filePath, List<String> outputLines) {
    try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
      outputLines.forEach(pw::println);
      log.info("File saved to {}", filePath);
    } catch (IOException | SecurityException e) {
      log.error("Error attempting to write file {}!", filePath);
      throw new NetworkPrinterException(e);
    }
  }

  private void openCreatedFile(String filePath) {
    Desktop dt = Desktop.getDesktop();
    File file = new File(filePath);
    try {
      dt.open(file);
    } catch (IOException e) {
      log.error("{} attempting to open created file at {}", e, file.getAbsolutePath());
    }
  }

  private void openFolder(String filePath) {
    try {
      Desktop.getDesktop().open(new File(filePath).getParentFile());
    } catch (IOException e) {
      log.error("Could not open the save directory folder!");
    }
  }

  private String generateDateTimeString() {
    return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MMdd/HHmmssSSS"));
  }
}
