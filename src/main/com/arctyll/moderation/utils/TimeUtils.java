package com.arctyll.moderation.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils {

    public static long parseDuration(String duration) {
        if (duration == null || duration.isEmpty()) return 0;

        try {
            char unit = duration.charAt(duration.length() - 1);
            int amount = Integer.parseInt(duration.substring(0, duration.length() - 1));

            switch (Character.toLowerCase(unit)) {
                case 's': return amount * 1000L;
                case 'm': return amount * 60L * 1000L;
                case 'h': return amount * 60L * 60L * 1000L;
                case 'd': return amount * 24L * 60L * 60L * 1000L;
                case 'w': return amount * 7L * 24L * 60L * 60L * 1000L;
                case 'y': return amount * 365L * 24L * 60L * 60L * 1000L;
                default: return 0;
            }
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String formatDuration(long millis) {
        if (millis <= 0) return "0 seconds";

        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;
        long years = days / 365;

        if (years > 0) {
            return years + " year" + (years != 1 ? "s" : "");
        } else if (weeks > 0) {
            return weeks + " week" + (weeks != 1 ? "s" : "");
        } else if (days > 0) {
            return days + " day" + (days != 1 ? "s" : "");
        } else if (hours > 0) {
            return hours + " hour" + (hours != 1 ? "s" : "");
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes != 1 ? "s" : "");
        } else {
            return seconds + " second" + (seconds != 1 ? "s" : "");
        }
    }

    public static String formatExpireDate(long timestamp) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(new Date(timestamp));
    }

    public static String formatRelativeTime(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = timestamp - now;

        if (diff <= 0) {
            return "now";
        }

        return formatDuration(diff) + " from now";
    }

    public static String formatTimeAgo(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff <= 0) {
            return "just now";
        }

        return formatDuration(diff) + " ago";
    }
}
