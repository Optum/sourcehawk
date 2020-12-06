package com.optum.sourcehawk.exec.flattenconfig

import com.optum.sourcehawk.core.scan.FlattenConfigResult
import com.optum.sourcehawk.exec.FileBaseSpecification
import com.optum.sourcehawk.exec.flattenconfig.FlattenConfigExecutor
import org.spockframework.util.IoUtil

class FlattenConfigExecutorSpec extends FileBaseSpecification {

    def "handleException"() {
        when:
        FlattenConfigResult flattenConfigResult = FlattenConfigExecutor.handleException("test", new IOException("message"))

        then:
        flattenConfigResult.error
        flattenConfigResult.formattedMessage == "Error flattening sourcehawk configuration at test with error: message"
    }

    def "flatten"() {
        when:
        FlattenConfigResult flattenConfigResult = FlattenConfigExecutor.flatten(repositoryRoot.toString() + "/sourcehawk.yml")

        then:
        !flattenConfigResult.error
        flattenConfigResult.formattedMessage == "Flatten successful"
        new String(flattenConfigResult.content).trim() == IoUtil.getResourceAsStream("/sourcehawk-flattened-base.yml").text.trim()
    }

    def "flatten - null/empty config file - [#configFileLocation]"() {
        when:
        FlattenConfigResult flattenConfigResult = FlattenConfigExecutor.flatten(configFileLocation)

        then:
        flattenConfigResult.error
        flattenConfigResult.formattedMessage

        where:
        configFileLocation << ["", " ", null]
    }

    def "flatten - config file not found"() {
        when:
        FlattenConfigResult flattenConfigResult = FlattenConfigExecutor.flatten("sourcehawk-does-not-exist.yml")

        then:
        flattenConfigResult.error
        flattenConfigResult.formattedMessage
    }

}
