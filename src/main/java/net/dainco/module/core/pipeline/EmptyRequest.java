package net.dainco.module.core.pipeline;

import java.io.Serializable;

public final class EmptyRequest implements Serializable {
  public static EmptyRequest empty() {
    return new EmptyRequest();
  }

  private EmptyRequest() {
    // Intentionally empty.
  }
}
