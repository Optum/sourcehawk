package com.optum.sourcehawk.exec.fix;

import com.optum.sourcehawk.core.constants.SourcehawkConstants;
import com.optum.sourcehawk.core.data.OutputFormat;
import com.optum.sourcehawk.core.data.Pair;
import com.optum.sourcehawk.core.data.Severity;
import com.optum.sourcehawk.core.data.Verbosity;
import com.optum.sourcehawk.core.result.FixResult;
import com.optum.sourcehawk.exec.AbstractExecResultLogger;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * A logger for fix results
 *
 * @see FixResult
 * @see OutputFormat
 *
 * @author Brian Wyka
 */
@AllArgsConstructor(staticName = "create")
public class FixResultLogger extends AbstractExecResultLogger<FixResult> {

    private static final String MESSAGE_APPLIED = "%d Fix(es) applied without any errors";
    private static final String MESSAGE_APPLIED_DRY_RUN = "%d Fix(es) would have been applied without any errors";
    private static final String MESSAGE_APPLIED_WITH_ERRORS = "Fixes applied. Error(s): %d";
    private static final String MESSAGE_APPLIED_WITH_ERRORS_DRY_RUN = "%d Fix(es) would have been applied. Error(s): %d";
    private static final String MESSAGE_ERROR = "Fixes unable to be applied. Error(s): %d";
    private static final String MESSAGE_ERROR_DRY_RUN = "Fixes would not have been applied. Error(s): %d";

    /**
     * Whether or not this fix result is a dry run
     */
    private final boolean dryRun;

    /** {@inheritDoc} */
    @Override
    protected Pair<Severity, String> formatTextSummary(final FixResult fixResult) {
        if (fixResult.isFixesApplied() && !fixResult.isError()) {
            return Pair.of(Severity.INFO, String.format(dryRun ? MESSAGE_APPLIED_DRY_RUN : MESSAGE_APPLIED, fixResult.getFixCount()));
        } else if (fixResult.isFixesApplied()) {
            val message = String.format(dryRun ? MESSAGE_APPLIED_WITH_ERRORS_DRY_RUN : MESSAGE_APPLIED_WITH_ERRORS, fixResult.getFixCount(), fixResult.getErrorCount());
            return Pair.of(Severity.WARNING, message);
        }
        return Pair.of(Severity.ERROR, String.format(dryRun ? MESSAGE_ERROR_DRY_RUN : MESSAGE_ERROR, fixResult.getErrorCount()));
    }

    /** {@inheritDoc} */
    @Override
    protected Collection<Pair<Severity, Pair<String, String>>> formatTextMessages(final FixResult fixResult) {
        return fixResult.getMessages()
                .values()
                .stream()
                .flatMap(Collection::stream)
                .map(messageDescriptor -> Pair.of(deriveSeverity(fixResult), Pair.of(messageDescriptor.getRepositoryPath(), messageDescriptor.getMessage())))
                .collect(Collectors.toList());
    }

    /** {@inheritDoc} */
    @Override
    protected String formatMarkdown(final FixResult fixResult, final Verbosity verbosity) {
        val markdownBuilder = new StringBuilder();
        markdownBuilder.append(String.format("## %s", SourcehawkConstants.NAME))
                .append(System.lineSeparator())
                .append(System.lineSeparator());
        if (fixResult.isFixesApplied() && !fixResult.isError()) {
            markdownBuilder.append(String.format(dryRun ? MESSAGE_APPLIED_DRY_RUN : MESSAGE_APPLIED, fixResult.getFixCount()));
        } else if (fixResult.isFixesApplied()) {
            val message = String.format(dryRun ? MESSAGE_APPLIED_WITH_ERRORS_DRY_RUN : MESSAGE_APPLIED_WITH_ERRORS, fixResult.getFixCount(), fixResult.getErrorCount());
            markdownBuilder.append(message);
        } else {
            markdownBuilder.append(String.format(dryRun ? MESSAGE_ERROR_DRY_RUN : MESSAGE_ERROR, fixResult.getErrorCount()));
        }
        if (verbosity.getLevel() >= Verbosity.MEDIUM.getLevel()) {
            markdownBuilder.append(System.lineSeparator())
                    .append(System.lineSeparator())
                    .append("### Results")
                    .append(System.lineSeparator())
                    .append(System.lineSeparator())
                    .append("* ")
                    .append(fixResult.getFormattedMessages().stream().collect(Collectors.joining(System.lineSeparator() + "* ")))
                    .append(System.lineSeparator());
        }
        return markdownBuilder.toString();
    }

    /**
     * Derive the severity from the fix result
     *
     * @param fixResult the fix result
     * @return the severity
     */
    private static Severity deriveSeverity(final FixResult fixResult) {
        if (fixResult.isError()) {
            return Severity.ERROR;
        }
        return Severity.INFO;
    }

}
