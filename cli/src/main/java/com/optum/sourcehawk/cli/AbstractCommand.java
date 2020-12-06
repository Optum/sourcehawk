package com.optum.sourcehawk.cli;

import com.optum.sourcehawk.core.constants.SourcehawkConstants;
import com.optum.sourcehawk.core.scan.OutputFormat;
import com.optum.sourcehawk.core.scan.Verbosity;
import com.optum.sourcehawk.core.utils.StringUtils;
import com.optum.sourcehawk.exec.ExecOptions;
import lombok.val;
import picocli.CommandLine;

import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * Base command for sharing common options and parameters
 *
 * @author Brian Wyka
 */
@CommandLine.Command(
        mixinStandardHelpOptions = true,
        subcommands = { CommandLine.HelpCommand.class }
)
abstract class AbstractCommand implements Callable<Integer> {

    @CommandLine.Parameters(
            index = "0",
            paramLabel = REPO_ROOT,
            description = "The repository root on the file system to scan relative to, defaults to current directory",
            defaultValue = ".",
            arity = "0..1"
    )
    protected Path repositoryRootPath;
    protected static final String REPO_ROOT = "REPO-ROOT";

    @CommandLine.ArgGroup
    protected ConfigFileExclusiveOptions configFile;

    @CommandLine.Option(
            names = {"-v", "--verbosity"},
            description = "Verbosity of output, valid values: ${COMPLETION-CANDIDATES}",
            defaultValue = "HIGH",
            showDefaultValue = CommandLine.Help.Visibility.ALWAYS
    )
    protected Verbosity verbosity;

    @CommandLine.Option(
            names = {"-f", "--output-format"},
            description = "Output Format, valid values: ${COMPLETION-CANDIDATES}",
            defaultValue = "CONSOLE",
            showDefaultValue = CommandLine.Help.Visibility.ALWAYS
    )
    protected OutputFormat outputFormat;

    /**
     * Build the exec options from the command line options
     *
     * @return the exec options
     */
    protected ExecOptions buildExecOptions() {
        val builder = ExecOptions.builder();
        if (repositoryRootPath != null && !StringUtils.isBlankOrEmpty(repositoryRootPath.toString())) {
            builder.repositoryRoot(repositoryRootPath);
        }
        if (configFile != null) {
            if (configFile.url != null) {
                builder.configurationFileLocation(configFile.url.toString());
            } else {
                builder.configurationFileLocation(configFile.path.toString());
            }
        }
        if (verbosity != null) {
            builder.verbosity(verbosity);
        }
        if (outputFormat != null) {
            builder.outputFormat(outputFormat);
            if (outputFormat == OutputFormat.JSON || outputFormat == OutputFormat.MARKDOWN) {
                builder.verbosity(Verbosity.ZERO);
            }
        }
        return builder.build();
    }

    /**
     * Exclusive config file options
     */
    static class ConfigFileExclusiveOptions {

        @CommandLine.Option(
                names = {"-c", "-cf", "--config-file"},
                paramLabel = "config-file-path",
                description = "The configuration file, can be relative to " + REPO_ROOT + ", or absolute",
                defaultValue = SourcehawkConstants.DEFAULT_CONFIG_FILE_NAME,
                showDefaultValue = CommandLine.Help.Visibility.ALWAYS
        )
        Path path;

        @CommandLine.Option(
                names = {"-cfu", "--config-file-url"},
                paramLabel = "config-file-url",
                description = "A remote URL which resolves to a configuration file"
        )
        URL url;

    }

}
