package com.optum.sourcehawk.exec.config

import com.optum.sourcehawk.core.result.FlattenConfigResult
import com.optum.sourcehawk.exec.FileBaseSpecification
import org.spockframework.util.IoUtil

class FlattenConfigExecutorSpec extends FileBaseSpecification {

    def "handleException"() {
        when:
        FlattenConfigResult flattenConfigResult = FlattenConfigExecutor.handleException("test", new IOException("message"))

        then:
        flattenConfigResult.error
        flattenConfigResult.message == "Error flattening sourcehawk configuration at test with error: message"
    }

    def "flatten"() {
        when:
        FlattenConfigResult flattenConfigResult = FlattenConfigExecutor.flatten(repositoryRoot.toString() + "/sourcehawk.yml")

        then:
        !flattenConfigResult.error
        flattenConfigResult.message == "Flatten successful"
        new String(flattenConfigResult.content).trim() == IoUtil.getResourceAsStream("/sourcehawk-flattened-base.yml").text.trim()
    }

    def "flatten - config file not found"() {
        when:
        FlattenConfigResult flattenConfigResult = FlattenConfigExecutor.flatten("sourcehawk-does-not-exist.yml")

        then:
        flattenConfigResult.error
        flattenConfigResult.message
    }

}
