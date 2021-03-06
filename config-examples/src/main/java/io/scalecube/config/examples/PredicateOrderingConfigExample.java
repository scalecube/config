package io.scalecube.config.examples;

import io.scalecube.config.ConfigRegistry;
import io.scalecube.config.ConfigRegistrySettings;
import io.scalecube.config.StringConfigProperty;
import io.scalecube.config.source.ClassPathConfigSource;
import io.scalecube.config.source.SystemPropertiesConfigSource;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class PredicateOrderingConfigExample {

  /**
   * Main method for example of predicate ordering.
   *
   * @param args program arguments
   */
  public static void main(String[] args) {
    Predicate<Path> propsPredicate = path -> path.toString().endsWith(".props");
    Predicate<Path> rootPredicate =
        propsPredicate.and(path -> path.toString().contains("config.props"));
    Predicate<Path> firstPredicate = propsPredicate.and(path -> path.toString().contains("order1"));
    Predicate<Path> secondPredicate =
        propsPredicate.and(path -> path.toString().contains("order2"));
    Predicate<Path> customSysPredicate =
        propsPredicate.and(path -> path.toString().contains("customSys"));

    // Emulate scenario where sys.foo was also given from system properties
    // System.setProperty("sys.foo", "sys foo from java system properties");

    ConfigRegistry configRegistry =
        ConfigRegistry.create(
            ConfigRegistrySettings.builder()
                .addLastSource("sysProps", new SystemPropertiesConfigSource())
                .addLastSource(
                    "customSysProps",
                    new SystemPropertiesConfigSource(new ClassPathConfigSource(customSysPredicate)))
                .addLastSource(
                    "classpath",
                    new ClassPathConfigSource(
                        Stream.of(firstPredicate, secondPredicate, rootPredicate)
                            .collect(Collectors.toList())))
                .build());

    StringConfigProperty orderedProp1 = configRegistry.stringProperty("orderedProp1");
    String foo = configRegistry.stringProperty("foo").valueOrThrow();
    String bar = configRegistry.stringProperty("bar").valueOrThrow();
    String sysFoo = configRegistry.stringProperty("sys.foo").valueOrThrow();

    System.out.println(
        "### Matched by first predicate: orderedProp1=" + orderedProp1.value().get());
    System.out.println("### Regardeless of predicates: foo=" + foo + ", bar=" + bar);
    System.out.println(
        "### Custom system property: sysFoo="
            + sysFoo
            + ", System.getProperty(sysFoo)="
            + System.getProperty("sys.foo"));
  }
}
