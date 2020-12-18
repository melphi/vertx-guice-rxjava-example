package net.dainco.module.core.support;

import java.util.function.Function;

public final class StreamUtils {
  @FunctionalInterface
  public interface CheckedFunction<T, R> {
    R apply(T t) throws Exception;
  }

  public static <T, R> Function<T, R> unchecked(CheckedFunction<T, R> checkedFunction) {
    return t -> {
      try {
        return checkedFunction.apply(t);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };
  }
}
