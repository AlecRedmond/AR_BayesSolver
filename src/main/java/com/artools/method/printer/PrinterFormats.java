package com.artools.method.printer;

import lombok.Getter;
import lombok.Setter;

public class PrinterFormats {
  @Getter @Setter private static int probPadding = 7;

  @Getter @Setter
  private static String getSaveDirectory =
      System.getProperty("user.home") + "\\AR_Tools\\proportional_fitter\\output\\";

  private PrinterFormats() {}
}
