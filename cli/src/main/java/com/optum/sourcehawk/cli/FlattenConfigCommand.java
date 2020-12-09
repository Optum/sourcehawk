package com.optum.sourcehawk.cli;

import com.optum.sourcehawk.core.constants.SourcehawkConstants;
import com.optum.sourcehawk.core.scan.FlattenConfigResult;
import com.optum.sourcehawk.core.utils.StringUtils;
import com.optum.sourcehawk.exec.ExecLoggers;
import com.optum.sourcehawk.exec.flattenconfig.FlattenConfigExecutor;
import com.optum.sourcehawk.exec.flattenconfig.FlattenConfigResultLogger;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@CommandLine.Command(
        name = FlattenConfigCommand.COMMAND_NAME,
        aliases = {"flatten", "flat", "merge", "converge", "fc"},
        description = "Flatten the sourcehawk configuration onto system by recursively reading and merging local and remote configurations will output to console by default",
        mixinStandardHelpOptions = true
)
public class FlattenConfigCommand implements Callable<Integer> {

    static final String COMMAND_NAME = "flatten-config";

    @CommandLine.Option(
            names = {"-o", "--output"},
            description = "Optional param to directly output flattened configuration to file system"
    )
    private Path output;

    @CommandLine.ArgGroup
    protected AbstractExecCommand.ConfigFileExclusiveOptions configFile;

    /**
     * Bootstrap the command
     *
     * @param args the command line args
     */
    public static void main(final String... args) {
        val status = new CommandLine(new FlattenConfigCommand()).setTrimQuotes(true).execute(args);
        Runtime.getRuntime().halt(status);
    }

    /**
     * flatten the configuration
     *
     * @return the exit code
     */
    public Integer call() {
        val configurationFileLocation = getConfigurationFileLocation();
        if (StringUtils.isBlankOrEmpty(configurationFileLocation)) {
            return CommandLine.ExitCode.SOFTWARE;
        }
        val flattenConfigResult = execute(configurationFileLocation);
        if (flattenConfigResult.isError()) {
            ExecLoggers.CONSOLE_RAW.info(flattenConfigResult.getFormattedMessage());
            return CommandLine.ExitCode.SOFTWARE;
        }
        FlattenConfigResultLogger.log(flattenConfigResult, output);
        return CommandLine.ExitCode.OK;
    }

    /**
     * Execute flatten config and return the result
     *
     * @param configurationFileLocation the configuration file location
     * @return the flatten config result
     */
    private static FlattenConfigResult execute(final String configurationFileLocation) {
        try {
            return FlattenConfigExecutor.flatten(configurationFileLocation);
        } catch (final Exception e) {
            return FlattenConfigResult.error(Optional.ofNullable(e.getMessage()).orElse("Unknown error"));
        }
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

}
