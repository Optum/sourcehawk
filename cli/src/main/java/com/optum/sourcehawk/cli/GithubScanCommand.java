package com.optum.sourcehawk.cli;

import com.optum.sourcehawk.core.constants.SourcehawkConstants;
import com.optum.sourcehawk.core.repository.GithubRepositoryFileReader;
import com.optum.sourcehawk.core.utils.StringUtils;
import com.optum.sourcehawk.exec.ExecOptions;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine;

import java.net.URL;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * CLI entry point for executing Sourcehawk scan github command.
 *
 * @see ScanCommand
 *
 * @author Brian Wyka
 */
@Slf4j
@CommandLine.Command(
        name = "github",
        aliases = "gh",
        description = "Runs a Sourcehawk scan on remote Github source code instead of local file system",
        mixinStandardHelpOptions = true,
        subcommands = { CommandLine.HelpCommand.class }
)
public class GithubScanCommand implements Callable<Integer> {

    private static final char COORDINATES_DELIMITER = '/';
    private static final char REF_DELIMITER = '@';

    /**
     * The command spec
     */
    @SuppressWarnings("unused")
    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    /**
     * Reference to the parent scan command
     */
    @SuppressWarnings("unused")
    @CommandLine.ParentCommand
    private ScanCommand parentCommand;

    /**
     * The github options
     */
    @SuppressWarnings("unused")
    @CommandLine.ArgGroup(exclusive = false)
    private CommandOptions.Github github;

    /**
     * Bootstrap the command
     *
     * @param args the command line args
     */
    public static void main(final String... args) {
        AbstractExecCommand.execute(new GithubScanCommand(), args);
    }

    /**
     * Execute the scan
     *
     * @return the exit code
     */
    @Override
    public Integer call() {
        val parentExecOptions = parentCommand.buildExecOptions();
        val execOptionsBuilder = parentExecOptions.toBuilder();
        val configFileProvided = Optional.ofNullable(parentCommand.spec)
                .map(CommandLine.Model.CommandSpec::commandLine)
                .map(CommandLine::getParseResult)
                .filter(parseResult -> parseResult.hasMatchedOption(CommandOptions.ConfigFile.OPTION_PATH)
                        || parseResult.hasMatchedOption(CommandOptions.ConfigFile.OPTION_PATH_LONG))
                .isPresent();
        val githubBuilder = parseCoordinates();
        Optional.ofNullable(github.token).filter(StringUtils::isNotBlankOrEmpty).ifPresent(githubBuilder::token);
        Optional.ofNullable(github.enterpriseUrl).ifPresent(githubBuilder::enterpriseUrl);
        val githubOptions = githubBuilder.build();
        if (StringUtils.equals(SourcehawkConstants.DEFAULT_CONFIG_FILE_NAME, parentExecOptions.getConfigurationFileLocation()) && !configFileProvided) {
            execOptionsBuilder.configurationFileLocation(constructRemoteConfigFileLocation(github, githubOptions));
        }
        return parentCommand.call(execOptionsBuilder.github(githubOptions).build());
    }

    /**
     * Construct the remote config file location
     *
     * @param github the github command options
     * @param githubOptions the github exec options
     * @return the config file remote location
     */
    private static String constructRemoteConfigFileLocation(final CommandOptions.Github github, final ExecOptions.GithubOptions githubOptions) {
        val githubEnterpriseUrl = Optional.ofNullable(github.enterpriseUrl).map(URL::toString);
        val githubRepoBaseUrl = GithubRepositoryFileReader.constructBaseUrl(
                githubEnterpriseUrl.orElse(GithubRepositoryFileReader.DEFAULT_BASE_URL),
                githubEnterpriseUrl.isPresent(),
                githubOptions.getOwner(),
                githubOptions.getRepository(),
                githubOptions.getRef()
        );
        return githubRepoBaseUrl + SourcehawkConstants.DEFAULT_CONFIG_FILE_NAME;
    }

    /**
     * Parse the coordinates to github options
     *
     * @return the github options builder
     */
    private ExecOptions.GithubOptions.GithubOptionsBuilder parseCoordinates() {
        val githubBuilder = ExecOptions.GithubOptions.builder();
        if (github.coordinates.indexOf(COORDINATES_DELIMITER) == -1) {
            val message = String.format("%s invalid, must contain '%s' separator between owner and repository",
                    CommandOptions.Github.COORDINATES_LABEL, COORDINATES_DELIMITER);
            throw new CommandLine.ParameterException(spec.commandLine(), message);
        }
        final String rawCoordinates;
        if (github.coordinates.indexOf(REF_DELIMITER) == -1) {
            rawCoordinates = github.coordinates;
        } else {
            val refDelimiterIndex= github.coordinates.indexOf(REF_DELIMITER);
            rawCoordinates = github.coordinates.substring(0, refDelimiterIndex);
            val ref = Optional.of(github.coordinates.substring(refDelimiterIndex + 1))
                    .filter(StringUtils::isNotBlankOrEmpty)
                    .orElseThrow(() -> new CommandLine.ParameterException(spec.commandLine(), String.format("Github ref must be provided after '%s'", REF_DELIMITER)));
            githubBuilder.ref(ref);
        }
        val coordinates = rawCoordinates.split(String.valueOf(COORDINATES_DELIMITER));
        if (StringUtils.isBlankOrEmpty(coordinates[0])) {
            throw new CommandLine.ParameterException(spec.commandLine(), "Github owner must not be empty");
        }
        if (coordinates.length < 2 || (StringUtils.isBlankOrEmpty(coordinates[1]))) {
            throw new CommandLine.ParameterException(spec.commandLine(), "Github repository must not be empty");
        }
        return githubBuilder.owner(coordinates[0])
                .repository(coordinates[1]);
    }

}
