package com.optum.sourcehawk.exec;

import com.optum.sourcehawk.core.constants.SourcehawkConstants;
import com.optum.sourcehawk.core.repository.LocalRepositoryFileWriter;
import com.optum.sourcehawk.core.scan.FixResult;
import com.optum.sourcehawk.core.scan.FlattenResult;
import com.optum.sourcehawk.core.scan.OutputFormat;
import com.optum.sourcehawk.core.scan.ScanResult;
import com.optum.sourcehawk.core.scan.Verbosity;
import com.optum.sourcehawk.core.utils.StringUtils;
import lombok.experimental.UtilityClass;

import java.io.IOException;

/**
 * A logger for flatten results
 *
 * @author Christian Oestreich
 * @see ScanResult
 * @see FixResult
 * @see OutputFormat
 */
@UtilityClass
class FlattenResultLogger {

    private static final String SOURCEHAWK_FLATTENED_YML = "sourcehawk-flattened.yml";

    /**
     * Log the result of the scan in the specified format
     *
     * @param flattenResult the scan result
     * @param execOptions   the scan options
     */
    @SuppressWarnings("squid:S2629")
    void log(final FlattenResult flattenResult, final ExecOptions execOptions, final String output, final boolean dryRun) {
        if (dryRun) {
            if (Sourcehawk.HIGHLIGHT_LOGGER.isInfoEnabled() && execOptions.getVerbosity() == Verbosity.HIGH) {
                Sourcehawk.HIGHLIGHT_LOGGER.info(generateHeader());
            }
            if (execOptions.getVerbosity() == Verbosity.HIGH) {
                Sourcehawk.CONSOLE_RAW_LOGGER.info(formatExecOptions(execOptions));
            }
            handleDryRunOutput(flattenResult);
        } else {
            try {
                String repositoryFilePath = StringUtils.defaultString(output, SOURCEHAWK_FLATTENED_YML);
                LocalRepositoryFileWriter.writer().write(repositoryFilePath, flattenResult.getContent());
                Sourcehawk.CONSOLE_RAW_LOGGER.info(String.join("/n", flattenResult.getFormattedMessages()));
                Sourcehawk.CONSOLE_RAW_LOGGER.info("Output to {}", repositoryFilePath);
            } catch (IOException e) {
                Sourcehawk.CONSOLE_RAW_LOGGER.error("Could not flatten file due to {}", e.getMessage());
            }
        }
    }

    /**
     * Handle textual output
     *
     * @param flattenResult the scan result
     */
    private void handleDryRunOutput(final FlattenResult flattenResult) {
        if (flattenResult != null && flattenResult.getContent() != null) {
            Sourcehawk.CONSOLE_RAW_LOGGER.info(new String(flattenResult.getContent()));
        } else {
            Sourcehawk.CONSOLE_RAW_LOGGER.error("No flattened file produced!");
        }
    }

    /**
     * Generate the header for plain text output
     *
     * @return the generated header
     */
    private String generateHeader() {
        return String.format(">_ %s", String.join(" ", SourcehawkConstants.NAME.toUpperCase().split("")));
    }

    /**
     * Format the exec options for plain text output
     *
     * @param execOptions the exec options
     * @return the formatted exec options
     */
    private String formatExecOptions(final ExecOptions execOptions) {
        return System.lineSeparator()
                + "Repository Root... " + execOptions.getRepositoryRoot() + System.lineSeparator()
                + "Config File....... " + execOptions.getConfigurationFileLocation() + System.lineSeparator()
                + "Verbosity......... " + execOptions.getVerbosity() + System.lineSeparator()
                + "Output Format..... " + execOptions.getOutputFormat() + System.lineSeparator()
                + "Fail on Warnings.. " + execOptions.isFailOnWarnings() + System.lineSeparator();
    }
}
