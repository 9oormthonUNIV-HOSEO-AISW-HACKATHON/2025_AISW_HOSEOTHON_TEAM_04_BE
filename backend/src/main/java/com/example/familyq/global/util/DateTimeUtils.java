package com.example.familyq.global.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class DateTimeUtils {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    private DateTimeUtils() {
    }

    public static LocalDate today() {
        return LocalDate.now(ZONE_ID);
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(ZONE_ID);
    }

    public static LocalDateTime startOfDay(LocalDate date) {
        ZonedDateTime zonedDateTime = date.atStartOfDay(ZONE_ID);
        return zonedDateTime.toLocalDateTime();
    }
}
