package com.optum.sourcehawk.cli;

import com.optum.sourcehawk.core.data.Pair;
import com.optum.sourcehawk.core.data.RemoteRef;
import com.optum.sourcehawk.core.repository.RemoteRepositoryFileReader;
import com.optum.sourcehawk.core.repository.RepositoryFileReader;
import lombok.val;
import picocli.CommandLine;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Optional;

/**
 * CLI entry point for executing Sourcehawk scan bitbucket command.
 *
 * @see ScanCommand
 *
 * @author Brian Wyka
 */
@CommandLine.Command(
        name = "bitbucket",
        aliases = "bb",
        description = "Runs a Sourcehawk scan on remote Bitbucket source code instead of local file system"
)
public class BitbucketScanCommand extends AbstractRemoteScanCommand {

    private static final String DEFAULT_BASE_URL = "https://bitbucket.org";
    private static final String DEFAULT_REF = "main";

    /**
     * The Bitbucket options
     */
    @SuppressWarnings("unused")
    @CommandLine.ArgGroup(exclusive = false)
    private CommandOptions.Bitbucket bitbucket;

    /**
     * Bootstrap the command
     *
     * @param args the command line args
     */
    public static void main(final String... args) {
        AbstractExecCommand.execute(new BitbucketScanCommand(), args);
    }

    /** {@inheritDoc} */
    @Override
    protected RepositoryFileReader createRepositoryFileReader(final RemoteRef remoteRef) {
        val rawFileUrlTemplate = Optional.ofNullable(bitbucket.serverUrl)
            .map(bitbucketServerUrl -> String.format("%s/rest/api/1.0/projects/%s/repos/%s/raw/%%s?at=%s",
                bitbucketServerUrl, remoteRef.getNamespace(), remoteRef.getRepository(), remoteRef.getRef()))
            .orElseGet(() -> String.format("%s/api/2.0/repositories/%s/%s/src/%s/%%s", DEFAULT_BASE_URL, remoteRef.getNamespace(), remoteRef.getRepository(), remoteRef.getRef()));
        val requestProperties = new HashMap<String, String>();
        requestProperties.put("Accept", "text/plain");
        if (bitbucket.token != null) {
            val authScheme = Optional.ofNullable(bitbucket.authScheme)
                .orElse(CommandOptions.Bitbucket.DEFAULT_AUTH_SCHEME);
            requestProperties.put("Authorization", String.format("%s %s", authScheme, bitbucket.token));
        }
        return new RemoteRepositoryFileReader(rawFileUrlTemplate, requestProperties);
    }

    /** {@inheritDoc} */
    @Override
    protected Pair<String, String> getRawRemoteReference() {
        return Pair.of(bitbucket.remoteReference, DEFAULT_REF);
    }

}
