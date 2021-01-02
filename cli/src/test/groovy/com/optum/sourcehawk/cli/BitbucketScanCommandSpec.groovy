package com.optum.sourcehawk.cli

import com.optum.sourcehawk.core.data.Pair
import com.optum.sourcehawk.core.data.RemoteRef
import com.optum.sourcehawk.core.repository.RepositoryFileReader
import picocli.CommandLine
import spock.lang.Unroll

class BitbucketScanCommandSpec extends CliBaseSpecification {

    def "getRawRemoteReference"() {
        when:
        Pair<RemoteRef.Type, String> rawRemoteReference = new BitbucketScanCommand(bitbucket: new CommandOptions.Bitbucket(remoteReference: "owner/repo@master")).getRawRemoteReference()

        then:
        rawRemoteReference.left == RemoteRef.Type.BITBUCKET
        rawRemoteReference.right == "owner/repo@master"
    }

    @Unroll
    def "main: #helpArg"() {
        given:
        String[] args = new String[] { helpArg }

        when:
        BitbucketScanCommand.main(args)

        then:
        SystemExit systemExit = thrown(SystemExit)
        systemExit.status == 0

        where:
        helpArg << ["-h", "--help" ]
    }

    def "main: error"() {
        given:
        String[] args = new String[] { }

        when:
        BitbucketScanCommand.main(args)

        then:
        SystemExit systemExit = thrown(SystemExit)
        systemExit.status == 1
    }

    @Unroll
    def "commandLine.execute: #coordinates (invalid github coordinates)"() {
        given:
        CommandLine commandLine = new CommandLine(new BitbucketScanCommand(parentCommand: new ScanCommand()))

        when:
        int exitCode = commandLine.execute([ coordinates ] as String[])

        then:
        exitCode == 2

        where:
        coordinates << [ "project", "project/", "project/repo@" ]
    }

    def "createRepositoryFileReader"() {
        given:
        String rawReference = "project/repo@master"
        BitbucketScanCommand githubScanCommand = new BitbucketScanCommand(bitbucket: new CommandOptions.Bitbucket(remoteReference: "owner/repo@main"))

        when:
        RepositoryFileReader repositoryFileReader = githubScanCommand.createRepositoryFileReader(RemoteRef.parse(RemoteRef.Type.BITBUCKET, rawReference))

        then:
        repositoryFileReader
    }

    def "createRepositoryFileReader - enterprise"() {
        given:
        String rawReference = "project/repo@master"
        BitbucketScanCommand githubScanCommand = new BitbucketScanCommand(bitbucket:
                new CommandOptions.Bitbucket(remoteReference: "owner/repo@main", serverUrl: new URL("https://bitbucket.example.com"))
        )

        when:
        RepositoryFileReader repositoryFileReader = githubScanCommand.createRepositoryFileReader(RemoteRef.parse(RemoteRef.Type.BITBUCKET, rawReference))

        then:
        repositoryFileReader
    }

}
