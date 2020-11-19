package com.optum.sourcehawk.exec

import com.optum.sourcehawk.core.scan.FlattenConfigResult
import spock.lang.Unroll

class FlattenConfigCommandSpec extends CliBaseSpecification {

    def setup() {
        new File(repositoryRoot.toString() + "/sourcehawk-flattened.yml").delete()
        new File(repositoryRoot.toString() + "/exec/sourcehawk-flattened.yml").delete()
    }

    @Unroll
    def "main: #helpArg"() {
        given:
        String[] args = new String[]{helpArg}

        when:
        FlattenConfigCommand.main(args)

        then:
        CliBaseSpecification.SystemExit systemExit = thrown(CliBaseSpecification.SystemExit)
        systemExit.status == 0

        where:
        helpArg << ["-h", "--help"]
    }

    def "main: console output"() {
        given:
        OutputStream stdOut = new ByteArrayOutputStream()
        System.out = new PrintStream(stdOut)
        String[] args = ["-c", repositoryRoot.toString() + "/sourcehawk.yml"]

        when:
        FlattenConfigCommand.main(args)

        then:
        CliBaseSpecification.SystemExit systemExit = thrown(CliBaseSpecification.SystemExit)
        systemExit.status == 0
        stdOut.toString().trim() == new File(testResourcesRoot.toString() + "/flattened/sourcehawk-flattened-base.yml").text.trim()
    }

    def "main: console output override"() {
        given:
        OutputStream stdOut = new ByteArrayOutputStream()
        System.out = new PrintStream(stdOut)
        String[] args = ["-c", testResourcesRoot.toString() + "/sourcehawk-override.yml"]

        when:
        FlattenConfigCommand.main(args)

        then:
        CliBaseSpecification.SystemExit systemExit = thrown(CliBaseSpecification.SystemExit)
        systemExit.status == 0
        stdOut.toString().trim() == new File(testResourcesRoot.toString() + "/flattened/sourcehawk-flattened-override.yml").text.trim()
    }

    def "main: console output not exists"() {
        given:
        String[] args = ["-c", repositoryRoot.toString() + "/sourcehawkasdfasdf.yml"]

        when:
        FlattenConfigCommand.main(args)

        then:
        CliBaseSpecification.SystemExit systemExit = thrown(CliBaseSpecification.SystemExit)
        systemExit.status == 1
    }

    def "main: console remote"() {
        given:
        String[] args = ["-cfu", "https://raw.githubusercontent.com/optum/sourcehawk-parent/main/.sourcehawk/config.yml"]

        when:
        FlattenConfigCommand.main(args)

        then:
        CliBaseSpecification.SystemExit systemExit = thrown(CliBaseSpecification.SystemExit)
        systemExit.status == 0
    }

    def "main: real run output file"() {
        given:
        String[] args = ["-o", repositoryRoot.toString() + "/exec/sourcehawk-flattened.yml",
                         "-c", repositoryRoot.toString() + "/sourcehawk.yml"]

        when:
        def exists = new File(repositoryRoot.toString() + "/exec/sourcehawk-flattened.yml").exists()

        then:
        !exists

        when:
        FlattenConfigCommand.main(args)

        then:
        CliBaseSpecification.SystemExit systemExit = thrown(CliBaseSpecification.SystemExit)
        systemExit.status == 0

        and:
        new File(repositoryRoot.toString() + "/exec/sourcehawk-flattened.yml").exists()
    }

    def "main: configuration file not found (failed)"() {
        given:
        String[] args = ["-o", repositoryRoot.toString() + "/exec/sourcehawk-flattened.yml",
                         "-c", "sourcehawk.yml"]

        when:
        FlattenConfigCommand.main(args)

        then:
        CliBaseSpecification.SystemExit systemExit = thrown(CliBaseSpecification.SystemExit)
        systemExit.status == 1
    }

    def "main: configuration file blank not found (failed)"() {
        given:
        String[] args = ["-c", "",
                         "-o", repositoryRoot.toString() + "/exec/sourcehawk-flattened.yml"]

        when:
        FlattenConfigCommand.main(args)

        then:
        CliBaseSpecification.SystemExit systemExit = thrown(CliBaseSpecification.SystemExit)
        systemExit.status == 1
    }


    @Unroll
    def "main: parse error"() {
        given:
        String[] args = new String[]{arg}

        when:
        FlattenConfigCommand.main(args)

        then:
        CliBaseSpecification.SystemExit systemExit = thrown(CliBaseSpecification.SystemExit)
        systemExit.status == 2

        where:
        arg << ["-n", "--none"]
    }

    def "handleException"() {
        when:
        def result = FlattenConfigCommand.handleException("test", new IOException("message"))

        then:
        result.error
        result.formattedMessage == "Error flattening sourcehawk configuration at test with error: message"
    }
    def "handleDryRunOutput"() {
        when:
        FlattenConfigCommand.handleDryRunOutput(null)

        then:
        notThrown(Exception)

        when:
        FlattenConfigCommand.handleDryRunOutput(FlattenConfigResult.success(null))

        then:
        notThrown(Exception)

    }
}
