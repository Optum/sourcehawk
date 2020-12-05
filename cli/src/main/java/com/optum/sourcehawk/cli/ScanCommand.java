package com.optum.sourcehawk.cli;

import com.optum.sourcehawk.core.scan.ScanResult;
import com.optum.sourcehawk.exec.ExecOptions;
import com.optum.sourcehawk.exec.scan.ScanExecutor;
import com.optum.sourcehawk.exec.scan.ScanResultFactory;
import com.optum.sourcehawk.exec.scan.ScanResultLogger;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine;

import java.util.Optional;

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
        val status = new CommandLine(new ScanCommand())
                .setCaseInsensitiveEnumValuesAllowed(true)
                .setTrimQuotes(true)
                .execute(args);
        Runtime.getRuntime().halt(status);
    }

    /**
     * Execute the scan
     *
     * @return the exit code
     */
    @Override
    public Integer call() {
        val execOptions = buildExecOptions().toBuilder().failOnWarnings(failOnWarnings).build();
        val scanResult = execute(execOptions);
        ScanResultLogger.log(scanResult, execOptions);
        if (scanResult.isPassed()) {
            return CommandLine.ExitCode.OK;
        }
        return CommandLine.ExitCode.SOFTWARE;
    }

    /**
     * Execute the scan and return the result
     *
     * @param execOptions the exec options
     * @return the scan result
     */
    private static ScanResult execute(final ExecOptions execOptions) {
        try {
            return ScanExecutor.scan(execOptions);
        } catch (final Exception e) {
            return ScanResultFactory.error("GLOBAL", Optional.ofNullable(e.getMessage()).orElse("Unknown error"));
        }
    }

}
