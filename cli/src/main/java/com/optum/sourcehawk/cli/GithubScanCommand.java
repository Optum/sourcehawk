package com.optum.sourcehawk.cli;

import com.optum.sourcehawk.core.data.Pair;
import com.optum.sourcehawk.core.data.RemoteRef;
import com.optum.sourcehawk.core.repository.RemoteRepositoryFileReader;
import com.optum.sourcehawk.core.repository.RepositoryFileReader;
import lombok.val;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.Optional;

/**
 * CLI entry point for executing Sourcehawk scan github command.
 *
 * @see ScanCommand
 *
 * @author Brian Wyka
 */
@CommandLine.Command(
        name = "github",
        aliases = "gh",
        description = "Runs a Sourcehawk scan on remote Github source code instead of local file system"
)
public class GithubScanCommand extends AbstractRemoteScanCommand {

    private static final String DEFAULT_BASE_URL = "raw.githubusercontent.com";
    private static final String DEFAULT_REF = "main";
    private static final String AUTHORIZATION_TOKEN_PREFIX = "Bearer";

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
        val baseUrl = Optional.ofNullable(github.enterpriseUrl)
            .map(githubEnterpriseUrl -> String.format("%s/raw", github.enterpriseUrl))
            .orElse(DEFAULT_BASE_URL);
        val rawFileUrlTemplate  = String.format("%s/%s/%s/%s/%%s", baseUrl, remoteRef.getNamespace(), remoteRef.getRepository(), remoteRef.getRef());
        val requestProperties = new HashMap<String, String>();
        requestProperties.put("Accept", "text/plain");
        if (github.token != null) {
            requestProperties.put("Authorization", String.format("%s %s", AUTHORIZATION_TOKEN_PREFIX, github.token));
        }
        return new RemoteRepositoryFileReader(rawFileUrlTemplate, requestProperties);
    }

    /** {@inheritDoc} */
    @Override
    protected Pair<String, String> getRawRemoteReference() {
        return Pair.of(github.remoteReference, DEFAULT_REF);
    }

}
