package io.github.alecredmond.method.utils;

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
    int cyclesLimit = test.loadInt("app.solver.cyclesLimit");
    assertEquals(1000, cyclesLimit);
  }

  @Test
  void loadProperty() {
    boolean openFileOnCreation =
        test.loadProperty("app.printer.openFileOnCreation", Boolean::parseBoolean);
    assertTrue(openFileOnCreation);
  }

  @Test
  void loadDouble() {
    double convergeThreshold = test.loadDouble("app.solver.convergeThreshold");
    assertEquals(1e-9, convergeThreshold, 1e-6);
  }

  @Test
  void loadBoolean() {
    boolean printToConsole = test.loadBoolean("app.printer.printToConsole");
    assertFalse(printToConsole);
  }

  @Test
  void loadString() {
    String saveDir = test.loadString("app.printer.saveDirectory");
    assertEquals("$$user.home$$/AR_Tools/bayes_solver/output/", saveDir);
  }

  @Test
  void loadDirectory() {
    String saveDir = test.loadDirectory("app.printer.saveDirectory");
    String userHome = System.getProperty("user.home");
    assertTrue(saveDir.contains(userHome));
  }
}
