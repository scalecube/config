package io.scalecube.config.source;

import io.scalecube.config.utils.ThrowableUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class FilteredPathConfigSource implements ConfigSource {
  private static final Logger LOGGER = LoggerFactory.getLogger(FilteredPathConfigSource.class);

  protected final List<Predicate<Path>> predicates;

  protected FilteredPathConfigSource(List<Predicate<Path>> predicates) {
    Objects.requireNonNull(predicates, "FilteredPathConfigSource: predicates are required");
    this.predicates = Collections.unmodifiableList(predicates);
  }

  protected final Map<Path, Map<String, String>> loadConfigMap(Collection<Path> pathCollection) {
    return pathCollection.stream()
        .filter(path -> predicates.stream().anyMatch(predicate -> predicate.test(path)))
        .collect(Collectors.toMap(path -> path, this::loadProperties));
  }

  private Map<String, String> loadProperties(Path input) {
    try (InputStream is = input.toUri().toURL().openStream()) {
      Properties properties = new Properties();
      properties.load(is);
      return fromProperties(properties);
    } catch (Exception e) {
      LOGGER.error("Exception at loading props from '{}', cause: {}", input, e);
      throw ThrowableUtil.propagate(e);
    }
  }

  private Map<String, String> fromProperties(Properties properties) {
    Map<String, String> map = new HashMap<>();
    for (Enumeration<?> e = properties.propertyNames(); e.hasMoreElements();) {
      String key = (String) e.nextElement();
      map.put(key, properties.getProperty(key));
    }
    return map;
  }
}
