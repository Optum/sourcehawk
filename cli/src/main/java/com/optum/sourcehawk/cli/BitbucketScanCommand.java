package com.optum.sourcehawk.cli;

import com.optum.sourcehawk.core.constants.SourcehawkConstants;
import com.optum.sourcehawk.core.data.Pair;
import com.optum.sourcehawk.core.data.RemoteRef;
import com.optum.sourcehawk.core.repository.BitbucketRepositoryFileReader;
import com.optum.sourcehawk.core.repository.RepositoryFileReader;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine;

import java.net.URL;
import java.util.Optional;

/**
 * CLI entry point for executing Sourcehawk scan bitbucket command.
 *
 * @see ScanCommand
 *
 * @author Brian Wyka
 */
@Slf4j
@CommandLine.Command(
        name = "bitbucket",
        aliases = "bb",
        description = "Runs a Sourcehawk scan on remote Bitbucket source code instead of local file system"
)
public class BitbucketScanCommand extends AbstractRemoteScanCommand {

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
        if (bitbucket.serverUrl != null) {
            return new BitbucketRepositoryFileReader(bitbucket.token, bitbucket.serverUrl.toString(), remoteRef);
        }
        return new BitbucketRepositoryFileReader(bitbucket.token, remoteRef);
    }

    /** {@inheritDoc} */
    @Override
    protected Pair<RemoteRef.Type, String> getRawRemoteReference() {
        return Pair.of(RemoteRef.Type.BITBUCKET, bitbucket.remoteReference);
    }

    /** {@inheritDoc} */
    @Override
    protected String constructRemoteConfigFileLocation(final RemoteRef remoteRef) {
        val bitbucketBaseUrl = Optional.ofNullable(bitbucket.serverUrl)
                .map(URL::toString)
                .orElseGet(RemoteRef.Type.BITBUCKET::getBaseUrl);
        return BitbucketRepositoryFileReader.constructBaseUrl(remoteRef, bitbucketBaseUrl) + SourcehawkConstants.DEFAULT_CONFIG_FILE_NAME;
    }

}
