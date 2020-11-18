package com.optum.sourcehawk.exec;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.optum.sourcehawk.configuration.SourcehawkConfiguration;
import com.optum.sourcehawk.core.scan.FlattenResult;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.IOException;

/**
 * Entry point into executing flattens
 *
 * @author Christian Oestreich
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FlattenExecutor {

    public static final ObjectWriter WRITER_WITH_DEFAULT_PRETTY_PRINTER = Sourcehawk.YAML_FORMATTER.writerWithDefaultPrettyPrinter();

    /**
     * Run the fix based on the provided options
     *
     * @param execOptions the scan options
     * @return the fix result
     */
    public static FlattenResult flatten(final ExecOptions execOptions) {
        return ConfigurationReader.readConfiguration(execOptions.getRepositoryRoot(), execOptions.getConfigurationFileLocation())
                .map(sourcehawkConfiguration -> executeFlatten(execOptions, sourcehawkConfiguration))
                .orElseGet(() -> FlattenResultFactory.error(execOptions.getConfigurationFileLocation(), "Configuration file not found or invalid"));
    }

    /**
     * Execute the scan.  Iterate over all file protocols, and each enforcer within the file protocol and aggregate the results
     *
     * @param execOptions             the scan options
     * @param sourcehawkConfiguration the configuration
     * @return the aggregated scan result
     */
    private static FlattenResult executeFlatten(final ExecOptions execOptions,
                                                final SourcehawkConfiguration sourcehawkConfiguration) {
        byte[] content;
        try {
            content = WRITER_WITH_DEFAULT_PRETTY_PRINTER.writeValueAsBytes(sourcehawkConfiguration);
        } catch (IOException e) {
            return handleException(execOptions, e);
        }

        return FlattenResultFactory.success(content);
    }

    /**
     * Handle exceptions from Serialization
     *
     * @param execOptions the scan options
     * @param e           the exception
     * @return an error flatten result
     */
    private static FlattenResult handleException(ExecOptions execOptions, IOException e) {
        val message = String.format("Error flattening file protocol: %s", e.getMessage());
        return FlattenResultFactory.error(execOptions.getConfigurationFileLocation(), message);
    }

}
