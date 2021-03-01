package com.optum.sourcehawk.exec.config;

import com.optum.sourcehawk.core.configuration.SourcehawkConfiguration;
import com.optum.sourcehawk.core.result.FlattenConfigResult;
import com.optum.sourcehawk.exec.ConfigurationReader;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import java.nio.charset.Charset;
import java.nio.file.Paths;

/**
 * Entry point for executing Sourcehawk flatten command
 *
 * @author Christian Oestreich
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FlattenConfigExecutor {

    private static final Yaml YAML;

    static {
        final Constructor constructor = new Constructor(SourcehawkConfiguration.class);
        constructor.setPropertyUtils(new PropertyUtils() {
            @Override
            public Property getProperty(final Class<? extends Object> type, final String name) {
                if (name.indexOf('-') > -1) {
                    return super.getProperty(type, name.replaceAll("([a-z])([A-Z]+)", "$1-$2"));
                }
                return super.getProperty(type, name);
            }
        });
        YAML = new Yaml(constructor);
    }

    /**
     * Run the flatten config based on the provided configuration file location
     *
     * @param configurationFileLocation the sourcehawk configuration location
     * @return the flatten config result
     */
    public static FlattenConfigResult flatten(final String configurationFileLocation) {
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
            return FlattenConfigResult.success(YAML.dump(sourcehawkConfiguration).getBytes(Charset.defaultCharset()));
        } catch (final Exception e) {
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
    private static FlattenConfigResult handleException(final String configurationFileLocation, final Exception e) {
        return FlattenConfigResult.error(String.format("Error flattening sourcehawk configuration at %s with error: %s", configurationFileLocation, e.getMessage()));
    }

}
