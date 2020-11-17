package com.optum.sourcehawk.exec

import com.optum.sourcehawk.core.scan.FixResult

class FixExecutorSpec extends FileBaseSpecification {

    def "fix - defaults"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
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
                .repositoryRoot(repositoryRoot)
                .configurationFileLocation(repositoryRoot.resolve("sourcehawk.yml").toAbsolutePath().toString())
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
                .repositoryRoot(repositoryRoot)
                .configurationFileLocation("https://raw.githubusercontent.com/optum/sourcehawk-parent/main/.sourcehawk/config.yml")
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
                .repositoryRoot(repositoryRoot)
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
                .repositoryRoot(repositoryRoot)
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
                .repositoryRoot(repositoryRoot)
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

    def "fix - dry run"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(testResourcesRoot.resolve("repo-updates"))
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
    }

     // TODO: temporary testing directory

}
