package net.dainco;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class TestingConstants {
  public static final Long DEFAULT_TIMESTAMP = ZonedDateTime.of(2019, 11, 9, 8, 59, 27, 1234, ZoneId.of("UTC"))
      .toInstant()
      .toEpochMilli();
}
