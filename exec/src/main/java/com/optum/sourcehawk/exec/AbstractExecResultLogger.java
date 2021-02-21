package com.optum.sourcehawk.exec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.optum.sourcehawk.core.constants.SourcehawkConstants;
import com.optum.sourcehawk.core.data.Pair;
import com.optum.sourcehawk.core.data.Severity;
import com.optum.sourcehawk.core.data.Verbosity;
import lombok.SneakyThrows;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.util.Collection;

/**
 * A generic logger for logging results
 *
 * @param <T> the type of result to log
 * @author Brian Wyka
 */
public abstract class AbstractExecResultLogger<T> {

    /**
     * The key for the repository file path in the formatted log message
     */
    private static final String KEY_REPOSITORY_FILE_PATH = "repositoryFilePath";

    /**
     * The JSON writer
     */
    protected static final ObjectWriter JSON_WRITER = new ObjectMapper().writerWithDefaultPrettyPrinter();

    /**
     * Log the result in the specified format
     *
     * @param result the result
     * @param execOptions the scan options
     */
    @SuppressWarnings("squid:S2629")
    public void log(final T result, final ExecOptions execOptions) {
        switch (execOptions.getOutputFormat()) {
            case JSON:
                ExecLoggers.CONSOLE_RAW.info(formatJson(result));
                break;
            case MARKDOWN:
                ExecLoggers.CONSOLE_RAW.info(formatMarkdown(result, execOptions.getVerbosity()));
                break;
            case CONSOLE:
                if (execOptions.getVerbosity() == Verbosity.HIGH) {
                    ExecLoggers.HIGHLIGHT.info(String.format(">_ %s", String.join(" ", SourcehawkConstants.NAME.toUpperCase().split(""))));
                    ExecLoggers.CONSOLE_RAW.info(execOptions.toString());
                }
                val consoleSummary = formatTextSummary(result);
                logBasedOnSeverity(ExecLoggers.CONSOLE_RAW, consoleSummary.getLeft(), consoleSummary.getRight());
                logMessages(execOptions.getVerbosity(), result, ExecLoggers.MESSAGE_ANSI);
                break;
            case TEXT:
            default:
                val textSummary = formatTextSummary(result);
                logBasedOnSeverity(ExecLoggers.CONSOLE_RAW, textSummary.getLeft(), textSummary.getRight());
                logMessages(execOptions.getVerbosity(), result, ExecLoggers.MESSAGE);
                break;
        }
    }

    /**
     * Log the messages
     *
     * @param verbosity the logging verbosity
     * @param result the result
     * @param messageLogger the message logger
     */
    private void logMessages(final Verbosity verbosity, final T result, final Logger messageLogger) {
        if (verbosity.getLevel() >= Verbosity.MEDIUM.getLevel()) {
            formatTextMessages(result).forEach(pair -> logMessage(messageLogger, pair.getLeft(), pair.getRight().getLeft(), pair.getRight().getRight()));
        }
    }

    /**
     * Derive the logger level from the message descriptor
     *
     * @param logger the logger to use
     * @param severity the severity of the message
     * @param repositoryFilePath the repository file path
     * @param message the message to log
     */
    private static void logMessage(final Logger logger, final Severity severity, final String repositoryFilePath, final String message) {
        MDC.put(KEY_REPOSITORY_FILE_PATH, repositoryFilePath);
        logBasedOnSeverity(logger, severity, message);
    }

    /**
     * Log based on severity
     *
     * @param logger the logger to use
     * @param severity the severity of the message
     * @param message the message to log
     */
    private static void logBasedOnSeverity(final Logger logger, final Severity severity, final String message) {
        switch (severity) {
            case ERROR:
                logger.error(message);
                break;
            case WARNING:
                logger.warn(message);
                break;
            case INFO:
            default:
                logger.info(message);
                break;
        }
    }

    /**
     * Format the summary for plain text output format
     *
     * @param result the result
     * @return the formatted text output for the summary
     */
    protected abstract Pair<Severity, String> formatTextSummary(final T result);

    /**
     * Format the result messages for text output
     *
     * @param result the result
     * @return the formatted text log statements
     */
    protected abstract Collection<Pair<Severity, Pair<String, String>>> formatTextMessages(final T result);

    /**
     * Format the scan result for JSON output format
     *
     * @param result the result
     * @return the formatted JSON output
     */
    @SneakyThrows
    protected String formatJson(final T result) {
        return JSON_WRITER.writeValueAsString(result);
    }

    /**
     * Format the result for markdown output format
     *
     * @param result the result
     * @param verbosity the output verbosity
     * @return the formatted markdown output
     */
    protected abstract String formatMarkdown(final T result, final Verbosity verbosity);
    
}
