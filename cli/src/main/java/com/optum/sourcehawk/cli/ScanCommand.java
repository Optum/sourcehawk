package com.optum.sourcehawk.cli;

import com.optum.sourcehawk.core.repository.LocalRepositoryFileReader;
import com.optum.sourcehawk.core.result.ScanResult;
import com.optum.sourcehawk.core.utils.Try;
import com.optum.sourcehawk.exec.ExecOptions;
import com.optum.sourcehawk.exec.scan.ScanExecutor;
import com.optum.sourcehawk.exec.scan.ScanResultFactory;
import com.optum.sourcehawk.exec.scan.ScanResultLogger;
import lombok.val;
import picocli.CommandLine;

import java.util.Optional;

/**
 * CLI entry point for executing Sourcehawk scan command
 *
 * @author Brian Wyka
 */
@CommandLine.Command(
        name = "scan",
        aliases = { "flyover", "survey" },
        description = "Runs a Sourcehawk scan on the source code",
        subcommands = { GithubScanCommand.class, BitbucketScanCommand.class }
)
public class ScanCommand extends AbstractExecCommand {

    /**
     * The scan result logger
     */
    private static final ScanResultLogger SCAN_RESULT_LOGGER = ScanResultLogger.create();

    /**
     * The local file system options group
     */
    @SuppressWarnings("unused")
    @CommandLine.ArgGroup(exclusive = false)
    private CommandOptions.FileSystem fileSystem;

    /**
     * Bootstrap the command
     *
     * @param args the command line args
     */
    public static void main(final String... args) {
        execute(new ScanCommand(), args);
    }

    /**
     * Execute the scan
     *
     * @return the exit code
     */
    @Override
    public Integer call() {
        val execOptionsBuilder = buildExecOptions().toBuilder();
        val repositoryRootOptional = Optional.ofNullable(fileSystem).map(fs -> fs.repositoryRoot);
        repositoryRootOptional.ifPresent(path -> execOptionsBuilder.repositoryRoot(path)
                .repositoryFileReader(LocalRepositoryFileReader.create(path)));
        return call(execOptionsBuilder.build());
    }

    /**
     * Call the command with the provided exec options
     *
     * @param execOptions the exec options
     * @return the exit code
     */
    Integer call(final ExecOptions execOptions) {
        val scanResult = execute(execOptions);
        SCAN_RESULT_LOGGER.log(scanResult, execOptions);
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
        return Try.attemptOrDefault(() -> ScanExecutor.scan(execOptions), ScanResultFactory::globalError);
    }

}
