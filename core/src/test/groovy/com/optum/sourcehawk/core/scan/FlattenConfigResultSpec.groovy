package com.optum.sourcehawk.core.scan

import spock.lang.Specification

class FlattenConfigResultSpec extends Specification {

    def "happy path constructors"() {
        when:
        def flattenResult = FlattenConfigResult.builder()
                .formattedMessage("Success")
                .content("hello".bytes)
                .error(false)
                .build()

        then:
        flattenResult.content == "hello".bytes
        !flattenResult.error
        flattenResult.formattedMessage == "Success"

        and:
        flattenResult.toString() == FlattenConfigResult.builder()
                .formattedMessage("Success")
                .content("hello".bytes)
                .error(false)
                .build().toString()
    }

    def "success"() {
        when:
        def flattenResult = FlattenConfigResult.success("hello".bytes)

        then:
        flattenResult.content == "hello".bytes
        flattenResult.formattedMessage == "Flatten successful"
        !flattenResult.error
    }

    def "error"() {
        when:
        def flattenResult = FlattenConfigResult.error("Bad")

        then:
        !flattenResult.content
        flattenResult.formattedMessage == "Bad"
        flattenResult.error
    }

}
