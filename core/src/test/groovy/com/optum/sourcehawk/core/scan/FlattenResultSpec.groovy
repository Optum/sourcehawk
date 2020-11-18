package com.optum.sourcehawk.core.scan

import spock.lang.Specification

class FlattenResultSpec extends Specification {

    def "happy path constructors"() {
        when:
        def flattenResult = FlattenResult.builder()
                .messages(["ONE": [FlattenResult.MessageDescriptor.builder()
                                           .message("test")
                                           .repositoryPath("path")
                                           .build()]])
                .content("hello".bytes)
                .error(false)
                .errorCount(0)
                .build()

        then:
        flattenResult.content == "hello".bytes
        !flattenResult.error
        flattenResult.errorCount == 0
        flattenResult.messages.size() == 1
        flattenResult.messages.get("ONE").toString() == "[path :: test]"
    }
}
