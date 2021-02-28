package com.optum.sourcehawk.cli

import com.fasterxml.jackson.core.JsonLocation
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import com.optum.sourcehawk.enforcer.file.FileEnforcer
import com.optum.sourcehawk.core.protocol.file.FileProtocol
import org.spockframework.util.IoUtil
import spock.lang.Unroll

class ValidateConfigCommandSpec extends CliBaseSpecification {

    @Unroll
    def "main: #helpArg"() {
        given:
        String[] args = new String[] { helpArg }

        when:
        ValidateConfigCommand.main(args)

        then:
        SystemExit systemExit = thrown(SystemExit)
        systemExit.status == 0

        where:
        helpArg << ["-h", "--help" ]
    }

    def "main: stdin (passed)"() {
        given:
        System.in = IoUtil.getResourceAsStream("/sourcehawk-simple.yml")
        OutputStream stdOut = new ByteArrayOutputStream()
        System.out = new PrintStream(stdOut)

        when:
        ValidateConfigCommand.main("-")

        then:
        SystemExit systemExit = thrown(SystemExit)
        systemExit.status == 0

        and:
        stdOut.toString().trim() == "Congratulations, you have created a valid configuration file"
    }

    @Unroll
    def "main: #args (passed)"() {
        given:
        OutputStream stdOut = new ByteArrayOutputStream()
        System.out = new PrintStream(stdOut)

        when:
        ValidateConfigCommand.main(args)

        then:
        SystemExit systemExit = thrown(SystemExit)
        systemExit.status == 0

        and:
        stdOut.toString().trim() == "Congratulations, you have created a valid configuration file"

        where:
        args << [
                [ repositoryRoot.toString() ] as String[],
                [ repositoryRoot.resolve("sourcehawk.yml").toString() ] as String[]
        ]
    }

    def "main: configuration file not found (failed)"() {
        given:
        String[] args = [".not-found.yml"]
        OutputStream stdErr = new ByteArrayOutputStream()
        System.err = new PrintStream(stdErr)

        when:
        ValidateConfigCommand.main(args)

        then:
        SystemExit systemExit = thrown(SystemExit)
        systemExit.status == 2

        when:
        String stdErrContents = new String(stdErr.toByteArray()).trim()
                .replaceAll("\\u001b", "")
                .replaceAll("\\[\\d+m", "")

        then:
        stdErrContents == "Configuration not provided through stdin or via file path"
    }

    def "main: configuration file not found - directory (failed)"() {
        given:
        String[] args = [ testResourcesRoot.toString() ]
        OutputStream stdErr = new ByteArrayOutputStream()
        System.err = new PrintStream(stdErr)

        when:
        ValidateConfigCommand.main(args)

        then:
        SystemExit systemExit = thrown(SystemExit)
        systemExit.status == 2

        when:
        String stdErrContents = new String(stdErr.toByteArray()).trim()
                .replaceAll("\\u001b", "")
                .replaceAll("\\[\\d+m", "")

        then:
        stdErrContents == "Configuration file is a directory and does not contain sourcehawk.yml file"
    }

    def "main: invalid config - file protocol invalid (required field missing)"() {
        given:
        String[] args = [ testResourcesRoot.resolve("sourcehawk-invalid-protocol-missing-required.yml").toString() ]

        when:
        ValidateConfigCommand.main(args)

        then:
        SystemExit systemExit = thrown(SystemExit)
        systemExit.status == 1
    }

    def "main: invalid config - file protocol invalid"() {
        given:
        String[] args = [ testResourcesRoot.resolve("sourcehawk-invalid-protocol.yml").toString() ]

        when:
        ValidateConfigCommand.main(args)

        then:
        SystemExit systemExit = thrown(SystemExit)
        systemExit.status == 1
    }

    def "main: invalid config - invalid enforcer class"() {
        given:
        String[] args = [ testResourcesRoot.resolve("sourcehawk-invalid-enforcer.yml") ]

        when:
        ValidateConfigCommand.main(args)

        then:
        SystemExit systemExit = thrown(SystemExit)
        systemExit.status == 1
    }

    def "main: invalid config - invalid enforcer property"() {
        given:
        String[] args = [ testResourcesRoot.resolve("sourcehawk-invalid-enforcer-property.yml") ]

        when:
        ValidateConfigCommand.main(args)

        then:
        SystemExit systemExit = thrown(SystemExit)
        systemExit.status == 1
    }

    def "main: invalid config - empty"() {
        given:
        String[] args = [ testResourcesRoot.resolve("sourcehawk-empty.yml") ]

        when:
        ValidateConfigCommand.main(args)

        then:
        SystemExit systemExit = thrown(SystemExit)
        systemExit.status == 0
    }

    def "compileFileEnforcerErrors - fileProtocols invalid"() {
        given:
        Map<String, Object> enforcer = [
                "enforcer": ".common.StringPropertyEquals",
                "property-name": "foo",
                "expected-property-value-INCORRECT": "bar",
        ]
        FileProtocol fileProtocol = FileProtocol.builder()
                .name("lombok")
                .repositoryPath("lombok.config")
                .enforcers([ enforcer ])
                .build()
        Collection<FileProtocol> fileProtocols = [fileProtocol ]

        when:
        Collection<String> errors = ValidateConfigCommand.compileFileEnforcerErrors(fileProtocols)

        then:
        errors
        errors.size() == 1
        errors[0] == "Unrecognized property 'expected-property-value-INCORRECT' in file protocol 'lombok'"
    }

    def "compileFileEnforcerErrors - fileProtocols null/empty"() {
        expect:
        ValidateConfigCommand.compileFileEnforcerErrors(null).isEmpty()
        ValidateConfigCommand.compileFileEnforcerErrors([]).isEmpty()
    }

    def "captureEnforcerConversionError - valid enforcer definition"() {
        given:
        FileProtocol fileProtocol = FileProtocol.builder()
                .name("lombok")
                .repositoryPath("lombok.config")
                .build()
        Map<String, Object> enforcer = [
                "enforcer": ".common.StringPropertyEquals",
                "property-name": "foo",
                "expected-property-value": "bar",
        ]

        when:
        Optional<String> error = ValidateConfigCommand.captureEnforcerConversionError(fileProtocol, enforcer)

        then:
        !error.isPresent()
    }

    def "captureEnforcerConversionError - invalid enforcer definition"() {
        given:
        FileProtocol fileProtocol = FileProtocol.builder()
                .name("lombok")
                .repositoryPath("lombok.config")
                .build()
        Map<String, Object> enforcer = [:]

        when:
        Optional<String> error = ValidateConfigCommand.captureEnforcerConversionError(fileProtocol, enforcer)

        then:
        error.isPresent()
    }

    def "deriveErrorMessage - UnrecognizedPropertyException"() {
        given:
        String context = "in file protocol Lombok"
        Throwable e = new UnrecognizedPropertyException(null, null, null, FileEnforcer, "property", [])

        when:
        String errorMessage = ValidateConfigCommand.deriveErrorMessage(context, e)

        then:
        errorMessage == "Unrecognized property 'property' ${context}"
    }

    def "deriveErrorMessage - InvalidTypeIdException"() {
        given:
        String context = "in file protocol pom.xml"
        Throwable e = new InvalidTypeIdException(null, null, null, "com.optum.sourcehawk.enforcer.file.common.UnknownEnforcer")

        when:
        String errorMessage = ValidateConfigCommand.deriveErrorMessage(context, e)

        then:
        errorMessage == "Unknown enforcer 'com.optum.sourcehawk.enforcer.file.common.UnknownEnforcer' ${context}"
    }

    def "deriveErrorMessage - JsonParseException"() {
        given:
        String context = "in global"
        Throwable e = new JsonParseException("error", JsonLocation.NA)

        when:
        String errorMessage = ValidateConfigCommand.deriveErrorMessage(context, e)

        then:
        errorMessage == "Parse error [Source: UNKNOWN; line: -1, column: -1] ${context}"
    }

    def "deriveErrorMessage - cause JsonProcessingException"() {
        given:
        String context = "in global"
        Throwable e = new IllegalArgumentException(new JsonParseException("error", JsonLocation.NA))

        when:
        String errorMessage = ValidateConfigCommand.deriveErrorMessage(context, e)

        then:
        errorMessage == "Parse error [Source: UNKNOWN; line: -1, column: -1] ${context}"
    }

    def "deriveErrorMessage - Exception"() {
        given:
        String context = "in global"
        Throwable e = new IllegalStateException("error",)

        when:
        String errorMessage = ValidateConfigCommand.deriveErrorMessage(context, e)

        then:
        errorMessage == e.message
    }

}
