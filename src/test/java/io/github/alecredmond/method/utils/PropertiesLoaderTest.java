package io.github.alecredmond.method.utils;

import static io.github.alecredmond.internal.method.utils.AppProperty.*;
import static org.junit.jupiter.api.Assertions.*;

import io.github.alecredmond.internal.method.utils.PropertiesLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PropertiesLoaderTest {

  PropertiesLoader test;

  @BeforeEach
  void init() {
    test = new PropertiesLoader();
  }

  @Test
  void loadInt() {
    int cyclesLimit = test.loadInt(SOLVER_CYCLES_LIMIT);
    assertEquals(1000, cyclesLimit);
  }

  @Test
  void loadProperty() {
    boolean openFileOnCreation =
        test.loadProperty(PRINTER_OPEN_FILE_ON_CREATION.get(), Boolean::parseBoolean);
    assertTrue(openFileOnCreation);
  }

  @Test
  void loadDouble() {
    double convergeThreshold = test.loadDouble(SOLVER_CONVERGE_THRESHOLD);
    assertEquals(1e-9, convergeThreshold, 1e-6);
  }

  @Test
  void loadBoolean() {
    boolean printToConsole = test.loadBoolean(PRINTER_PRINT_TO_CONSOLE);
    assertFalse(printToConsole);
  }

  @Test
  void loadString() {
    String saveDir = test.loadString(DIRECTORY_ROOT);
    assertEquals("$$user.home$$/AR_Tools/bayes_solver/", saveDir);
  }

  @Test
  void loadDirectory() {
    String saveDir = test.loadDirectory(DIRECTORY_ROOT);
    String userHome = System.getProperty("user.home");
    assertTrue(saveDir.contains(userHome));
  }
}
