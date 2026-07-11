package io.github.alecredmond.internal.method.utils;

import io.github.alecredmond.exceptions.PropertiesLoaderException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PropertiesLoader {
  private static final String DEFAULT_PROPERTIES = "/app-default.properties";
  private static final String USER_PROPERTIES = "app.properties";
  private static final Pattern SYSTEM_PROPS_REGEX = Pattern.compile("\\$\\$(.*?)\\$\\$");
  private final Properties properties;

  public PropertiesLoader() {
    properties = new Properties();
    loadDefaultProps();
    loadUserProps();
  }

  private void loadDefaultProps() {
    try (InputStream defaultStream =
        PropertiesLoader.class.getResourceAsStream(DEFAULT_PROPERTIES)) {
      properties.load(defaultStream);
    } catch (IOException e) {
      throw new PropertiesLoaderException(e);
    }
  }

  private void loadUserProps() {
    try (InputStream userStream =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(USER_PROPERTIES)) {
      properties.load(userStream);
    } catch (IOException e) {
      log.warn("User Properties Warning: {}", e.getMessage());
    } catch (NullPointerException e) {
      log.warn("User Properties file {} does not exist! Using defaults...", USER_PROPERTIES);
    }
  }

  public int loadInt(AppProperty property) {
    try {
      return loadProperty(property.getKey(), Integer::parseInt);
    } catch (NumberFormatException e) {
      throw new PropertiesLoaderException(e);
    }
  }

  public <T> T loadProperty(String propName, Function<String, T> parser) {
    return parser.apply(properties.getProperty(propName));
  }

  public double loadDouble(AppProperty property) {
    try {
      return loadProperty(property.getKey(), Double::parseDouble);
    } catch (NumberFormatException e) {
      throw new PropertiesLoaderException(e);
    }
  }

  public String loadDirectory(AppProperty directoryProp, AppProperty extensionProp) {
    return loadDirectory(directoryProp) + loadString(extensionProp);
  }

  public String loadDirectory(AppProperty property) {
    String raw = properties.getProperty(property.getKey());
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
    return properties.getProperty(property.getKey());
  }

  public boolean loadBoolean(AppProperty property) {
    return loadProperty(property.getKey(), Boolean::parseBoolean);
  }
}
