package com.optum.sourcehawk.exec.scan;

import com.optum.sourcehawk.core.constants.SourcehawkConstants;
import com.optum.sourcehawk.core.data.OutputFormat;
import com.optum.sourcehawk.core.data.Pair;
import com.optum.sourcehawk.core.data.Severity;
import com.optum.sourcehawk.core.data.Verbosity;
import com.optum.sourcehawk.core.result.ScanResult;
import com.optum.sourcehawk.exec.AbstractExecResultLogger;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * A logger for scan results
 *
 * @see ScanResult
 * @see OutputFormat
 *
 * @author Brian Wyka
 */
@NoArgsConstructor(staticName = "create")
public class ScanResultLogger extends AbstractExecResultLogger<ScanResult> {

    private static final String MESSAGE_PASSED = "Scan passed without any errors";
    private static final String MESSAGE_PASSED_WITH_WARNINGS = "Scan passed. Errors: 0, Warning(s): %d";
    private static final String MESSAGE_FAILED = "Scan resulted in failure. Error(s): %d, Warning(s): %d";

    /** {@inheritDoc} */
    @Override
    protected Pair<Severity, String> formatTextSummary(final ScanResult scanResult) {
        if (scanResult.isPassedWithNoWarnings()) {
            return Pair.of(Severity.INFO, MESSAGE_PASSED);
        } else if (scanResult.isPassed()) {
            return Pair.of(Severity.WARNING, String.format(MESSAGE_PASSED_WITH_WARNINGS, scanResult.getWarningCount()));
        }
        return Pair.of(Severity.ERROR, String.format(MESSAGE_FAILED, scanResult.getErrorCount(), scanResult.getWarningCount()));
    }

    /** {@inheritDoc} */
    @Override
    protected Collection<Pair<Severity, Pair<String, String>>> formatTextMessages(final ScanResult scanResult) {
        return scanResult.getMessages()
                .values()
                .stream()
                .flatMap(Collection::stream)
                .map(md -> Pair.of(Severity.valueOf(md.getSeverity()), Pair.of(md.getRepositoryPath(), md.getMessage())))
                .collect(Collectors.toList());
    }

    /** {@inheritDoc} */
    @Override
    protected String formatMarkdown(final ScanResult scanResult, final Verbosity verbosity) {
        val markdownBuilder = new StringBuilder();
        markdownBuilder.append(String.format("## %s", SourcehawkConstants.NAME))
                .append(System.lineSeparator())
                .append(System.lineSeparator());
        if (scanResult.isPassedWithNoWarnings()) {
            markdownBuilder.append(MESSAGE_PASSED);
        } else if (verbosity.getLevel() >= Verbosity.MEDIUM.getLevel()) {
            if (scanResult.isPassed()) {
                markdownBuilder.append(String.format(MESSAGE_PASSED_WITH_WARNINGS, scanResult.getWarningCount()));
            } else {
                markdownBuilder.append(String.format(MESSAGE_FAILED, scanResult.getErrorCount(), scanResult.getWarningCount()));
            }
            markdownBuilder.append(System.lineSeparator())
                    .append(System.lineSeparator())
                    .append("### Results")
                    .append(System.lineSeparator())
                    .append(System.lineSeparator())
                    .append("* ")
                    .append(scanResult.getFormattedMessages().stream().collect(Collectors.joining(System.lineSeparator() + "* ")))
                    .append(System.lineSeparator());
        }
        return markdownBuilder.toString();
    }

}
