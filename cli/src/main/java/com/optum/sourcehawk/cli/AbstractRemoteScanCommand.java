package com.optum.sourcehawk.cli;

import com.optum.sourcehawk.core.constants.SourcehawkConstants;
import com.optum.sourcehawk.core.data.Pair;
import com.optum.sourcehawk.core.data.RemoteRef;
import com.optum.sourcehawk.core.repository.RepositoryFileReader;
import com.optum.sourcehawk.core.utils.StringUtils;
import lombok.val;
import picocli.CommandLine;

import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * Abstract remote exec command
 *
 * @author Brian Wyka
 */
@CommandLine.Command(
        mixinStandardHelpOptions = true,
        subcommands = CommandLine.HelpCommand.class
)
abstract class AbstractRemoteScanCommand implements Callable<Integer> {

    /**
     * The command spec
     */
    @SuppressWarnings("unused")
    @CommandLine.Spec
    protected CommandLine.Model.CommandSpec spec;

    /**
     * Reference to the parent scan command
     */
    @SuppressWarnings("unused")
    @CommandLine.ParentCommand
    protected ScanCommand parentCommand;

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
                .filter(AbstractRemoteScanCommand::configFileProvided)
                .isPresent();
        val remoteRef = validateAndParseRemoteRef();
        execOptionsBuilder.remoteRef(remoteRef);
        val repositoryFileReader = createRepositoryFileReader(remoteRef);
        execOptionsBuilder.repositoryFileReader(repositoryFileReader);
        if (StringUtils.equals(SourcehawkConstants.DEFAULT_CONFIG_FILE_NAME, parentExecOptions.getConfigurationFileLocation()) && !configFileProvided) {
            execOptionsBuilder.configurationFileLocation(repositoryFileReader.getAbsoluteLocation(SourcehawkConstants.DEFAULT_CONFIG_FILE_NAME));
        }
        return parentCommand.call(execOptionsBuilder.build());
    }

    /**
     * Create the repository file reader based off the remote reference
     *
     * @param remoteRef the remote reference
     * @return the repository file reader
     */
    protected abstract RepositoryFileReader createRepositoryFileReader(final RemoteRef remoteRef);

    /**
     * Get the raw remote reference
     *
     * @return the raw remote reference
     */
    protected abstract Pair<String, String> getRawRemoteReference();

    /**
     * Parse the coordinates to github options
     *
     * @return the remote reference
     */
    private RemoteRef validateAndParseRemoteRef() {
        val rawRemoteReference = getRawRemoteReference();
        try {
            return RemoteRef.parse(rawRemoteReference.getLeft(), rawRemoteReference.getRight());
        } catch (final IllegalArgumentException e) {
            throw new CommandLine.ParameterException(spec.commandLine(), e.getMessage());
        }
    }

    /**
     * Determine if the config file was provided within the command line options
     *
     * @param parseResult the command parse result
     * @return true if config file was provided, false otherwise
     */
    private static boolean configFileProvided(final CommandLine.ParseResult parseResult) {
        return parseResult.hasMatchedOption(CommandOptions.ConfigFile.OPTION_PATH) || parseResult.hasMatchedOption(CommandOptions.ConfigFile.OPTION_PATH_LONG);
    }

}
