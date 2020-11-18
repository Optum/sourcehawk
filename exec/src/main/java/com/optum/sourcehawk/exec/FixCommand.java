package com.optum.sourcehawk.exec;

import com.optum.sourcehawk.core.scan.FixResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine;

import java.util.Optional;

/**
 * CLI entry point for executing Sourcehawk fix command
 *
 * @author Brian Wyka
 */
@Slf4j
@CommandLine.Command(
        name = FixCommand.COMMAND_NAME,
        aliases = { "correct", "resolve" },
        description = "Fix the source based on Sourcehawk configuration file. This will update the files in place if any updates are required to be made."
)
class FixCommand extends AbstractCommand {

    static final String COMMAND_NAME = "fix";

    @CommandLine.Option(
            names = {"-d", "--dry-run"},
            description = "Display fixes which would be performed, but do not perform them"
    )
    private boolean dryRun;

    /**
     * Bootstrap the command
     *
     * @param args the command line args
     */
    public static void main(final String... args) {
        val command = new FixCommand();
        val status = new CommandLine(command)
                .setCaseInsensitiveEnumValuesAllowed(true)
                .setTrimQuotes(true)
                .execute(args);
        Runtime.getRuntime().halt(status);
    }

    /**
     * Fix the source code
     *
     * @return the exit code
     */
    @Override
    public Integer call() {
        val execOptions = buildExecOptions();
        val fixResult = execute(execOptions, dryRun);
        FixResultLogger.log(fixResult, execOptions);
        if (fixResult.isError()) {
            return CommandLine.ExitCode.SOFTWARE;
        } else if (!fixResult.isFixesApplied() && !dryRun) {
            return CommandLine.ExitCode.USAGE;
        }
        return CommandLine.ExitCode.OK;
    }

    /**
     * Execute the fix
     *
     * @param execOptions the exec options
     * @param dryRun whether or not this is a dry run
     * @return the fix result
     */
    private static FixResult execute(final ExecOptions execOptions, final boolean dryRun) {
        try {
            return FixExecutor.fix(execOptions, dryRun);
        } catch (final Exception e) {
            return FixResultFactory.error("GLOBAL", Optional.ofNullable(e.getMessage()).orElse("Unknown error"));
        }
    }

}
