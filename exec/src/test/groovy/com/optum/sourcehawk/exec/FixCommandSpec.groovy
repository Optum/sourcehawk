package com.optum.sourcehawk.exec

import com.optum.sourcehawk.core.scan.FixResult
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.spockframework.util.IoUtil
import spock.lang.Shared
import spock.lang.Unroll

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class FixCommandSpec extends CliBaseSpecification {

    @Shared
    String repoRoot = new File(IoUtil.getResource("/marker").toURI())
            .getParentFile()
            .getAbsolutePath() + "/repo"

    @Shared
    String updateRepoRoot = new File(IoUtil.getResource("/marker").toURI())
            .getParentFile()
            .getAbsolutePath() + "/repo-updates"

    @Rule
    private TemporaryFolder temporaryFolder = TemporaryFolder.builder()
            .assureDeletion()
            .parentFolder(new File("/tmp"))
            .build()

    def setup() {
        temporaryFolder.create()
    }

    def cleanup() {
        temporaryFolder.delete()
    }

    @Unroll
    def "main: #helpArg"() {
        given:
        String[] args = new String[] { helpArg }

        when:
        FixCommand.main(args)

        then:
        SystemExit systemExit = thrown(SystemExit)
        systemExit.status == 0

        where:
        helpArg << ["-h", "--help" ]
    }

    def "main: dry run (no file updates)"() {
        given:
        File directory = temporaryFolder.newFolder("dry-run")
        Files.copy(Paths.get(updateRepoRoot).resolve("sourcehawk.yml"), directory.toPath().resolve("sourcehawk.yml"))
        Path lombokConfigPath = Files.copy(Paths.get(updateRepoRoot).resolve("lombok.config"), directory.toPath().resolve("lombok.config"))
        String[] args = [ "--dry-run", directory.getAbsolutePath() ]

        when:
        FixCommand.main(args)

        then:
        SystemExit systemExit = thrown(SystemExit)
        systemExit.status == 0

        and:
        new FileInputStream(Paths.get(updateRepoRoot).resolve("lombok.config").toFile()).text == new FileInputStream(lombokConfigPath.toFile()).text
    }

    def "main: files updated"() {
        given:
        File directory = temporaryFolder.newFolder("updates")
        Files.copy(Paths.get(updateRepoRoot).resolve("sourcehawk.yml"), directory.toPath().resolve("sourcehawk.yml"))
        Path lombokConfigPath = Files.copy(Paths.get(updateRepoRoot).resolve("lombok.config"), directory.toPath().resolve("lombok.config"))
        String[] args = [ directory.getAbsolutePath() ]

        when:
        FixCommand.main(args)

        then:
        SystemExit systemExit = thrown(SystemExit)
        systemExit.status == 0

        when:
        Properties properties = new Properties()
        properties.load(new InputStreamReader(new FileInputStream(lombokConfigPath.toFile())))

        then:
        properties.getProperty("config.stopBubbling") == "true"
        properties.getProperty("lombok.addLombokGeneratedAnnotation") == "true"
        properties.getProperty("lombok.anyConstructor.addConstructorProperties") == "true"
    }

    def "main: no files updated"() {
        given:
        File directory = temporaryFolder.newFolder("no-updates")
        Files.copy(Paths.get(repoRoot).resolve("sourcehawk.yml"), directory.toPath().resolve("sourcehawk.yml"))
        Path lombokConfigPath = Files.copy(Paths.get(repoRoot).resolve("lombok.config"), directory.toPath().resolve("lombok.config"))
        String[] args = [ directory.getAbsolutePath() ]

        when:
        FixCommand.main(args)

        then:
        SystemExit systemExit = thrown(SystemExit)
        systemExit.status == 2

        and:
        new FileInputStream(Paths.get(repoRoot).resolve("lombok.config").toFile()).text == new FileInputStream(lombokConfigPath.toFile()).text
    }

    def "main: configuration file not found (failed)"() {
        given:
        String[] args = ["-c", "/tmp/does-not-exist/sourcehawk.yml"]

        when:
        FixCommand.main(args)

        then:
        SystemExit systemExit = thrown(SystemExit)
        systemExit.status == 1
    }

    @Unroll
    def "main: parse error"() {
        given:
        String[] args = new String[] { arg }

        when:
        FixCommand.main(args)

        then:
        SystemExit systemExit = thrown(SystemExit)
        systemExit.status == 2

        where:
        arg << [ "-n", "--none" ]
    }

    def "execute - exception"() {
        given:
        ExecOptions execOptions = null

        when:
        FixResult fixResult = FixCommand.execute(execOptions, false)

        then:
        fixResult
        fixResult.error

        and:
        noExceptionThrown()
    }

}
