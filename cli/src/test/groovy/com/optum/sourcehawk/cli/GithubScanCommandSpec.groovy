package com.optum.sourcehawk.cli

import com.optum.sourcehawk.core.data.Pair
import com.optum.sourcehawk.core.data.RemoteRef
import com.optum.sourcehawk.core.repository.RepositoryFileReader
import picocli.CommandLine
import spock.lang.Unroll

class GithubScanCommandSpec extends CliBaseSpecification {

    def "getRawRemoteReference"() {
        when:
        Pair<RemoteRef.Type, String> rawRemoteReference = new GithubScanCommand(github: new CommandOptions.Github(remoteReference: "owner/repo@main")).getRawRemoteReference()

        then:
        rawRemoteReference.left == RemoteRef.Type.GITHUB
        rawRemoteReference.right == "owner/repo@main"
    }

    @Unroll
    def "main: #helpArg"() {
        given:
        String[] args = new String[] { helpArg }

        when:
        GithubScanCommand.main(args)

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
        GithubScanCommand.main(args)

        then:
        SystemExit systemExit = thrown(SystemExit)
        systemExit.status == 1
    }

    @Unroll
    def "commandLine.execute: #coordinates (invalid github coordinates)"() {
        given:
        CommandLine commandLine = new CommandLine(new GithubScanCommand(parentCommand: new ScanCommand()))

        when:
        int exitCode = commandLine.execute([ coordinates ] as String[])

        then:
        exitCode == 2

        where:
        coordinates << [ "owner", "owner/", "owner/repo@" ]
    }

    def "createRepositoryFileReader"() {
        given:
        String rawReference = "owner/repo@main"
        GithubScanCommand githubScanCommand = new GithubScanCommand(github: new CommandOptions.Github(remoteReference: "owner/repo@main"))

        when:
        RepositoryFileReader repositoryFileReader = githubScanCommand.createRepositoryFileReader(RemoteRef.parse(RemoteRef.Type.GITHUB, rawReference))

        then:
        repositoryFileReader
    }

    def "createRepositoryFileReader - enterprise"() {
        given:
        String rawReference = "owner/repo@main"
        GithubScanCommand githubScanCommand = new GithubScanCommand(github:
                new CommandOptions.Github(remoteReference: "owner/repo@main", enterpriseUrl: new URL("https://github.example.com"))
        )

        when:
        RepositoryFileReader repositoryFileReader = githubScanCommand.createRepositoryFileReader(RemoteRef.parse(RemoteRef.Type.GITHUB, rawReference))

        then:
        repositoryFileReader
    }

}
