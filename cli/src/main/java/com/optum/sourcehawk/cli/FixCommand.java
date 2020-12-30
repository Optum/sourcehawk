package com.optum.sourcehawk.cli;

import com.optum.sourcehawk.core.repository.LocalRepositoryFileReader;
import com.optum.sourcehawk.core.scan.FixResult;
import com.optum.sourcehawk.core.utils.Try;
import com.optum.sourcehawk.exec.ExecOptions;
import com.optum.sourcehawk.exec.fix.FixExecutor;
import com.optum.sourcehawk.exec.fix.FixResultFactory;
import com.optum.sourcehawk.exec.fix.FixResultLogger;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.Optional;

/**
 * CLI entry point for executing Sourcehawk fix command
 *
 * @author Brian Wyka
 */
@Slf4j
@CommandLine.Command(
        name = "fix",
        aliases = { "correct", "resolve" },
        description = "Fix the source based on Sourcehawk configuration file. This will update the files in place if any updates are required to be made."
)
public class FixCommand extends AbstractExecCommand {

    /**
     * The local file system options group
     */
    @SuppressWarnings("unused")
    @CommandLine.ArgGroup(exclusive = false)
    private CommandOptions.FileSystem fileSystem;

    /**
     * Whether or not this should be executed as a dry run, without applying fixes
     */
    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-d", "--dry-run"}, description = "Display fixes which would be performed, but do not perform them")
    private boolean dryRun;

    /**
     * Bootstrap the command
     *
     * @param args the command line args
     */
    public static void main(final String... args) {
        execute(new FixCommand(), args);
    }

    /**
     * Fix the source code
     *
     * @return the exit code
     */
    @Override
    public Integer call() {
        val execOptionsBuilder = buildExecOptions().toBuilder();
        val repositoryRootOptional = Optional.ofNullable(fileSystem).map(fs -> fs.repositoryRoot);
        repositoryRootOptional.ifPresent(path -> execOptionsBuilder.repositoryRoot(path)
                .repositoryFileReader(LocalRepositoryFileReader.create(path)));
        val execOptions = execOptionsBuilder.build();
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
        return Try.attemptOrDefault(() -> FixExecutor.fix(execOptions, dryRun), FixResultFactory::globalError);
    }

}
