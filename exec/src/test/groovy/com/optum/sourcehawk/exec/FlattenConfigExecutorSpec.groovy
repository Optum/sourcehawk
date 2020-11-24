package com.optum.sourcehawk.exec

import spock.lang.Unroll

class FlattenConfigExecutorSpec extends FileBaseSpecification {

    def "handleException"() {
        when:
        def result = FlattenConfigExecutor.handleException("test", new IOException("message"))

        then:
        result.error
        result.formattedMessage == "Error flattening sourcehawk configuration at test with error: message"
    }

    def "flatten"() {
        when:
        def result = FlattenConfigExecutor.flatten(repositoryRoot.toString() + "/sourcehawk.yml")

        then:
        !result.error
        result.formattedMessage == "Flatten successful"
        new String(result.content).trim() == new File(testResourcesRoot.toString() + "/flattened/sourcehawk-flattened-base.yml").text.trim()
    }

    @Unroll
    def "flatten no config file #configurationFileLocation"() {
        when:
        def result = FlattenConfigExecutor.flatten(configurationFileLocation)

        then:
        result.error
        result.formattedMessage

        where:
        configurationFileLocation << ["", " ", null]
    }
}
