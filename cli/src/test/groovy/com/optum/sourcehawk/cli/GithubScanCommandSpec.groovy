package com.optum.sourcehawk.cli

import picocli.CommandLine
import spock.lang.Unroll

class GithubScanCommandSpec extends CliBaseSpecification {

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
        coordinates << [ "", "owner", "owner/", "owner/ ", "owner/repo@" ]
    }

}
