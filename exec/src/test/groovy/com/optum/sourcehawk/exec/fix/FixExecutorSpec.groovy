package com.optum.sourcehawk.exec.fix

import com.optum.sourcehawk.core.repository.LocalRepositoryFileReader
import com.optum.sourcehawk.core.result.FixResult
import com.optum.sourcehawk.exec.ConfigurationException
import com.optum.sourcehawk.exec.ExecOptions
import com.optum.sourcehawk.exec.FileBaseSpecification
import com.optum.sourcehawk.exec.fix.FixExecutor
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.spockframework.util.IoUtil
import spock.lang.Shared

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class FixExecutorSpec extends FileBaseSpecification {

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

    def "fix - defaults"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(Paths.get(repoRoot))
                .build()

        when:
        FixResult fixResult = FixExecutor.fix(execOptions, false)

        then:
        fixResult
        !fixResult.fixesApplied
    }

    def "fix - absolute configuration file"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(Paths.get(repoRoot))
                .configurationFileLocation(Paths.get(repoRoot).resolve("sourcehawk.yml").toAbsolutePath().toString())
                .build()

        when:
        FixResult fixResult = FixExecutor.fix(execOptions, false)

        then:
        fixResult
        !fixResult.fixesApplied
    }

    def "fix - local override"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .configurationFileLocation(repositoryRoot.resolve(".test/override.yml").toString())
                .build()

        when:
        FixResult fixResult = FixExecutor.fix(execOptions, false)

        then:
        fixResult
        !fixResult.fixesApplied
    }

    def "fix - bad url"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .configurationFileLocation(repositoryRoot.resolve(".test/bad-url.yml").toString())
                .build()

        when:
        FixExecutor.fix(execOptions, false)

        then:
        thrown(ConfigurationException)
    }


    def "fix - local relative"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .configurationFileLocation(repositoryRoot.resolve(".test/local.yml").toString())
                .build()

        when:
        FixResult fixResult = FixExecutor.fix(execOptions, false)

        then:
        fixResult
        !fixResult.fixesApplied
    }

    def "fix - URL configuration file"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(Paths.get(repoRoot))
                .configurationFileLocation("https://raw.githubusercontent.com/optum/sourcehawk-parent/main/.sourcehawk/lombok.yml")
                .build()

        when:
        FixResult fixResult = FixExecutor.fix(execOptions, false)

        then:
        fixResult
        !fixResult.fixesApplied
    }

    def "fix - relative configuration file - configuration file not found"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(Paths.get(repoRoot))
                .configurationFileLocation("Sourcehawk")
                .build()

        when:
        FixResult fixResult = FixExecutor.fix(execOptions, false)

        then:
        fixResult
        !fixResult.fixesApplied
        fixResult.error
    }

    def "fix - file not found (no enforcers)"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(Paths.get(repoRoot))
                .configurationFileLocation(testResourcesRoot.resolve("sourcehawk-file-not-found.yml").toString())
                .build()

        when:
        FixResult fixResult = FixExecutor.fix(execOptions, false)

        then:
        fixResult
        !fixResult.fixesApplied

        and:
        noExceptionThrown()
    }

    def "fix - file not found (with enforcers)"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(Paths.get(repoRoot))
                .configurationFileLocation(testResourcesRoot.resolve("sourcehawk-file-not-found-enforcers.yml").toString())
                .build()

        when:
        FixResult fixResult = FixExecutor.fix(execOptions, false)

        then:
        fixResult
        !fixResult.fixesApplied

        and:
        noExceptionThrown()
    }

    def "fix - no enforcers"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .configurationFileLocation(testResourcesRoot.resolve("sourcehawk-no-enforcers.yml").toString())
                .build()

        when:
        FixResult fixResult = FixExecutor.fix(execOptions, false)

        then:
        fixResult
        !fixResult.fixesApplied

        and:
        noExceptionThrown()
    }

    def "fix - no resolvers"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .build()

        when:
        FixResult fixResult = FixExecutor.fix(execOptions, false)

        then:
        fixResult
        !fixResult.fixesApplied
        fixResult.fixCount == 0
        fixResult.noResolver
    }

    def "fix - fixes applied"() {
        given:
        File directory = temporaryFolder.newFolder("fixes-applied")
        Files.copy(Paths.get(updateRepoRoot).resolve("sourcehawk.yml"), directory.toPath().resolve("sourcehawk.yml"))
        Path lombokConfigPath = Files.copy(Paths.get(updateRepoRoot).resolve("lombok.config"), directory.toPath().resolve("lombok.config"))
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(directory.toPath().toAbsolutePath())
                .repositoryFileReader(LocalRepositoryFileReader.create(directory.toPath().toAbsolutePath()))
                .build()

        when:
        FixResult fixResult = FixExecutor.fix(execOptions, false)

        then:
        fixResult
        fixResult.fixesApplied
        fixResult.fixCount == 3
        !fixResult.noResolver
        fixResult.messages.size() == 1
        fixResult.messages['lombok.config']
        fixResult.messages['lombok.config'].size() == 3
        fixResult.messages['lombok.config'][0].repositoryPath == 'lombok.config'
        fixResult.messages['lombok.config'][0].message == 'Property [config.stopBubbling] with value [false] has been updated to value [true]'
        fixResult.messages['lombok.config'][1].repositoryPath == 'lombok.config'
        fixResult.messages['lombok.config'][1].message == 'Property [lombok.addLombokGeneratedAnnotation] with value [false] has been updated to value [true]'
        fixResult.messages['lombok.config'][2].repositoryPath == 'lombok.config'
        fixResult.messages['lombok.config'][2].message == 'Property [lombok.anyConstructor.addConstructorProperties] with value [false] has been updated to value [true]'
        fixResult.formattedMessages
        fixResult.formattedMessages.size() == 3
        fixResult.formattedMessages[0] == 'lombok.config :: Property [lombok.anyConstructor.addConstructorProperties] with value [false] has been updated to value [true]'
        fixResult.formattedMessages[1] == 'lombok.config :: Property [lombok.addLombokGeneratedAnnotation] with value [false] has been updated to value [true]'
        fixResult.formattedMessages[2] == 'lombok.config :: Property [config.stopBubbling] with value [false] has been updated to value [true]'

        when:
        Properties properties = new Properties()
        properties.load(new InputStreamReader(new FileInputStream(lombokConfigPath.toFile())))

        then:
        properties.getProperty("config.stopBubbling") == "true"
        properties.getProperty("lombok.addLombokGeneratedAnnotation") == "true"
        properties.getProperty("lombok.anyConstructor.addConstructorProperties") == "true"
    }

    def "fix - dry run"() {
        given:
        File directory = temporaryFolder.newFolder("dry-run")
        Files.copy(Paths.get(updateRepoRoot).resolve("sourcehawk.yml"), directory.toPath().resolve("sourcehawk.yml"))
        Path lombokConfigPath = Files.copy(Paths.get(updateRepoRoot).resolve("lombok.config"), directory.toPath().resolve("lombok.config"))
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(directory.toPath().toAbsolutePath())
                .repositoryFileReader(LocalRepositoryFileReader.create(directory.toPath().toAbsolutePath()))
                .build()

        when:
        FixResult fixResult = FixExecutor.fix(execOptions, true)

        then:
        fixResult
        !fixResult.fixesApplied
        fixResult.fixCount == 3
        !fixResult.noResolver
        fixResult.messages.size() == 1
        fixResult.messages['lombok.config']
        fixResult.messages['lombok.config'].size() == 3
        fixResult.messages['lombok.config'][0].repositoryPath == 'lombok.config'
        fixResult.messages['lombok.config'][0].message == 'Property [config.stopBubbling] with value [false] has been updated to value [true]'
        fixResult.messages['lombok.config'][1].repositoryPath == 'lombok.config'
        fixResult.messages['lombok.config'][1].message == 'Property [lombok.addLombokGeneratedAnnotation] with value [false] has been updated to value [true]'
        fixResult.messages['lombok.config'][2].repositoryPath == 'lombok.config'
        fixResult.messages['lombok.config'][2].message == 'Property [lombok.anyConstructor.addConstructorProperties] with value [false] has been updated to value [true]'
        fixResult.formattedMessages
        fixResult.formattedMessages.size() == 3
        fixResult.formattedMessages[0] == 'lombok.config :: Property [lombok.anyConstructor.addConstructorProperties] with value [false] has been updated to value [true]'
        fixResult.formattedMessages[1] == 'lombok.config :: Property [lombok.addLombokGeneratedAnnotation] with value [false] has been updated to value [true]'
        fixResult.formattedMessages[2] == 'lombok.config :: Property [config.stopBubbling] with value [false] has been updated to value [true]'

        and:
        new FileInputStream(Paths.get(updateRepoRoot).resolve("lombok.config").toFile()).text == new FileInputStream(lombokConfigPath.toFile()).text
    }

    def "fix - dry run (tag filter)"() {
        given:
        File directory = temporaryFolder.newFolder("dry-run-tag-filter")
        Files.copy(Paths.get(updateRepoRoot).resolve("sourcehawk.yml"), directory.toPath().resolve("sourcehawk.yml"))
        Path lombokConfigPath = Files.copy(Paths.get(updateRepoRoot).resolve("lombok.config"), directory.toPath().resolve("lombok.config"))
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(directory.toPath().toAbsolutePath())
                .tags(["unknown"])
                .repositoryFileReader(LocalRepositoryFileReader.create(directory.toPath().toAbsolutePath()))
                .build()

        when:
        FixResult fixResult = FixExecutor.fix(execOptions, true)

        then:
        fixResult
        !fixResult.fixesApplied
        fixResult.fixCount == 0
        !fixResult.noResolver
        !fixResult.messages
        !fixResult.formattedMessages
    }

    def "executeFix"(){

        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(testResourcesRoot.resolve("repo-updates"))
                .build()

        when:
        def result = FixExecutor.executeFix(execOptions, null, false)

        then:
        result.error
    }

}
