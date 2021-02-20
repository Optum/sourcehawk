package com.optum.sourcehawk.exec.config;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.optum.sourcehawk.core.configuration.SourcehawkConfiguration;
import com.optum.sourcehawk.core.result.FlattenConfigResult;
import com.optum.sourcehawk.core.utils.StringUtils;
import com.optum.sourcehawk.exec.ConfigurationReader;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Entry point for executing Sourcehawk flatten command
 *
 * @author Christian Oestreich
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FlattenConfigExecutor {

    /**
     * A YAML Mapper with pretty printing enabled
     */
    private static final ObjectWriter YAML_WRITER = new YAMLMapper()
            .configure(YAMLGenerator.Feature.WRITE_DOC_START_MARKER, false)
            .setPropertyNamingStrategy(new PropertyNamingStrategy.KebabCaseStrategy())
            .writerWithDefaultPrettyPrinter();

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
    private static FlattenConfigResult executeFlatten(final String configurationFileLocation, final SourcehawkConfiguration sourcehawkConfiguration) {
        try {
            return FlattenConfigResult.success(YAML_WRITER.writeValueAsBytes(sourcehawkConfiguration));
        } catch (final IOException e) {
            return handleException(configurationFileLocation, e);
        }
    }

    /**
     * Handle exceptions from Serialization
     *
     * @param configurationFileLocation the configuration location
     * @param e the exception
     * @return an error flatten config result
     */
    private static FlattenConfigResult handleException(final String configurationFileLocation, final IOException e) {
        return FlattenConfigResult.error(String.format("Error flattening sourcehawk configuration at %s with error: %s", configurationFileLocation, e.getMessage()));
    }

}
