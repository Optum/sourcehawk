package com.optum.sourcehawk.cli;

import com.optum.sourcehawk.core.scan.OutputFormat;
import com.optum.sourcehawk.core.scan.Verbosity;
import com.optum.sourcehawk.exec.ExecOptions;
import lombok.val;
import picocli.CommandLine;

import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * Base exec command for sharing common options and parameters
 *
 * @author Brian Wyka
 */
@CommandLine.Command(
        mixinStandardHelpOptions = true,
        subcommands = { CommandLine.HelpCommand.class }
)
abstract class AbstractExecCommand implements Callable<Integer> {

    /**
     * The command spec
     */
    @CommandLine.Spec
    protected CommandLine.Model.CommandSpec spec;

    /**
     * The command exec options group
     */
    @CommandLine.ArgGroup(exclusive = false)
    protected CommandOptions.Exec exec;

    /**
     * Build the exec options from the command line options
     *
     * @return the exec options
     */
    protected ExecOptions buildExecOptions() {
        if (exec == null) {
            return ExecOptions.builder().build();
        }
        val builder = ExecOptions.builder();
        Optional.ofNullable(exec.configFile)
                .map(configFile -> Optional.<Object>ofNullable(configFile.url)
                        .orElseGet(() -> Optional.ofNullable(configFile.path).orElse(null)))
                .map(Object::toString)
                .ifPresent(builder::configurationFileLocation);
        Optional.ofNullable(exec.verbosity).ifPresent(builder::verbosity);
        Optional.ofNullable(exec.outputFormat).ifPresent(builder::outputFormat);
        if (exec.outputFormat == OutputFormat.JSON || exec.outputFormat == OutputFormat.MARKDOWN) {
            builder.verbosity(Verbosity.ZERO);
        }
        builder.failOnWarnings(exec.failOnWarnings);
        return builder.build();
    }

    /**
     * Execute the command with the provided args
     *
     * @param command the command to execute
     * @param args the args
     */
    protected static void execute(final Callable<Integer> command, final String... args) {
        val status = new CommandLine(command)
                .setCaseInsensitiveEnumValuesAllowed(true)
                .setTrimQuotes(true)
                .execute(args);
        Runtime.getRuntime().halt(status);
    }

}
