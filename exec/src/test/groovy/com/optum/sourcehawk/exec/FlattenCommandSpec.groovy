package com.optum.sourcehawk.exec


import spock.lang.Unroll

class FlattenCommandSpec extends CliBaseSpecification {

    def setup() {
        new File(repositoryRoot.toString() + "/sourcehawk-flattened.yml").delete()
        new File(repositoryRoot.toString() + "/exec/sourcehawk-flattened.yml").delete()
    }

    @Unroll
    def "main: #helpArg"() {
        given:
        String[] args = new String[]{helpArg}

        when:
        FlattenCommand.main(args)

        then:
        CliBaseSpecification.SystemExit systemExit = thrown(CliBaseSpecification.SystemExit)
        systemExit.status == 0

        where:
        helpArg << ["-h", "--help"]
    }

    def "main: dry run"() {
        given:
        String[] args = ["--dry-run", repositoryRoot.toString()]

        when:
        FlattenCommand.main(args)

        then:
        CliBaseSpecification.SystemExit systemExit = thrown(CliBaseSpecification.SystemExit)
        systemExit.status == 0
    }

    def "main: dry run verbose"() {
        given:
        String[] args = ["--dry-run", "-v", "HIGH", repositoryRoot.toString()]

        when:
        FlattenCommand.main(args)

        then:
        CliBaseSpecification.SystemExit systemExit = thrown(CliBaseSpecification.SystemExit)
        systemExit.status == 0
    }

    def "main: real run"() {
        given:
        String[] args = ["-c", repositoryRoot.toString() + "/sourcehawk.yml"]

        when:
        def exists = new File(repositoryRoot.toString() + "/exec/sourcehawk-flattened.yml").exists()

        then:
        !exists

        when:
        FlattenCommand.main(args)

        then:
        CliBaseSpecification.SystemExit systemExit = thrown(CliBaseSpecification.SystemExit)
        systemExit.status == 0

        and:
        new File(repositoryRoot.toString() + "/exec/sourcehawk-flattened.yml").exists()
    }

    def "main: real run verbose"() {
        given:
        String[] args = ["-v", "HIGH", "-c", repositoryRoot.toString() + "/sourcehawk.yml"]

        when:
        def exists = new File(repositoryRoot.toString() + "/exec/sourcehawk-flattened.yml").exists()

        then:
        !exists

        when:
        FlattenCommand.main(args)

        then:
        CliBaseSpecification.SystemExit systemExit = thrown(CliBaseSpecification.SystemExit)
        systemExit.status == 0

        and:
        new File(repositoryRoot.toString() + "/exec/sourcehawk-flattened.yml").exists()
    }

    def "main: configuration file not found (failed)"() {
        given:
        String[] args = ["-c", "sourcehawk.yml"]

        when:
        FlattenCommand.main(args)

        then:
        CliBaseSpecification.SystemExit systemExit = thrown(CliBaseSpecification.SystemExit)
        systemExit.status == 1
    }

    @Unroll
    def "main: parse error"() {
        given:
        String[] args = new String[]{arg}

        when:
        FlattenCommand.main(args)

        then:
        CliBaseSpecification.SystemExit systemExit = thrown(CliBaseSpecification.SystemExit)
        systemExit.status == 2

        where:
        arg << ["-n", "--none"]
    }
}
