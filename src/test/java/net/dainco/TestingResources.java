package net.dainco;

import static java.util.Objects.requireNonNull;

import java.io.InputStream;

public final class TestingResources {
  public static final String FORBES_SAMPLE_NEWS = "forbes/forbes_sample.html";
  public static final String SEEKINGALPHA_SAMPLE_NEWS = "seekingalpha/seekingalpha_sample.html";
  public static final String PRNEWSWIRE_SAMPLE_NEWS = "prnewswire/prnewswire_sample.html";

  public static InputStream read(String filePath) {
    try {
      return requireNonNull(ClassLoader.getSystemResourceAsStream(filePath));
    } catch (Exception e) {
      throw new IllegalArgumentException(String.format("Could not open file [%s]: [%s].", filePath, e.getMessage()));
    }
  }
}
