package com.optum.sourcehawk.exec;

import com.optum.sourcehawk.core.scan.FlattenResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine;

/**
 * CLI entry point for executing Sourcehawk flatten command
 *
 * @author Christian Oestreich
 */
@Slf4j
@CommandLine.Command(
        name = FlattenCommand.COMMAND_NAME,
        aliases = {"flat", "merge", "converge"},
        description = "Flatten the sourcehawk.yml onto system by recursively reading and merging local and remote configurations"
)
class FlattenCommand extends AbstractCommand {

    static final String COMMAND_NAME = "flatten";


    @CommandLine.Option(
            names = {"-o", "--output"},
            description = "Override the default output name and path flattened file",
            defaultValue = "sourcehawk-flattened.yml",
            showDefaultValue = CommandLine.Help.Visibility.ALWAYS
    )
    private String output;

    @CommandLine.Option(
            names = {"-d", "--dry-run"},
            description = "Display output to console instead of file system"
    )
    private boolean dryRun;

    /**
     * Bootstrap the command
     *
     * @param args the command line args
     */
    public static void main(final String... args) {
        val command = new FlattenCommand();
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
    @Override
    public Integer call() {
        FlattenResult flattenResult;
        val execOptions = buildExecOptions();
        try {
            flattenResult = FlattenExecutor.flatten(execOptions);
        } catch (final Exception e) {
            flattenResult = FlattenResultFactory.error(execOptions.getRepositoryRoot().toString(), e.getMessage());
        }
        FlattenResultLogger.log(flattenResult, execOptions, output, dryRun);
        if (flattenResult.isError()) {
            return CommandLine.ExitCode.SOFTWARE;
        }
        return CommandLine.ExitCode.OK;
    }

}
