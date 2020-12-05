package com.optum.sourcehawk.exec;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.optum.sourcehawk.core.configuration.SourcehawkConfiguration;
import com.optum.sourcehawk.core.scan.FlattenConfigResult;
import com.optum.sourcehawk.core.utils.StringUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Entry point for executing Sourcehawk flatten command
 *
 * @author Christian Oestreich
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FlattenConfigExecutor {

    private static final ObjectWriter WRITER_WITH_DEFAULT_PRETTY_PRINTER = Sourcehawk.YAML_FORMATTER.writerWithDefaultPrettyPrinter();

    /**
     * Run the flatten config based on the provided configuration file location
     *
     * @param configurationFileLocation the sourcehawk configuration location
     * @return the flatten config result
     */
    public static FlattenConfigResult flatten(final String configurationFileLocation) {
        if (StringUtils.isBlankOrEmpty(configurationFileLocation)) {
            return FlattenConfigResult.error(String.format("Configuration file %s not found or invalid", configurationFileLocation));
        }
        return ConfigurationReader.readConfiguration(Paths.get("."), configurationFileLocation)
                .map(sourcehawkConfiguration -> executeFlatten(configurationFileLocation, sourcehawkConfiguration))
                .orElseGet(() -> FlattenConfigResult.error(String.format("Configuration file %s not found or invalid", configurationFileLocation)));
    }

    /**
     * Execute the flatten iterating over all configuration locations and aggregate the results
     *
     * @param configurationFileLocation the configuration location
     * @param sourcehawkConfiguration   the flattened configuration object
     * @return the aggregated flatten result
     */
    private static FlattenConfigResult executeFlatten(final String configurationFileLocation,
                                                      final SourcehawkConfiguration sourcehawkConfiguration) {
        byte[] content;
        try {
            content = WRITER_WITH_DEFAULT_PRETTY_PRINTER.writeValueAsBytes(sourcehawkConfiguration);
        } catch (IOException e) {
            return handleException(configurationFileLocation, e);
        }

        return FlattenConfigResult.success(content);
    }

    /**
     * Handle exceptions from Serialization
     *
     * @param configurationFileLocation the configuration location
     * @param e                         the exception
     * @return an error flatten result
     */
    private static FlattenConfigResult handleException(final String configurationFileLocation, final IOException e) {
        val message = String.format("Error flattening sourcehawk configuration at %s with error: %s", configurationFileLocation, e.getMessage());
        return FlattenConfigResult.error(message);
    }
}
