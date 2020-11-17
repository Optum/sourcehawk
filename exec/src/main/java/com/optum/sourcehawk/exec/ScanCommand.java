package com.optum.sourcehawk.exec;

import com.optum.sourcehawk.core.scan.ScanResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine;

/**
 * CLI entry point for executing Sourcehawk scan command
 *
 * @author Brian Wyka
 */
@Slf4j
@CommandLine.Command(
        name = ScanCommand.COMMAND_NAME,
        aliases = { "flyover", "survey" },
        description = "Runs a Sourcehawk scan on the source code"
)
class ScanCommand extends AbstractCommand {

    static final String COMMAND_NAME = "scan";

    @CommandLine.Option(
            names = {"-fow", "--fail-on-warnings"},
            description = "Whether to fail the scan if only warnings are found",
            defaultValue = "false",
            showDefaultValue = CommandLine.Help.Visibility.ALWAYS
    )
    private boolean failOnWarnings;

    /**
     * Bootstrap the command
     *
     * @param args the command line args
     */
    public static void main(final String... args) {
        val command = new ScanCommand();
        val status = new CommandLine(command)
                .setCaseInsensitiveEnumValuesAllowed(true)
                .setTrimQuotes(true)
                .execute(args);
        Runtime.getRuntime()
                .halt(status);
    }

    /**
     * Execute the scan
     *
     * @return the exit code
     */
    @Override
    public Integer call() {
        ScanResult scanResult;
        val execOptions = buildExecOptions().toBuilder().failOnWarnings(failOnWarnings).build();
        try {
            scanResult = ScanExecutor.scan(execOptions);
        } catch (final Exception e) {
            scanResult = ScanResultFactory.error(execOptions.getRepositoryRoot().toString(), e.getMessage());
        }
        ScanResultLogger.log(scanResult, execOptions);
        if (scanResult.isPassed()) {
            return CommandLine.ExitCode.OK;
        }
        return CommandLine.ExitCode.SOFTWARE;
    }

}
