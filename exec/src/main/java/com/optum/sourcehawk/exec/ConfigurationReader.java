package com.optum.sourcehawk.exec;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.optum.sourcehawk.configuration.SourcehawkConfiguration;
import com.optum.sourcehawk.core.utils.CollectionUtils;
import com.optum.sourcehawk.core.utils.StringUtils;
import com.optum.sourcehawk.enforcer.file.FileEnforcer;
import com.optum.sourcehawk.enforcer.file.FileResolver;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Utility class for reading and deserialization of configuration
 *
 * @author Brian Wyka
 */
@Slf4j
@UtilityClass
class ConfigurationReader {

    /**
     * The object mapper which is used to deserialize the configuration from file
     */
    final ObjectMapper MAPPER = new YAMLMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .setPropertyNamingStrategy(new PropertyNamingStrategy.KebabCaseStrategy());

    /**
     * Parse the configuration from the provided yaml string
     *
     * @param inputStream the input stream
     * @return the configuration
     */
    SourcehawkConfiguration parseConfiguration(final InputStream inputStream) throws IOException {
        return MAPPER.readValue(inputStream, SourcehawkConfiguration.class);
    }

    /**
     * Parse the configuration from the provided file
     *
     * @param configurationFilePath the configuration file path
     * @return the configuration
     */
    SourcehawkConfiguration parseConfiguration(final Path configurationFilePath) throws IOException {
        return parseConfiguration(Files.newInputStream(configurationFilePath));
    }

    /**
     * Read the configuration from the provided location
     *
     * @param repositoryRoot the repository root
     * @param configurationFileLocation the config file location
     * @return the configuration
     */
    Optional<SourcehawkConfiguration> readConfiguration(final Path repositoryRoot, final String configurationFileLocation) {
        try {
            return Optional.ofNullable(obtainInputStream(repositoryRoot, configurationFileLocation))
                    .flatMap(ConfigurationReader::deserialize)
                    .map(sourcehawkConfiguration -> readConfigurationLocations(new HashSet<>(), sourcehawkConfiguration, repositoryRoot))
                    .map(ConfigurationReader::merge);
        } catch (final IOException e) {
            return Optional.empty();
        }
    }

    /**
     * Will read configuration objects from reading local config and then reading recursively and remotely.
     *
     * @param processedConfigLocations The remote locations processed so far to avoid dupe reads
     * @param sourcehawkConfiguration  The config to read from
     * @param repositoryRoot the repository root
     * @return a list of all the configs in the hierarchy
     */
    private static Set<SourcehawkConfiguration> readConfigurationLocations(final Set<String> processedConfigLocations, final SourcehawkConfiguration sourcehawkConfiguration,
                                                                           final Path repositoryRoot) {
        val sourcehawkConfigurations = new LinkedHashSet<>(Collections.singletonList(sourcehawkConfiguration));
        Optional.ofNullable(sourcehawkConfiguration)
                .map(SourcehawkConfiguration::getConfigLocations)
                .orElseGet(HashSet::new)
                .stream()
                .filter(configLocation -> !processedConfigLocations.contains(configLocation))
                .forEach(configLocation -> processChildConfigurations(processedConfigLocations, repositoryRoot, sourcehawkConfigurations, configLocation));
        return sourcehawkConfigurations;
    }

    /**
     * Process child configurations and add to list to avoid reprocessing duplicated configs.
     *
     * @param processedConfigLocations already processed config set
     * @param repositoryRoot the repository root
     * @param sourcehawkConfigurations current config children
     * @param configLocation the current config location
     */
    private static void processChildConfigurations(final Set<String> processedConfigLocations, final Path repositoryRoot,
                                                   final Set<SourcehawkConfiguration> sourcehawkConfigurations, final String configLocation) {
        val childConfiguration = readConfiguration(repositoryRoot, configLocation)
                .orElseThrow(() -> new ConfigurationException(String.format("Could not locate or deserialize file %s", configLocation)));
        processedConfigLocations.add(configLocation);
        sourcehawkConfigurations.add(childConfiguration);
        if (childConfiguration != null && CollectionUtils.isNotEmpty(childConfiguration.getConfigLocations())) {
            sourcehawkConfigurations.addAll(readConfigurationLocations(processedConfigLocations, childConfiguration, repositoryRoot));
        }
    }

    /**
     * Obtain the configuration input stream
     *
     * @param repositoryRoot the repository root
     * @param configFileLocation the config file URI
     * @return the configuration
     * @throws IOException if any error occurs obtaining input stream
     */
    private InputStream obtainInputStream(final Path repositoryRoot, final String configFileLocation) throws IOException {
        if (StringUtils.isUrl(configFileLocation)) {
            return new URL(configFileLocation).openStream();
        }
        val configFilePath = Paths.get(configFileLocation);
        if (configFilePath.isAbsolute()) {
            return Files.newInputStream(Paths.get(configFileLocation), StandardOpenOption.READ);
        }
        return Files.newInputStream(repositoryRoot.resolve(configFilePath), StandardOpenOption.READ);
    }

    /**
     * Deserialize the configuration file
     *
     * @param inputStream the configuration file input stream
     * @return the deserialized configuration
     */
    private Optional<SourcehawkConfiguration> deserialize(final InputStream inputStream) {
        try {
            return Optional.of(MAPPER.readValue(inputStream, SourcehawkConfiguration.class));
        } catch (final IOException e) {
            log.error("Error reading configuration file", e);
            return Optional.empty();
        }
    }

    /**
     * Merge together all the configurations into a single file
     *
     * @param sourcehawkConfigurations the list of configurations to merge
     * @return the merged configurations
     */
    private SourcehawkConfiguration merge(final Set<SourcehawkConfiguration> sourcehawkConfigurations) {
        SourcehawkConfiguration sourcehawkConfiguration = SourcehawkConfiguration.of(new LinkedHashSet<>(), new HashSet<>());
        if (CollectionUtils.isEmpty(sourcehawkConfigurations)) {
            return sourcehawkConfiguration;
        }
        if (sourcehawkConfigurations.size() == 1) {
                return Optional.ofNullable(sourcehawkConfigurations.iterator().next())
                        .orElseThrow(() -> new ConfigurationException("No sourcehawk file could be located or serialized"));
        }
        for (val configuration : sourcehawkConfigurations) {
            try {
                sourcehawkConfiguration = MAPPER.readerForUpdating(sourcehawkConfiguration)
                        .readValue(MAPPER.writeValueAsString(configuration), SourcehawkConfiguration.class);
            } catch (final IOException e) {
                throw new ConfigurationException("Could not merge config files " + e.getMessage());
            }
        }
        return sourcehawkConfiguration;
    }

    /**
     * Parse the file enforcer
     *
     * @param fileEnforcerObject the file enforcer object
     * @return the file enforcer
     */
    static FileEnforcer parseFileEnforcer(final Object fileEnforcerObject) {
        return MAPPER.convertValue(fileEnforcerObject, FileEnforcer.class);
    }

    /**
     * Convert the file enforcer to a file resolver
     *
     * @param fileEnforcerObject the file enforcer object
     * @return the file resolver if able to be converted, otherwise {@link Optional#empty()}
     */
    static Optional<FileResolver> convertFileEnforcerToFileResolver(final Object fileEnforcerObject) {
        val fileEnforcer = parseFileEnforcer(fileEnforcerObject);
        if (fileEnforcer instanceof FileResolver) {
            return Optional.of(fileEnforcer).map(FileResolver.class::cast);
        }
        return Optional.empty();
    }

}
