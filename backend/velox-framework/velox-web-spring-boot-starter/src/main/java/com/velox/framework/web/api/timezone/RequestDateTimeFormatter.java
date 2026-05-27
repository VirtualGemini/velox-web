package com.velox.framework.web.api.timezone;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class RequestDateTimeFormatter {

    private static final ZoneId UTC = ZoneOffset.UTC;

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private RequestDateTimeFormatter() {
    }

    public static String format(LocalDateTime utcDateTime) {
        if (utcDateTime == null) {
            return null;
        }
        return utcDateTime.atZone(UTC)
                .withZoneSameInstant(RequestTimeZoneContext.getCurrentZoneId())
                .toLocalDateTime()
                .format(DATETIME_FORMATTER);
    }

    public static LocalDateTime toUtcStartOfDay(LocalDate localDate) {
        return localDate.atStartOfDay(RequestTimeZoneContext.getCurrentZoneId())
                .withZoneSameInstant(UTC)
                .toLocalDateTime();
    }

    public static LocalDateTime toUtcEndOfDay(LocalDate localDate) {
        return localDate.plusDays(1)
                .atStartOfDay(RequestTimeZoneContext.getCurrentZoneId())
                .withZoneSameInstant(UTC)
                .toLocalDateTime()
                .minusSeconds(1);
    }

    public static LocalDateTime parseToUtc(String localDateTimeText) {
        if (localDateTimeText == null || localDateTimeText.trim().isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(localDateTimeText.trim(), DATETIME_FORMATTER)
                .atZone(RequestTimeZoneContext.getCurrentZoneId())
                .withZoneSameInstant(UTC)
                .toLocalDateTime();
    }
}
