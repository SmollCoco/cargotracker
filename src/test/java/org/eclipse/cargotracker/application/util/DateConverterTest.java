package org.eclipse.cargotracker.application.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

class DateConverterTest {

  @Test
  void shouldRoundTripDate() {
    LocalDate original = LocalDate.of(2025, 1, 5);

    String asString = DateConverter.toString(original);

    assertEquals("1/5/2025", asString);
    assertEquals(original, DateConverter.toDate(asString));
  }

  @Test
  void shouldRoundTripDateTime() {
    LocalDateTime original = LocalDateTime.of(2025, 1, 5, 13, 15).truncatedTo(ChronoUnit.MINUTES);

    String asString = DateConverter.toString(original);

    assertEquals(original, DateConverter.toDateTime(asString));
  }

  @Test
  void shouldRejectInvalidDate() {
    assertThrows(DateTimeParseException.class, () -> DateConverter.toDate("13/42/2025"));
  }
}
