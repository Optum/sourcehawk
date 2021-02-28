package com.optum.sourcehawk.cli;

import com.optum.sourcehawk.core.constants.SourcehawkConstants;
import com.optum.sourcehawk.core.result.FlattenConfigResult;
import com.optum.sourcehawk.core.utils.Try;
import com.optum.sourcehawk.exec.Console;
import com.optum.sourcehawk.exec.config.FlattenConfigExecutor;
import com.optum.sourcehawk.exec.config.FlattenConfigResultLogger;
import lombok.val;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * CLI entry point for executing Sourcehawk flatten command
 *
 * @author Christian Oestreich
 */
@CommandLine.Command(
        name = "flatten-config",
        aliases = {"fc", "flatten"},
        description = "Flatten the sourcehawk configuration onto system by recursively reading and merging local and remote configurations will output to console by default",
        mixinStandardHelpOptions = true
)
public class FlattenConfigCommand implements Callable<Integer> {

    /**
     * The configuration file to flatten
     */
    @SuppressWarnings("unused")
    @CommandLine.ArgGroup
    private CommandOptions.ConfigFile configFile;

    /**
     * The path on the file system to output the flattened configuration to
     */
    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-o", "--output"}, description = "Optional param to directly output flattened configuration to file system")
    private Path outputPath;

    /**
     * Bootstrap the command
     *
     * @param args the command line args
     */
    public static void main(final String... args) {
        val status = new CommandLine(new FlattenConfigCommand())
                .setTrimQuotes(true)
                .execute(args);
        Runtime.getRuntime().halt(status);
    }

    /**
     * flatten the configuration
     *
     * @return the exit code
     */
    public Integer call() {
        val configurationFileLocation = getConfigurationFileLocation();
        val flattenConfigResult = execute(configurationFileLocation);
        if (flattenConfigResult.isError()) {
            Console.Err.error(flattenConfigResult.getMessage());
            return CommandLine.ExitCode.SOFTWARE;
        }
        FlattenConfigResultLogger.log(flattenConfigResult, outputPath);
        return CommandLine.ExitCode.OK;
    }

    /**
     * Get the configuration file location based on the options provided
     *
     * @return the config file location
     */
    private String getConfigurationFileLocation() {
        if (configFile != null && configFile.url != null) {
            return configFile.url.toString();
        } else if (configFile != null) {
            return configFile.path.toString();
        }
        return SourcehawkConstants.DEFAULT_CONFIG_FILE_NAME;
    }

    /**
     * Execute flatten config and return the result
     *
     * @param configurationFileLocation the configuration file location
     * @return the flatten config result
     */
    private static FlattenConfigResult execute(final String configurationFileLocation) {
        return Try.attemptOrDefault(
                () -> FlattenConfigExecutor.flatten(configurationFileLocation),
                e -> FlattenConfigResult.error(Optional.ofNullable(e.getMessage()).orElse("Unknown error"))
        );
    }

}
