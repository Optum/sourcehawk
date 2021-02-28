package com.optum.sourcehawk.cli

import com.optum.sourcehawk.core.constants.SourcehawkConstants
import spock.lang.Unroll

import java.nio.file.Path
import java.nio.file.Paths

class FlattenConfigCommandSpec extends CliBaseSpecification {

    def setup() {
        new File(repositoryRoot.toString() + "/sourcehawk-flattened.yml").delete()
        new File(repositoryRoot.toString() + "/cli/sourcehawk-flattened.yml").delete()
    }

    @Unroll
    def "main: #helpArg"() {
        given:
        String[] args = new String[]{helpArg}

        when:
        FlattenConfigCommand.main(args)

        then:
        SystemExit systemExit = thrown(SystemExit)
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
        SystemExit systemExit = thrown(SystemExit)
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
        SystemExit systemExit = thrown(SystemExit)
        systemExit.status == 0
        stdOut.toString().trim() == new File(testResourcesRoot.toString() + "/flattened/sourcehawk-flattened-override.yml").text.trim()
    }

    def "main: console output not exists"() {
        given:
        OutputStream stdErr = new ByteArrayOutputStream()
        System.err = new PrintStream(stdErr)
        String[] args = ["-c", repositoryRoot.toString() + "/sourcehawkasdfasdf.yml"]

        when:
        FlattenConfigCommand.main(args)

        then:
        SystemExit systemExit = thrown(SystemExit)
        systemExit.status == 1

        when:
        String stdErrContents = new String(stdErr.toByteArray()).trim()
                .replaceAll("\\u001b", "")
                .replaceAll("\\[\\d+m", "")

        then:
        stdErrContents == "Configuration file not found: ${repositoryRoot.toString()}/sourcehawkasdfasdf.yml\nConfiguration file ${repositoryRoot.toString()}/sourcehawkasdfasdf.yml not found or invalid"
    }

    def "main: console remote"() {
        given:
        String[] args = ["-U", "https://raw.githubusercontent.com/optum/sourcehawk-parent/main/.sourcehawk/config.yml"]

        when:
        FlattenConfigCommand.main(args)

        then:
        SystemExit systemExit = thrown(SystemExit)
        systemExit.status == 0
    }

    def "main: real run output file"() {
        given:
        String[] args = ["-o", repositoryRoot.toString() + "/cli/sourcehawk-flattened.yml",
                         "-c", repositoryRoot.toString() + "/sourcehawk.yml"]

        when:
        def exists = new File(repositoryRoot.toString() + "/cli/sourcehawk-flattened.yml").exists()

        then:
        !exists

        when:
        FlattenConfigCommand.main(args)

        then:
        SystemExit systemExit = thrown(SystemExit)
        systemExit.status == 0

        and:
        new File(repositoryRoot.toString() + "/cli/sourcehawk-flattened.yml").exists()
    }

    def "main: configuration file not found (failed)"() {
        given:
        OutputStream stdErr = new ByteArrayOutputStream()
        System.err = new PrintStream(stdErr)
        String[] args = ["-o", repositoryRoot.toString() + "/cli/sourcehawk-flattened.yml",
                         "-c", "sourcehawk.yml"]

        when:
        FlattenConfigCommand.main(args)

        then:
        SystemExit systemExit = thrown(SystemExit)
        systemExit.status == 1

        when:
        String stdErrContents = new String(stdErr.toByteArray()).trim()
                .replaceAll("\\u001b", "")
                .replaceAll("\\[\\d+m", "")

        then:
        stdErrContents == "Configuration file not found: sourcehawk.yml\nConfiguration file sourcehawk.yml not found or invalid"
    }

    def "main: configuration file blank not found (failed)"() {
        given:
        String[] args = ["-c", "",
                         "-o", repositoryRoot.toString() + "/cli/sourcehawk-flattened.yml"]

        when:
        FlattenConfigCommand.main(args)

        then:
        SystemExit systemExit = thrown(SystemExit)
        systemExit.status == 1
    }


    @Unroll
    def "main: parse error"() {
        given:
        String[] args = new String[]{arg}

        when:
        FlattenConfigCommand.main(args)

        then:
        SystemExit systemExit = thrown(SystemExit)
        systemExit.status == 2

        where:
        arg << ["-n", "--none"]
    }

    def "getConfigurationFileLocation - URL"() {
        given:
        URL configFileUrl = new URL("https://raw.githubusercontent.com/optum/sourcehawk/main/sourcehawk.yml")
        FlattenConfigCommand flattenConfigCommand = new FlattenConfigCommand(configFile: new CommandOptions.ConfigFile(url: configFileUrl))

        when:
        String configurationFileLocation = flattenConfigCommand.getConfigurationFileLocation()

        then:
        configurationFileLocation == configFileUrl.toString()
    }

    def "getConfigurationFileLocation - local"() {
        given:
        Path configFilePath = Paths.get("sourcehawk.yml")
        FlattenConfigCommand flattenConfigCommand = new FlattenConfigCommand(configFile: new CommandOptions.ConfigFile(path: configFilePath))

        when:
        String configurationFileLocation = flattenConfigCommand.getConfigurationFileLocation()

        then:
        configurationFileLocation == configFilePath.toString()
    }

    def "getConfigurationFileLocation - default"() {
        given:
        FlattenConfigCommand flattenConfigCommand = new FlattenConfigCommand()

        when:
        String configurationFileLocation = flattenConfigCommand.getConfigurationFileLocation()

        then:
        configurationFileLocation == SourcehawkConstants.DEFAULT_CONFIG_FILE_NAME
    }

}
