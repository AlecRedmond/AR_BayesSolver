package io.github.alecredmond.method.utils;

import io.github.alecredmond.exceptions.PropertiesLoaderException;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropertiesLoader {
  private static final String PROPERTIES_FILE = "/app.properties";
  private static final Pattern SYSTEM_PROPS_REGEX = Pattern.compile("\\$\\$(.*?)\\$\\$");
  private final Properties properties;

  public PropertiesLoader() {
    properties = new Properties();
    try {
      properties.load(PropertiesLoader.class.getResourceAsStream(PROPERTIES_FILE));
    } catch (IOException e) {
      throw new PropertiesLoaderException(e);
    }
  }

  public int loadInt(String propertyName) {
    try {
      return loadProperty(propertyName, Integer::parseInt);
    } catch (NumberFormatException e) {
      throw new PropertiesLoaderException(e);
    }
  }

  public <T> T loadProperty(String propName, Function<String, T> parser) {
    return parser.apply(properties.getProperty(propName));
  }

  public double loadDouble(String propertyName) {
    try {
      return loadProperty(propertyName, Double::parseDouble);
    } catch (NumberFormatException e) {
      throw new PropertiesLoaderException(e);
    }
  }

  public String loadString(String propertyName) {
    return properties.getProperty(propertyName);
  }

  public String loadDirectory(String propertyName) {
    String raw = properties.getProperty(propertyName);
    Matcher matcher = SYSTEM_PROPS_REGEX.matcher(raw);
    StringBuilder sb = new StringBuilder();
    while (matcher.find()) {
      Optional<String> property = Optional.ofNullable(System.getProperty(matcher.group(1)));
      String replacement = property.orElseGet(matcher::group);
      matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(sb);
    return sb.toString();
  }

  public boolean loadBoolean(String propertyName) {
    return loadProperty(propertyName, Boolean::parseBoolean);
  }
}
