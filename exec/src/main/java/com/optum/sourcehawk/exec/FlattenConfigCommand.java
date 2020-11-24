package com.optum.sourcehawk.exec;

import com.optum.sourcehawk.core.constants.SourcehawkConstants;
import com.optum.sourcehawk.core.scan.FlattenConfigResult;
import com.optum.sourcehawk.core.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine;

import java.nio.file.Path;
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
        val configurationFileLocation = getConfigurationFileLocation();
        if (StringUtils.isBlankOrEmpty(configurationFileLocation)) {
            return CommandLine.ExitCode.SOFTWARE;
        }
        try {
            flattenResult = FlattenConfigExecutor.flatten(configurationFileLocation);
        } catch (final Exception e) {
            flattenResult = FlattenConfigResult.error(e.getMessage());
        }

        if (flattenResult.isError()) {
            Sourcehawk.CONSOLE_RAW_LOGGER.info(flattenResult.getFormattedMessage());
            return CommandLine.ExitCode.SOFTWARE;
        }

        FlattenConfigResultLogger.log(flattenResult, output);

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
}
