package com.optum.sourcehawk.exec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.optum.sourcehawk.core.data.Pair;
import com.optum.sourcehawk.core.data.Severity;
import com.optum.sourcehawk.core.data.Verbosity;
import lombok.SneakyThrows;
import lombok.val;

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
                Console.Out.log(formatJson(result));
                break;
            case MARKDOWN:
                Console.Out.log(formatMarkdown(result, execOptions.getVerbosity()));
                break;
            case TEXT:
            default:
                val textSummary = formatTextSummary(result);
                Console.Out.log(textSummary.getRight());
                logMessages(execOptions.getVerbosity(), result);
                break;
        }
    }

    /**
     * Log the messages
     *
     * @param verbosity the logging verbosity
     * @param result the result
     */
    private void logMessages(final Verbosity verbosity, final T result) {
        if (verbosity.getLevel() >= Verbosity.MEDIUM.getLevel()) {
            formatTextMessages(result).forEach(pair -> logWithSeverityAndContext(pair.getLeft(), pair.getRight().getLeft(), pair.getRight().getRight()));
        }
    }

    /**
     * Log the given message with severity and context
     *
     * @param severity the severity of the message
     * @param context the message context
     * @param message the message to log
     */
    private static void logWithSeverityAndContext(final Severity severity, final String context, final String message) {
        switch (severity) {
            case ERROR:
                Console.Out.contextualError(context, message);
                break;
            case WARNING:
                Console.Out.contextualWarn(context, message);
                break;
            case INFO:
            default:
                Console.Out.contextualInfo(context, message);
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
