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
  private static final String REMOVE_PUNCTUATION = "[^\\sa-zA-Z0-9]";
  private static final int ISO_TIME_STRING_LIMIT = 6;
  private static final String TXT_EXTENSION = ".txt";
  private final PrinterConfigs configs;

  public FileExporter(PrinterConfigs configs) {
    this.configs = configs;
  }

  public void exportLinesToFile(List<String> outputLines, String fileSuffix, String networkName) {
    String filePath = determineFilePath(networkName, fileSuffix);
    printWriteToFilePath(filePath, outputLines);
    if (configs.isOpenFileOnCreation()) {
      openCreatedFile(filePath);
    }
    if (configs.isOpenFolderOnCreation()) {
      openFolder();
    }
  }

  private String determineFilePath(String networkName, String fileSuffix) {
    String filePath = "";
    try {
      String baseFilePath = createBaseFilePath(networkName, fileSuffix);
      int i = 0;
      boolean fileCreated = false;
      while (!fileCreated) {
        filePath = addSuffix(baseFilePath, i);
        fileCreated = new File(filePath).createNewFile();
        i++;
      }
      return filePath;
    } catch (IOException | SecurityException e) {
      log.error("Error attempting to create file {}", filePath);
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

  private void openFolder() {
    try {
      Desktop.getDesktop().open(new File(configs.getSaveDirectory()));
    } catch (IOException e) {
      log.error("Could not open the save directory folder!");
    }
  }

  private String createBaseFilePath(String networkName, String fileSuffix) {
    return "%s%s-%s_%s"
        .formatted(configs.getSaveDirectory(), generateDateTimeString(), networkName, fileSuffix)
        .replace(' ', '_');
  }

  private String addSuffix(String filePathBase, int counter) {
    String suffix = counter == 0 ? "" : "_%d".formatted(counter);
    return "%s%s%s".formatted(filePathBase, suffix, TXT_EXTENSION);
  }

  private String generateDateTimeString() {
    LocalDateTime now = LocalDateTime.now();
    String date = now.format(DateTimeFormatter.BASIC_ISO_DATE);
    String time =
        now.format(DateTimeFormatter.ISO_LOCAL_TIME)
            .replaceAll(REMOVE_PUNCTUATION, "")
            .substring(0, ISO_TIME_STRING_LIMIT);
    return date + "-" + time;
  }
}
