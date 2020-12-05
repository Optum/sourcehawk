package com.optum.sourcehawk.cli


import spock.lang.Unroll

class SourcehawkSpec extends CliBaseSpecification {

    @Unroll
    def "main: #arg"() {
        given:
        String[] args = new String[] { arg }

        when:
        Sourcehawk.main(args)

        then:
        SystemExit systemExit = thrown(SystemExit)
        systemExit.status == 0

        where:
        arg << [ "-h", "--help", "-V", "--version" ]
    }

    def "main: empty args (prints help)"() {
        when:
        Sourcehawk.main(new String[0])

        then:
        SystemExit systemExit = thrown(SystemExit)
        systemExit.status == 2
    }

    @Unroll
    def "main: unknown arg (parse error)"() {
        given:
        String[] args = new String[] { arg }

        when:
        Sourcehawk.main(args)

        then:
        SystemExit systemExit = thrown(SystemExit)
        systemExit.status == 2

        where:
        arg << [ "unknown", "-u", "--unknown" ]
    }

    @Unroll
    def "main (sub command): #commandAndArgs"() {
        when:
        Sourcehawk.main(commandAndArgs)

        then:
        SystemExit systemExit = thrown(SystemExit)
        systemExit.status == 0

        where:
        commandAndArgs << [
                ["help"] as String[],
                ["scan", repositoryRoot] as String[],
                ["validate-config", repositoryRoot] as String[],
                ["fix", "--dry-run", repositoryRoot] as String[],
        ]
    }

}
