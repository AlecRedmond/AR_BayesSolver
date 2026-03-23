package io.github.alecredmond.internal.method.utils;

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

  public int loadInt(AppProperty property) {
    try {
      return loadProperty(property.get(), Integer::parseInt);
    } catch (NumberFormatException e) {
      throw new PropertiesLoaderException(e);
    }
  }

  public <T> T loadProperty(String propName, Function<String, T> parser) {
    return parser.apply(properties.getProperty(propName));
  }

  public double loadDouble(AppProperty property) {
    try {
      return loadProperty(property.get(), Double::parseDouble);
    } catch (NumberFormatException e) {
      throw new PropertiesLoaderException(e);
    }
  }

  public String loadDirectory(AppProperty directoryProp, AppProperty extensionProp) {
    return loadDirectory(directoryProp) + loadString(extensionProp);
  }

  public String loadDirectory(AppProperty property) {
    String raw = properties.getProperty(property.get());
    Matcher matcher = SYSTEM_PROPS_REGEX.matcher(raw);
    StringBuilder sb = new StringBuilder();
    while (matcher.find()) {
      Optional<String> propertyOpt = Optional.ofNullable(System.getProperty(matcher.group(1)));
      String replacement = propertyOpt.orElseGet(matcher::group);
      matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(sb);
    return sb.toString();
  }

  public String loadString(AppProperty property) {
    return properties.getProperty(property.get());
  }

  public boolean loadBoolean(AppProperty property) {
    return loadProperty(property.get(), Boolean::parseBoolean);
  }
}
