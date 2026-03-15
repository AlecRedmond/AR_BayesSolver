package io.github.alecredmond.internal.serialization;

import static io.github.alecredmond.internal.method.utils.AppProperty.*;

import io.github.alecredmond.internal.method.utils.PropertiesLoader;
import java.io.File;
import javax.swing.filechooser.FileFilter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class NetworkFileIoProperties {
  private final String extension;
  private final File directory;
  private final FileFilter fileFilter;

  public NetworkFileIoProperties() {
    PropertiesLoader loader = new PropertiesLoader();
    directory = new File(loader.loadDirectory(DIRECTORY_ROOT, DIRECTORY_SAVE));
    extension = loader.loadString(EXTENSION_FILE_TYPE);
    fileFilter = buildFileFilter();
    if (directory.mkdir()) {
      log.info("Directory Created: {}", directory.getPath());
    }
  }

  private FileFilter buildFileFilter() {
    return new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.getName().endsWith(extension);
      }

      @Override
      public String getDescription() {
        return "AR Bayes file (*%s)".formatted(extension);
      }
    };
  }
}
