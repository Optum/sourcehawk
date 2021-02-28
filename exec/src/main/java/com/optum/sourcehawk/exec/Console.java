package com.optum.sourcehawk.exec;

import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * Console logging utility
 *
 * @author Brian Wyka
 */
@UtilityClass
@SuppressWarnings("squid:S106")
public class Console {

    private static final String RESET = "\u001B[0m";
    private static final String	BOLD = "\u001B[1m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
    private static final boolean WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    /**
     * Console logging to standard out
     *
     * @author Brian Wyka
     */
    @UtilityClass
    public static class Out {

        /**
         * Log a message to standard out
         *
         * @param message the message to log to console
         */
        // CHECKSTYLE:OFF
        public void log(final String message) {
            System.out.println(message);
        }
        // CHECKSTYLE:ON

        /**
         * Log an INFO message to standard out in for the format "[INFO]  {@code context} :: {@code message}"
         *
         * @param context message context
         * @param message the message to log to console
         * @param args the message args
         */
        public void contextualInfo(final String context, final String message, final Object... args) {
            contextualLog(BLUE, "[INFO]  ", context, message, args);
        }

        /**
         * Log a WARNING message to standard out in for the format "[WARN]  {@code context} :: {@code message}"
         *
         * @param context message context
         * @param message the message to log to console
         * @param args the message args
         */
        public void contextualWarn(final String context, final String message, final Object... args) {
            contextualLog(YELLOW, "[WARN]  ", context, message, args);
        }

        /**
         * Log an ERROR message to standard out in for the format "[ERROR] {@code context} :: {@code message}"
         *
         * @param context message contextA
         * @param message the message to log to console
         * @param args the message args
         */
        public void contextualError(final String context, final String message, final Object... args) {
            contextualLog(RED, "[ERROR] ", context, message, args);
        }

        /**
         * Log the given message with context and color and with the specified level
         *
         * @param levelColor the color to log level with
         * @param level the level
         * @param context the context
         * @param message the message
         * @param args the message args
         */
        // CHECKSTYLE:OFF
        private void contextualLog(final String levelColor, final String level, final String context, final String message, final Object... args) {
            val stylizedMessage = message.replace("[", BOLD + MAGENTA + "[").replace("]", "]" + RESET);
            System.out.println(ansi(levelColor, level) + ansi(CYAN, context) + " :: " + String.format(stylizedMessage, args));
        }
        // CHECKSTYLE:ON

    }

    /**
     * Console logging to standard err
     *
     * @author Brian Wyka
     */
    @UtilityClass
    public static class Err {

        /**
         * Log a message to standard err
         *
         * @param message the message to log to console
         * @param args the message args
         */
        // CHECKSTYLE:OFF
        public void log(final String message, final Object... args) {
            System.err.println(String.format(message, args));
        }
        // CHECKSTYLE:ON

        /**
         * Log an error message to standard err
         *
         * @param message the message to log to console
         * @param args the message args
         */
        // CHECKSTYLE:OFF
        public void error(final String message, final Object... args) {
            System.err.println(ansi(RED, String.format(message, args)));
        }
        // CHECKSTYLE:ON

    }

    /**
     * GEt the ansi encoded string, if not on Windows
     *
     * @param ansi the ansi code
     * @param subject the subject string
     * @return the ansi-encoded string
     */
    private String ansi(final String ansi, final String subject) {
        if (WINDOWS) {
            return subject;
        }
        return ansi + subject + RESET;
    }

}
