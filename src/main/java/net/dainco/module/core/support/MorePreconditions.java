package net.dainco.module.core.support;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Strings;

public final class MorePreconditions {
  public static String checkNotNullOrEmpty(String value) {
    return checkNotNullOrEmpty(value, "Value empty or null.");
  }

  public static String checkNotNullOrEmpty(String value, String errorMessage) {
    checkArgument(!Strings.isNullOrEmpty(value), errorMessage);
    return value;
  }
}
