package io.scalecube.config;

import io.scalecube.config.audit.ConfigEventListener;
import io.scalecube.config.source.ConfigSource;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Represents settings of config registry.
 *
 * @author Anton Kharenko
 */
public final class ConfigRegistrySettings {

  public static final int DEFAULT_RELOAD_PERIOD_SEC = 15;
  public static final int DEFAULT_RECENT_EVENTS_NUM = 30;
  public static final boolean DEFAULT_JMX_ENABLED = true;
  public static final String DEFAULT_JMX_MBEAN_NAME = "io.scalecube.config:name=ConfigRegistry";

  private final Map<String, ConfigSource> sources;
  private final String host;
  private final int reloadIntervalSec;
  private final int recentConfigEventsNum;
  private final Map<String, ConfigEventListener> listeners;
  private final boolean jmxEnabled;
  private final String jmxMBeanName;

  private ConfigRegistrySettings(Builder builder) {
    Map<String, ConfigSource> sourcesTmp = new LinkedHashMap<>(builder.sources.size());
    for (String name : builder.sourceOrder) {
      sourcesTmp.put(name, builder.sources.get(name));
    }
    this.sources = Collections.unmodifiableMap(sourcesTmp);
    this.host = builder.host != null ? builder.host : resolveLocalHost();
    this.reloadIntervalSec = builder.reloadIntervalSec;
    this.recentConfigEventsNum = builder.recentConfigEventsNum;
    this.listeners = Collections.unmodifiableMap(new HashMap<>(builder.listeners));
    this.jmxEnabled = builder.jmxEnabled;
    this.jmxMBeanName = builder.jmxMBeanName;
  }

  private static String resolveLocalHost() {
    try {
      return InetAddress.getLocalHost().getHostAddress();
    } catch (Exception e) {
      return "unresolved";
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public int getReloadIntervalSec() {
    return reloadIntervalSec;
  }

  public boolean isReloadEnabled() {
    return reloadIntervalSec != Integer.MAX_VALUE;
  }

  public int getRecentConfigEventsNum() {
    return recentConfigEventsNum;
  }

  public Map<String, ConfigEventListener> getListeners() {
    return listeners;
  }

  public Map<String, ConfigSource> getSources() {
    return sources;
  }

  public String getHost() {
    return host;
  }

  public boolean isJmxEnabled() {
    return jmxEnabled;
  }

  public String getJmxMBeanName() {
    return jmxMBeanName;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", ConfigRegistrySettings.class.getSimpleName() + "[", "]")
        .add("sources=" + sources)
        .add("host='" + host + "'")
        .add("reloadIntervalSec=" + reloadIntervalSec)
        .add("recentConfigEventsNum=" + recentConfigEventsNum)
        .add("listeners=" + listeners)
        .add("jmxEnabled=" + jmxEnabled)
        .add("jmxMBeanName='" + jmxMBeanName + "'")
        .toString();
  }

  public static class Builder {
    private final LinkedList<String> sourceOrder = new LinkedList<>();
    private final Map<String, ConfigSource> sources = new HashMap<>();
    private final String host = null;
    private int reloadIntervalSec = DEFAULT_RELOAD_PERIOD_SEC;
    private int recentConfigEventsNum = DEFAULT_RECENT_EVENTS_NUM;
    private final Map<String, ConfigEventListener> listeners = new HashMap<>();
    private boolean jmxEnabled = DEFAULT_JMX_ENABLED;
    private String jmxMBeanName = DEFAULT_JMX_MBEAN_NAME;

    private Builder() {}

    public Builder noReload() {
      this.reloadIntervalSec = Integer.MAX_VALUE;
      return this;
    }

    public Builder reloadIntervalSec(int reloadPeriodSec) {
      this.reloadIntervalSec = reloadPeriodSec;
      return this;
    }

    public Builder keepRecentConfigEvents(int recentConfigEventsNum) {
      this.recentConfigEventsNum = recentConfigEventsNum;
      return this;
    }

    public Builder addListener(ConfigEventListener configEventListener) {
      this.listeners.put(configEventListener.getClass().getSimpleName(), configEventListener);
      return this;
    }

    /**
     * Add config source as the last one to be processed.
     *
     * @param name source alias name
     * @param configSource config source instance
     * @return builder instance
     */
    public Builder addLastSource(String name, ConfigSource configSource) {
      sourceOrder.addLast(name);
      sources.put(name, configSource);
      return this;
    }

    /**
     * Add config source as the first one to be processed.
     *
     * @param name source alias name
     * @param configSource config source instance
     * @return builder instance
     */
    public Builder addFirstSource(String name, ConfigSource configSource) {
      sourceOrder.addFirst(name);
      sources.put(name, configSource);
      return this;
    }

    /**
     * Add config source to be processed before another specified by <code>beforeName</code> source.
     *
     * @param beforeName source alias name before which newly added source will be processed
     * @param name source alias name
     * @param configSource config source instance
     * @return builder instance
     */
    public Builder addBeforeSource(String beforeName, String name, ConfigSource configSource) {
      int ind = sourceOrder.indexOf(beforeName);
      sourceOrder.add(ind, name);
      sources.put(name, configSource);
      return this;
    }

    public Builder jmxEnabled(boolean jmxEnabled) {
      this.jmxEnabled = jmxEnabled;
      return this;
    }

    public Builder jmxMBeanName(String jmxMBeanName) {
      this.jmxMBeanName = jmxMBeanName;
      return this;
    }

    public ConfigRegistrySettings build() {
      return new ConfigRegistrySettings(this);
    }
  }
}
