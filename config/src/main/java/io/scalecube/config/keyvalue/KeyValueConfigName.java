package io.scalecube.config.keyvalue;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Generic key-value config name. Comes in two parts: group name and config collection name.
 */
public final class KeyValueConfigName {
  /**
   * A group name. Nullable field.
   */
  private final String groupName;

  /**
   * A config collection name. Not null.
   */
  private final String collectionName;

  public KeyValueConfigName(@Nullable String groupName, @Nonnull String collectionName) {
    this.groupName = groupName;
    this.collectionName = Objects.requireNonNull(collectionName);
  }

  public String getQualifiedName() {
    return groupName != null ? groupName + '.' + collectionName : collectionName;
  }

  public Optional<String> getGroupName() {
    return Optional.ofNullable(groupName);
  }

  public String getCollectionName() {
    return collectionName;
  }

  @Override
  public String toString() {
    return "KeyValueConfigName{" +
        "groupName='" + groupName + '\'' +
        ", collectionName='" + collectionName + '\'' +
        '}';
  }
}
