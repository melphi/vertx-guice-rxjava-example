package net.dainco.module.core.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.YearMonth;
import org.junit.jupiter.api.Test;

class DateUtilsTest {
  @Test
  public void testGetTimestampUtc() {
    // When.
    long timestampUtc = DateUtils.getTimestampUtc();

    // Then.
    assertThat(timestampUtc).isGreaterThan(1000000000000L);
    assertThat(timestampUtc).isLessThan(2000000000000L);
  }

  @Test
  public void testGetYearMonth() {
    // When.
    YearMonth yearMonth = DateUtils.getYearMonth(1600361435000L);

    // Then.
    assertThat(yearMonth.getYear()).isEqualTo(2020);
    assertThat(yearMonth.getMonthValue()).isEqualTo(9);
  }
}
