package com.optum.sourcehawk.cli

import com.optum.sourcehawk.core.constants.SourcehawkConstants
import com.optum.sourcehawk.core.scan.OutputFormat
import com.optum.sourcehawk.core.scan.Verbosity
import com.optum.sourcehawk.exec.ExecOptions
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Path
import java.nio.file.Paths

class AbstractExecCommandSpec extends Specification {

    def "buildExecOptions - defaults"() {
        given:
        AbstractExecCommand command = new AbstractExecCommand() {
            @Override
            Integer call() throws Exception {
                return 0
            }
        }

        when:
        ExecOptions execOptions = command.buildExecOptions()

        then:
        execOptions
        execOptions.repositoryRoot == Paths.get(".")
        execOptions.outputFormat == OutputFormat.CONSOLE
        execOptions.configurationFileLocation == "sourcehawk.yml"
        execOptions.verbosity == Verbosity.HIGH
        !execOptions.failOnWarnings
    }

    def "buildExecOptions - provided options"() {
        given:
        AbstractExecCommand command = new ScanCommand(
                exec: new CommandOptions.Exec(
                        configFile: new CommandOptions.ConfigFile(
                                path: Paths.get("sh.yml")
                        ),
                        outputFormat: OutputFormat.TEXT,
                        verbosity: Verbosity.MEDIUM,
                        failOnWarnings: false
                )
        )

        when:
        ExecOptions execOptions = command.buildExecOptions()

        then:
        execOptions
        execOptions.repositoryRoot == Paths.get(".")
        execOptions.outputFormat == OutputFormat.TEXT
        execOptions.configurationFileLocation == "sh.yml"
        execOptions.verbosity == Verbosity.MEDIUM
        !execOptions.failOnWarnings
    }

    @Unroll
    def "buildExecOptions - provided options - repository path null/empty"() {
        given:
        AbstractExecCommand command = new ScanCommand(
                exec: new CommandOptions.Exec(
                    configFile: new CommandOptions.ConfigFile(
                            path: Paths.get("sh.yml")
                    ),
                    outputFormat: OutputFormat.TEXT,
                    verbosity: Verbosity.MEDIUM,
                    failOnWarnings: false
                )
        )

        when:
        ExecOptions execOptions = command.buildExecOptions()

        then:
        execOptions
        execOptions.repositoryRoot == Paths.get(".")
        execOptions.outputFormat == OutputFormat.TEXT
        execOptions.configurationFileLocation == "sh.yml"
        execOptions.verbosity == Verbosity.MEDIUM
        !execOptions.failOnWarnings

        where:
        repositoryRootPath << [ null as Path, Paths.get("") ]
    }

    @Unroll
    def "buildExecOptions - provided options (#outputFormat) - verbosity downgraded"() {
        given:
        AbstractExecCommand command = new FixCommand(
                exec: new CommandOptions.Exec(
                        configFile: new CommandOptions.ConfigFile(
                                url: new URL("https://raw.githubusercontent.com/optum/sourcehawk-parent/sourcehawk.yml")
                        ),
                        outputFormat: outputFormat,
                        verbosity: Verbosity.HIGH
                )
        )

        when:
        ExecOptions execOptions = command.buildExecOptions()

        then:
        execOptions
        execOptions.repositoryRoot == Paths.get(".")
        execOptions.outputFormat == outputFormat
        execOptions.configurationFileLocation == "https://raw.githubusercontent.com/optum/sourcehawk-parent/sourcehawk.yml"
        execOptions.verbosity == Verbosity.ZERO

        where:
        outputFormat << [ OutputFormat.JSON, OutputFormat.MARKDOWN ]
    }

}
