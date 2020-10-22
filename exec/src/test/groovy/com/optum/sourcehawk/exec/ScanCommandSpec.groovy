package com.optum.sourcehawk.exec


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
    def "main: #args (passed)"() {
        when:
        ScanCommand.main(args)

        then:
        SystemExit systemExit = thrown(SystemExit)
        systemExit.status == 0

        where:
        args << [
                [ repositoryRoot.toString() ] as String[],
                [ "-c", "sourcehawk.yml", repositoryRoot.toString() ] as String[],
                [ "--config-file", "sourcehawk.yml", repositoryRoot.toString() ] as String[],
                [ "-v", "HIGH", repositoryRoot.toString() ] as String[],
                [ "--verbosity", "HIGH", repositoryRoot.toString() ] as String[],
                [ "-f", "JSON", repositoryRoot.toString() ] as String[],
                [ "--output-format", "JSON", repositoryRoot.toString() ] as String[]
        ]
    }

    def "main: enforcer fails (failed)"() {
        given:
        String[] args = ["-c", ".sourcehawk-failed-enforcer.yml", testResourcesRoot.toString()]

        when:
        ScanCommand.main(args)

        then:
        SystemExit systemExit = thrown(SystemExit)
        systemExit.status == 1
    }

    def "main: configuration file not found (failed)"() {
        given:
        String[] args = ["-c", "sourcehawk.yml"]

        when:
        ScanCommand.main(args)

        then:
        SystemExit systemExit = thrown(SystemExit)
        systemExit.status == 1
    }

    @Unroll
    def "main: parse error"() {
        given:
        String[] args = new String[] { arg }

        when:
        ScanCommand.main(args)

        then:
        SystemExit systemExit = thrown(SystemExit)
        systemExit.status == 2

        where:
        arg << [ "-n", "--none" ]
    }

}
