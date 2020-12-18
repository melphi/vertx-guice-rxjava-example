package net.dainco.module.core.support;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class DateUtils {
  private static final ZoneId ZONE_UTC = ZoneId.of("UTC");

  /**
   * Returns the current UTC timestamp in milliseconds.
   */
  public static long getTimestampUtc() {
    return ZonedDateTime.now(ZONE_UTC)
        .toInstant()
        .toEpochMilli();
  }

  public static YearMonth getYearMonth(long timestampUtc) {
    ZonedDateTime dateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestampUtc), ZONE_UTC);
    return YearMonth.of(dateTime.getYear(), dateTime.getMonth());
  }
}
