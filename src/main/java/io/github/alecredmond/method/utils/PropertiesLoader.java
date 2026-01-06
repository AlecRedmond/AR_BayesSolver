package io.github.alecredmond.method.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {

  public static Properties loadProperties(String resourceFileName) throws IOException {
    Properties configuration = new Properties();
    InputStream inputStream =
        PropertiesLoader.class.getClassLoader().getResourceAsStream(resourceFileName);
    configuration.load(inputStream);
    assert inputStream != null;
    inputStream.close();
    return configuration;
  }
}
