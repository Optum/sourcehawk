package com.optum.sourcehawk.cli

import com.optum.sourcehawk.core.scan.ScanResult
import com.optum.sourcehawk.exec.ExecOptions
import picocli.CommandLine
import spock.lang.Unroll

class ScanCommandSpec extends CliBaseSpecification {

    @Unroll
    def "main: #helpArg"() {
        given:
        String[] args = new String[] { helpArg }

        when:
        ScanCommand.main(args)

        then:
        SystemExit systemExit = thrown(SystemExit)
        systemExit.status == 0

        where:
        helpArg << ["-h", "--help" ]
    }

    @Unroll
    def "commandLine.execute: #args (passed)"() {
        given:
        CommandLine commandLine = new CommandLine(new ScanCommand())

        when:
        int exitCode = commandLine.execute(args)

        then:
        exitCode == 0

        where:
        args << [
                [ repositoryRoot.toString() ] as String[],
                [ "-c", "sourcehawk.yml", repositoryRoot.toString() ] as String[],
                [ "--config-file", "sourcehawk.yml", repositoryRoot.toString() ] as String[],
                [ "-v", "HIGH", repositoryRoot.toString() ] as String[],
                [ "--verbosity", "HIGH", repositoryRoot.toString() ] as String[],
                [ "-f", "JSON", repositoryRoot.toString() ] as String[],
                [ "--output-format", "JSON", repositoryRoot.toString() ] as String[],
                [ "-w", repositoryRoot.toString() ] as String[],
                [ "--fail-on-warnings", repositoryRoot.toString() ] as String[]
        ]
    }

    def "commandLine.execute: enforcer fails (failed)"() {
        given:
        String[] args = ["-c", "sourcehawk-failed-enforcer.yml", testResourcesRoot.toString()]
        CommandLine commandLine = new CommandLine(new ScanCommand())

        when:
        int exitCode = commandLine.execute(args)

        then:
        exitCode == 1
    }

    def "commandLine.execute: configuration file not found (failed)"() {
        given:
        String[] args = ["-c", "sourcehawk.yml"]
        CommandLine commandLine = new CommandLine(new ScanCommand())

        when:
        int exitCode = commandLine.execute(args)

        then:
        exitCode == 1
    }

    @Unroll
    def "commandLine.execute: parse error"() {
        given:
        String[] args = new String[] { arg }
        CommandLine commandLine = new CommandLine(new ScanCommand())

        when:
        int exitCode = commandLine.execute(args)

        then:
        exitCode == 2

        where:
        arg << [ "-n", "--none" ]
    }

    def "commandLine.execute: multiple exclusive options"() {
        given:
        String[] args = new String[] { "-U", "http://www.example.com", "-c", "sourcehawk.yml" }
        CommandLine commandLine = new CommandLine(new ScanCommand())

        when:
        int exitCode = commandLine.execute(args)

        then:
        exitCode == 2
    }

    def "execute - exception"() {
        given:
        ExecOptions execOptions = null

        when:
        ScanResult scanResult = ScanCommand.execute(execOptions)

        then:
        scanResult
        !scanResult.passed

        and:
        noExceptionThrown()
    }

}
