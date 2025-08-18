package com.insiders.util;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TimeUtils {

    public static String getRelativeTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isEmpty()) {
            return "unknown time";
        }

        try {
            LocalDateTime dateTime;

            if (dateTimeString.endsWith("Z") || dateTimeString.contains("+") || dateTimeString.matches(".*[+-]\\d{2}:\\d{2}$")) {
                ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTimeString);
                dateTime = zonedDateTime.toLocalDateTime();
            } else {
                LocalDateTime utcDateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                ZonedDateTime utcZoned = utcDateTime.atZone(ZoneId.of("UTC"));
                dateTime = utcZoned.withZoneSameInstant(ZoneId.of("Europe/Bucharest")).toLocalDateTime();
            }

            LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Bucharest"));

            long minutes = ChronoUnit.MINUTES.between(dateTime, now);
            long hours = ChronoUnit.HOURS.between(dateTime, now);
            long days = ChronoUnit.DAYS.between(dateTime, now);
            long weeks = ChronoUnit.WEEKS.between(dateTime, now);
            long months = ChronoUnit.MONTHS.between(dateTime, now);
            long years = ChronoUnit.YEARS.between(dateTime, now);

            if (minutes < 1) {
                return "just now";
            } else if (minutes < 60) {
                return minutes + (minutes == 1 ? " min ago" : " mins ago");
            } else if (hours < 24) {
                return hours + (hours == 1 ? " hour ago" : " hours ago");
            } else if (days < 7) {
                return days + (days == 1 ? " day ago" : " days ago");
            } else if (weeks < 4) {
                return weeks + (weeks == 1 ? " week ago" : " weeks ago");
            } else if (months < 12) {
                return months + (months == 1 ? " month ago" : " months ago");
            } else {
                return years + (years == 1 ? " year ago" : " years ago");
            }
        } catch (Exception e) {
            return dateTimeString;
        }
    }
}
