package me.simplyran.simplymines.utils;

public final class TimeUtils {

    private TimeUtils() {}

    /** "mm:ss", negative values are clamped to 00:00. */
    public static String formatMMSS(long totalSeconds) {
        long clamped = Math.max(0, totalSeconds);
        return String.format("%02d:%02d", clamped / 60, clamped % 60);
    }

    /** "h:mm:ss" once there is at least an hour left, otherwise "mm:ss". */
    public static String formatHMS(long totalSeconds) {
        long clamped = Math.max(0, totalSeconds);
        long hours = clamped / 3600;
        long minutes = (clamped % 3600) / 60;
        long seconds = clamped % 60;
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%02d:%02d", minutes, seconds);
    }
}
