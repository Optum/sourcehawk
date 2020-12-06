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
abstract class AbstractExecCommand implements Callable<Integer> {

    @CommandLine.Spec
    protected CommandLine.Model.CommandSpec spec;

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

    @CommandLine.ArgGroup(exclusive = false)
    protected GithubOptions github;

    /**
     * Build the exec options from the command line options
     *
     * @return the exec options
     */
    protected ExecOptions buildExecOptions() {
        val builder = ExecOptions.builder();
        buildRepositoryOptions(builder);
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
     * Build up the repository options
     *
     * @param builder the exec options builder
     */
    private void buildRepositoryOptions(final ExecOptions.ExecOptionsBuilder builder) {
        if (github != null) {
            val githubBuilder = ExecOptions.GithubOptions.builder();
            if (github.coords.indexOf('/') == -1) {
                throw new IllegalArgumentException(String.format("%s invalid, must contain '/' separator between owner and repository", GithubOptions.COORDS_LABEL));
            }
            githubBuilder.coords(github.coords);
            if (StringUtils.isNotBlankOrEmpty(github.ref)) {
                githubBuilder.ref(github.ref);
            }
            if (StringUtils.isNotBlankOrEmpty(github.token)) {
                githubBuilder.token(github.token);
            }
            if (github.enterpriseUrl != null) {
                githubBuilder.enterpriseUrl(github.enterpriseUrl);
            }
            builder.github(githubBuilder.build());
        } else if (repositoryRootPath != null && !StringUtils.isBlankOrEmpty(repositoryRootPath.toString())) {
            builder.repositoryRoot(repositoryRootPath);
        }
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

    /**
     * Exclusive config file options
     *
     * @author Brian Wyka
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

    /**
     * Github options
     *
     * @author Brian Wyka
     */
    static class GithubOptions {

        @CommandLine.Option(
                names = {"-ght", "--github-token"},
                paramLabel = "github",
                description = "The Github personal access token for authorizing requests, useful for preventing rate limiting or accessing private repositories"
        )
        String token;

        @CommandLine.Option(
                names = {"-ghc", "--github-coords"},
                paramLabel = COORDS_LABEL,
                required = true,
                description = "The Github coordinates - owner/repo/ref combination, i.e - owner/repo/main or owner/repo/a6de43fa51c"
        )
        String coords;
        private static final String COORDS_LABEL = "github-coords";

        @CommandLine.Option(
                names = {"-ghr", "--github-ref"},
                paramLabel = "github-ref",
                defaultValue = ExecOptions.GithubOptions.DEFAULT_REF,
                showDefaultValue = CommandLine.Help.Visibility.ALWAYS,
                description = "The Github ref, i.e - main, v1.0.0, a6de43fa51c, defaults to ${DEFAULT-VALUE}"
        )
        String ref;

        @CommandLine.Option(
                names = {"-ghe", "--github-enterprise"},
                paramLabel = "github-enterprise",
                description = "The Github enterprise URL, i.e - https://github.yourenterprise.com"
        )
        URL enterpriseUrl;

    }

}
