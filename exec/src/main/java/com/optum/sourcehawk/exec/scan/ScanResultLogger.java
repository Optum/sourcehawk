package com.optum.sourcehawk.exec.scan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.optum.sourcehawk.core.constants.SourcehawkConstants;
import com.optum.sourcehawk.core.scan.OutputFormat;
import com.optum.sourcehawk.core.scan.ScanResult;
import com.optum.sourcehawk.core.scan.Severity;
import com.optum.sourcehawk.core.scan.Verbosity;
import com.optum.sourcehawk.exec.ExecLoggers;
import com.optum.sourcehawk.exec.ExecOptions;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A logger for scan results
 *
 * @see ScanResult
 * @see OutputFormat
 *
 * @author Brian Wyka
 */
@UtilityClass
public class ScanResultLogger {

    /**
     * The JSON writer
     */
    private final ObjectWriter JSON_WRITER = new ObjectMapper().writerWithDefaultPrettyPrinter();

    private final String MESSAGE_PASSED = "Scan passed without any errors";
    private final String MESSAGE_PASSED_WITH_WARNINGS = "Scan passed. Errors: 0, Warning(s): %d";
    private final String MESSAGE_FAILED_TEMPLATE = "Scan resulted in failure. Error(s): %d, Warning(s): %d";

    /**
     * Log the result of the scan in the specified format
     *
     * @param scanResult the scan result
     * @param execOptions the scan options
     */
    @SuppressWarnings("squid:S2629")
    public void log(final ScanResult scanResult, final ExecOptions execOptions) {
        switch (execOptions.getOutputFormat()) {
            case JSON:
                ExecLoggers.CONSOLE_RAW.info(formatJson(scanResult));
                break;
            case MARKDOWN:
                ExecLoggers.CONSOLE_RAW.info(formatMarkdown(scanResult, execOptions.getVerbosity()));
                break;
            case CONSOLE:
                if (execOptions.getVerbosity() == Verbosity.HIGH) {
                    ExecLoggers.HIGHLIGHT.info(String.format(">_ %s", String.join(" ", SourcehawkConstants.NAME.toUpperCase().split(""))));
                    ExecLoggers.CONSOLE_RAW.info(execOptions.toString());
                }
                handleTextualOutput(scanResult, execOptions, ExecLoggers.MESSAGE_ANSI);
                break;
            case TEXT:
            default:
                handleTextualOutput(scanResult, execOptions, ExecLoggers.MESSAGE);
                break;
        }
    }

    /**
     * Format the scan result for JSON output format
     *
     * @param scanResult the scan result
     * @return the formatted JSON output
     */
    private String formatJson(final ScanResult scanResult) {
        try {
            return JSON_WRITER.writeValueAsString(scanResult);
        } catch (final IOException e) {
            return String.format("{\"passed\": %s,\"formattedMessages\":[\"Error serializing scan result: %s\"]}", scanResult.isPassed(), e.getMessage());
        }
    }

    /**
     * Handle textual output
     *
     * @param scanResult the scan result
     * @param execOptions the scan options
     * @param scanMessageLogger the scan message logger
     */
    private void handleTextualOutput(final ScanResult scanResult, final ExecOptions execOptions, final Logger scanMessageLogger) {
        val formattedText = formatText(scanResult);
        if (scanResult.isPassedWithNoWarnings()) {
            ExecLoggers.CONSOLE_RAW.info(formattedText);
        } else if (scanResult.isPassed()) {
            ExecLoggers.CONSOLE_RAW.warn(formattedText);
        } else {
            ExecLoggers.CONSOLE_RAW.error(formattedText);
        }
        if (execOptions.getVerbosity() == Verbosity.MEDIUM || execOptions.getVerbosity() == Verbosity.HIGH) {
            logMessages(scanResult.getMessages(), scanMessageLogger);
        }
    }

    /**
     * Format the scan result for plain text output format
     *
     * @param scanResult the scan result
     * @return the formatted text output
     */
    private String formatText(final ScanResult scanResult) {
        if (scanResult.isPassedWithNoWarnings()) {
            return MESSAGE_PASSED;
        } else if (scanResult.isPassed()) {
            return String.format(MESSAGE_PASSED_WITH_WARNINGS, scanResult.getWarningCount());
        }
        return String.format(MESSAGE_FAILED_TEMPLATE, scanResult.getErrorCount(), scanResult.getWarningCount());
    }

    /**
     * Format the scan result for markdown output format
     *
     * @param scanResult the scan result
     * @param verbosity the output verbosity
     * @return the formatted markdown output
     */
    private String formatMarkdown(final ScanResult scanResult, final Verbosity verbosity) {
        val markdownBuilder = new StringBuilder();
        markdownBuilder.append("## Sourcehawk Scan")
                .append(System.lineSeparator())
                .append(System.lineSeparator());
        if (scanResult.isPassedWithNoWarnings()) {
            markdownBuilder.append(MESSAGE_PASSED);
        } else if (verbosity.getLevel() >= Verbosity.MEDIUM.getLevel()) {
            if (scanResult.isPassed()) {
                markdownBuilder.append(String.format(MESSAGE_PASSED_WITH_WARNINGS, scanResult.getWarningCount()));
            } else {
                markdownBuilder.append(String.format(MESSAGE_FAILED_TEMPLATE, scanResult.getErrorCount(), scanResult.getWarningCount()));
            }
            markdownBuilder.append(System.lineSeparator())
                    .append(System.lineSeparator());
            markdownBuilder.append("### Results")
                    .append(System.lineSeparator())
                    .append(System.lineSeparator());
            scanResult.getFormattedMessages().stream()
                    .map(message -> message + System.lineSeparator())
                    .forEach(markdownBuilder::append);
        }
        return markdownBuilder.toString();
    }

    /**
     * Log the scan messages if appropriate
     *
     * @param messages the scan result messages'
     * @param scanMessageLogger the scan message logger
     */
    private void logMessages(final Map<String, Collection<ScanResult.MessageDescriptor>> messages, final Logger scanMessageLogger) {
        ExecLoggers.CONSOLE_RAW.error("");
        for (val messageEntry: messages.entrySet()) {
            MDC.put("repositoryFilePath", messageEntry.getKey());
            for (val messageDescriptor: messageEntry.getValue()) {
                final Consumer<String> logger;
                switch (Severity.valueOf(messageDescriptor.getSeverity())) {
                    case ERROR:
                        logger = scanMessageLogger::error;
                        break;
                    case WARNING:
                        logger = scanMessageLogger::warn;
                        break;
                    default: logger = scanMessageLogger::info;
                }
                logger.accept(messageDescriptor.getMessage());
            }
        }
    }

}
