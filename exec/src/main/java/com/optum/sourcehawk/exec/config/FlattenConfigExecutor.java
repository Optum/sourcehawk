package com.optum.sourcehawk.exec.config;

import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.optum.sourcehawk.core.configuration.SourcehawkConfiguration;
import com.optum.sourcehawk.core.scan.FlattenConfigResult;
import com.optum.sourcehawk.core.utils.StringUtils;
import com.optum.sourcehawk.exec.ConfigurationReader;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.yaml.snakeyaml.DumperOptions;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Paths;

/**
 * Entry point for executing Sourcehawk flatten command
 *
 * @author Christian Oestreich
 */
@Slf4j
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
     * @param e                         the exception
     * @return an error flatten result
     */
    private static FlattenConfigResult handleException(final String configurationFileLocation, final IOException e) {
        val message = String.format("Error flattening sourcehawk configuration at %s with error: %s", configurationFileLocation, e.getMessage());
        return FlattenConfigResult.error(message);
    }

    private static class YamlGenerator extends YAMLGenerator {

        public YamlGenerator(final IOContext ctxt, final int jsonFeatures, final int yamlFeatures, final ObjectCodec codec, final Writer out,
                             final DumperOptions.Version version) throws IOException {
            super(ctxt, jsonFeatures, yamlFeatures, codec, out, version);
        }

        protected DumperOptions buildDumperOptions(int jsonFeatures, int yamlFeatures, DumperOptions.Version version) {
            val dumperOptions = super.buildDumperOptions(jsonFeatures, yamlFeatures, version);
            dumperOptions.setIndicatorIndent(2);
            dumperOptions.setIndent(4);
            dumperOptions.setPrettyFlow(true);
            return dumperOptions;
        }

    }

}
