package com.optum.sourcehawk.exec;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.optum.sourcehawk.configuration.SourcehawkConfiguration;
import com.optum.sourcehawk.core.constants.SourcehawkConstants;
import com.optum.sourcehawk.core.repository.LocalRepositoryFileWriter;
import com.optum.sourcehawk.core.scan.FlattenConfigResult;
import com.optum.sourcehawk.core.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

/**
 * CLI entry point for executing Sourcehawk flatten command
 *
 * @author Christian Oestreich
 */
@Slf4j
@CommandLine.Command(
        name = FlattenConfigCommand.COMMAND_NAME,
        aliases = {"flatten", "flat", "merge", "converge", "fc"},
        description = "Flatten the sourcehawk configuration onto system by recursively reading and merging local and remote configurations will output to console by default",
        mixinStandardHelpOptions = true
)
class FlattenConfigCommand implements Callable<Integer> {

    private static final ObjectWriter WRITER_WITH_DEFAULT_PRETTY_PRINTER = Sourcehawk.YAML_FORMATTER.writerWithDefaultPrettyPrinter();

    static final String COMMAND_NAME = "flatten-config";

    @CommandLine.Option(
            names = {"-o", "--output"},
            description = "Optional param to directly output flattened configuration to file system"
    )
    private Path output;

    @CommandLine.ArgGroup
    protected AbstractCommand.ConfigFileExclusiveOptions configFile;

    /**
     * Bootstrap the command
     *
     * @param args the command line args
     */
    public static void main(final String... args) {
        val command = new FlattenConfigCommand();
        val status = new CommandLine(command)
                .setCaseInsensitiveEnumValuesAllowed(true)
                .setTrimQuotes(true)
                .execute(args);
        Runtime.getRuntime()
                .halt(status);
    }

    /**
     * flatten the configuration
     *
     * @return the exit code
     */
    public Integer call() {
        FlattenConfigResult flattenResult;
        String configurationFileLocation = getConfigurationFileLocation();
        if (StringUtils.isBlankOrEmpty(configurationFileLocation)) {
            return CommandLine.ExitCode.SOFTWARE;
        }
        try {
            flattenResult = ConfigurationReader.readConfiguration(Paths.get("."), configurationFileLocation)
                    .map(sourcehawkConfiguration -> executeFlatten(configurationFileLocation, sourcehawkConfiguration))
                    .orElseGet(() -> FlattenConfigResult.error(String.format("Configuration file %s not found or invalid", configurationFileLocation)));
        } catch (final Exception e) {
            flattenResult = FlattenConfigResult.error(e.getMessage());
        }

        outputResults(flattenResult, output);

        if (flattenResult.isError()) {
            return CommandLine.ExitCode.SOFTWARE;
        }
        return CommandLine.ExitCode.OK;
    }

    private String getConfigurationFileLocation() {
        String configurationFileLocation = SourcehawkConstants.DEFAULT_CONFIG_FILE_NAME;
        if (configFile != null) {
            if (configFile.url != null) {
                configurationFileLocation = configFile.url.toString();
            } else {
                configurationFileLocation = configFile.path.toString();
            }
        }
        return configurationFileLocation;
    }

    /**
     * Execute the flatten iterating over all configuration locations and aggregate the results
     *
     * @param configurationFileLocation the configuration location
     * @param sourcehawkConfiguration   the configuration
     * @return the aggregated scan result
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

    private static final String SOURCEHAWK_FLATTENED_YML = "sourcehawk-flattened.yml";

    /**
     * Log the result of the flatten which will either output to a file or console based on params
     *
     * @param flattenResult      the flatten result
     * @param repositoryFilePath the repository file path
     */
    @SuppressWarnings("squid:S2629")
    void outputResults(final FlattenConfigResult flattenResult,
                       final Path repositoryFilePath) {
        if (repositoryFilePath == null || StringUtils.isBlankOrEmpty(repositoryFilePath.toString())) {
            handleDryRunOutput(flattenResult);
        } else {
            handleFileSystemOutput(flattenResult, repositoryFilePath);
        }
    }

    /**
     * Log the result of the flatten to the file system
     *
     * @param flattenResult      the flatten result
     * @param repositoryFilePath the repository file path
     */
    private static void handleFileSystemOutput(final FlattenConfigResult flattenResult, final Path repositoryFilePath) {
        try {
            String writerPath = StringUtils.defaultString(repositoryFilePath.toString(), SOURCEHAWK_FLATTENED_YML);
            LocalRepositoryFileWriter.writer().write(writerPath, flattenResult.getContent());
            Sourcehawk.CONSOLE_RAW_LOGGER.info(flattenResult.getFormattedMessage());
            Sourcehawk.CONSOLE_RAW_LOGGER.info("Output to {}", writerPath);
        } catch (IOException e) {
            Sourcehawk.CONSOLE_RAW_LOGGER.error("Could not flatten file due to {}", e.getMessage());
        }
    }

    /**
     * Log the result of the flatten to the console
     *
     * @param flattenResult the flatten result
     */
    private static void handleDryRunOutput(final FlattenConfigResult flattenResult) {
        if (flattenResult != null && flattenResult.getContent() != null) {
            Sourcehawk.CONSOLE_RAW_LOGGER.info(new String(flattenResult.getContent()));
        } else {
            Sourcehawk.CONSOLE_RAW_LOGGER.error("No flattened file produced!");
        }
    }
}
