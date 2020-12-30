package com.optum.sourcehawk.cli;

import com.optum.sourcehawk.core.constants.SourcehawkConstants;
import com.optum.sourcehawk.core.data.Pair;
import com.optum.sourcehawk.core.data.RemoteRef;
import com.optum.sourcehawk.core.repository.GithubRepositoryFileReader;
import com.optum.sourcehawk.core.repository.RepositoryFileReader;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine;

import java.net.URL;
import java.util.Optional;

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
        description = "Runs a Sourcehawk scan on remote Github source code instead of local file system"
)
public class GithubScanCommand extends AbstractRemoteScanCommand {

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

    /** {@inheritDoc} */
    @Override
    protected RepositoryFileReader createRepositoryFileReader(final RemoteRef remoteRef) {
        if (github.enterpriseUrl != null) {
            return new GithubRepositoryFileReader(github.token, github.enterpriseUrl.toString(), remoteRef);
        }
        return new GithubRepositoryFileReader(github.token, remoteRef);
    }

    /** {@inheritDoc} */
    @Override
    protected Pair<RemoteRef.Type, String> getRawRemoteReference() {
        return Pair.of(RemoteRef.Type.GITHUB, github.remoteReference);
    }

    /** {@inheritDoc} */
    @Override
    protected String constructRemoteConfigFileLocation(final RemoteRef remoteRef) {
        val githubEnterpriseUrl = Optional.ofNullable(github.enterpriseUrl).map(URL::toString);
        val githubRepoBaseUrl = GithubRepositoryFileReader.constructBaseUrl(
                githubEnterpriseUrl.orElseGet(RemoteRef.Type.GITHUB::getBaseUrl),
                githubEnterpriseUrl.isPresent(),
                remoteRef
        );
        return githubRepoBaseUrl + SourcehawkConstants.DEFAULT_CONFIG_FILE_NAME;
    }

}
