package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for logging messages with timestamps.
 * <p>
 * Provides static methods to log informational and error messages,
 * automatically prefixing them with the current date and time.
 * </p>
 */
public class LoggerUtil {
    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Logs an informational message to standard output with a timestamp.
     *
     * @param message The message to log.
     */
    public static void log(String message) {
        String ts = LocalDateTime.now().format(FORMATTER);
        System.out.println("[" + ts + "] INFO: " + message);
    }

    /**
     * Logs an error message to standard error with a timestamp.
     *
     * @param message The error message to log.
     */
    public static void logError(String message) {
        String ts = LocalDateTime.now().format(FORMATTER);
        System.err.println("[" + ts + "] ERROR: " + message);
    }
}
