package com.optum.sourcehawk.exec

import com.optum.sourcehawk.core.scan.FlattenResult
import spock.lang.Specification

class FlattenResultFactorySpec extends Specification {

    def "error"() {
        when:
        FlattenResult flattenResult = FlattenResultFactory.error("/target", "test")

        then:
        flattenResult
        flattenResult.error
        flattenResult.errorCount == 1
        !flattenResult.content
        flattenResult.messages.size() == 1
        flattenResult.formattedMessages.size() == 1
    }

    def "success"() {
        when:
        FlattenResult flattenResult = FlattenResultFactory.success("hello".bytes)

        then:
        flattenResult
        !flattenResult.error
        flattenResult.errorCount == 0
        flattenResult.content
        flattenResult.messages.size() == 0
        flattenResult.formattedMessages.size() == 1
    }
}
