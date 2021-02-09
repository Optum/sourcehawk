package com.optum.sourcehawk.exec;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;
import com.optum.sourcehawk.core.configuration.SourcehawkConfiguration;
import com.optum.sourcehawk.core.utils.CollectionUtils;
import com.optum.sourcehawk.core.utils.StringUtils;
import com.optum.sourcehawk.enforcer.file.FileEnforcer;
import com.optum.sourcehawk.enforcer.file.FileResolver;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for reading and deserialization of configuration
 *
 * @author Brian Wyka
 */
@UtilityClass
public class ConfigurationReader {

    /**
     * The object mapper which is used to deserialize the configuration from file
     */
    public final ObjectMapper MAPPER = YAMLMapper.builder()
        .addModule(new BlackbirdModule())
        .serializationInclusion(JsonInclude.Include.NON_NULL)
        .propertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE)
        .annotationIntrospector(new JacksonAnnotationIntrospector() {
            @Override
            public JsonPOJOBuilder.Value findPOJOBuilderConfig(final AnnotatedClass annotatedClass) {
                return new JsonPOJOBuilder.Value("build", "");
            }
        })
        .build();

    /**
     * Parse the configuration from the provided yaml string
     *
     * @param inputStream the input stream
     * @return the configuration
     */
    public SourcehawkConfiguration parseConfiguration(final InputStream inputStream) throws IOException {
        return MAPPER.readValue(inputStream, SourcehawkConfiguration.class);
    }

    /**
     * Parse the configuration from the provided file
     *
     * @param configurationFilePath the configuration file path
     * @return the configuration
     */
    public SourcehawkConfiguration parseConfiguration(final Path configurationFilePath) throws IOException {
        return parseConfiguration(Files.newInputStream(configurationFilePath));
    }

    /**
     * Read the configuration from the provided location
     *
     * @param repositoryRoot the repository root
     * @param configurationFileLocation the config file location
     * @return the configuration
     */
    public Optional<SourcehawkConfiguration> readConfiguration(final Path repositoryRoot, final String configurationFileLocation) {
        try {
            return obtainInputStream(repositoryRoot, configurationFileLocation)
                    .flatMap(ConfigurationReader::deserialize)
                    .map(sourcehawkConfiguration -> readConfigurationLocations(new HashSet<>(), sourcehawkConfiguration, repositoryRoot))
                    .map(ConfigurationReader::merge);
        } catch (final IOException e) {
            Console.Err.error("Error reading configuration file: %s", e.getMessage());
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
        if (CollectionUtils.isNotEmpty(childConfiguration.getConfigLocations())) {
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
    private Optional<InputStream> obtainInputStream(final Path repositoryRoot, final String configFileLocation) throws IOException {
        try {
            if (StringUtils.isUrl(configFileLocation)) {
                return Optional.of(new URL(configFileLocation).openStream());
            }
            val configFilePath = Paths.get(configFileLocation);
            if (configFilePath.isAbsolute()) {
                return Optional.of(Files.newInputStream(Paths.get(configFileLocation), StandardOpenOption.READ));
            }
            return Optional.of(Files.newInputStream(repositoryRoot.resolve(configFilePath), StandardOpenOption.READ));
        } catch (final NoSuchFileException | FileNotFoundException e) {
            Console.Err.error("Configuration file not found: %s", configFileLocation);
            return Optional.empty();
        }
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
            Console.Err.error("Error parsing configuration file: %s", e.getMessage());
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
        if (CollectionUtils.isEmpty(sourcehawkConfigurations)) {
            return SourcehawkConfiguration.empty();
        }
        if (sourcehawkConfigurations.size() == 1) {
            return sourcehawkConfigurations.iterator().next();
        }
        val configLocations = sourcehawkConfigurations.stream()
                .filter(Objects::nonNull)
                .map(SourcehawkConfiguration::getConfigLocations)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        val fileProtocols = sourcehawkConfigurations.stream()
                .filter(Objects::nonNull)
                .map(SourcehawkConfiguration::getFileProtocols)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        return SourcehawkConfiguration.of(configLocations, fileProtocols);
    }

    /**
     * Parse the file enforcer
     *
     * @param fileEnforcerObject the file enforcer object
     * @return the file enforcer
     */
    public FileEnforcer parseFileEnforcer(final Object fileEnforcerObject) {
        return MAPPER.convertValue(fileEnforcerObject, FileEnforcer.class);
    }

    /**
     * Convert the file enforcer to a file resolver
     *
     * @param fileEnforcerObject the file enforcer object
     * @return the file resolver if able to be converted, otherwise {@link Optional#empty()}
     */
    public Optional<FileResolver> convertFileEnforcerToFileResolver(final Object fileEnforcerObject) {
        val fileEnforcer = parseFileEnforcer(fileEnforcerObject);
        if (fileEnforcer instanceof FileResolver) {
            return Optional.of(fileEnforcer)
                    .map(FileResolver.class::cast);
        }
        return Optional.empty();
    }

}
