package com.optum.sourcehawk.core.result

import spock.lang.Specification
import spock.lang.Unroll

class FixResultSpec extends Specification {

    def "builder"() {
        given:
        FixResult.FixResultBuilder fixResultBuilder = FixResult.builder()
                .fixesApplied(true)
                .fixCount(2)
                .noResolver(true)
                .messages(["lombok.config": [FixResult.MessageDescriptor.builder()
                                                     .repositoryPath("path")
                                                     .message("test")
                                                     .build()]])
                .formattedMessages(["lombok.config :: Fixed up"])

        when:
        FixResult fixResult = fixResultBuilder.build()

        then:
        fixResult
        fixResult.toString()
        fixResult.hashCode() == FixResult.builder()
                .fixesApplied(true)
                .fixCount(2)
                .noResolver(true)
                .messages(["lombok.config": [FixResult.MessageDescriptor.builder()
                                                     .repositoryPath("path")
                                                     .message("test")
                                                     .build()]])
                .formattedMessages(["lombok.config :: Fixed up"])
                .build()
                .hashCode()
    }

    def "builder - defaults"() {
        given:
        FixResult.FixResultBuilder fixResultBuilder = FixResult.builder()

        when:
        FixResult fixResult = fixResultBuilder.build()

        then:
        fixResult
        !fixResult.fixesApplied
        fixResult.fixCount == 0
        !fixResult.noResolver
        !fixResult.error
        !fixResult.messages
        !fixResult.formattedMessages
    }

    @Unroll
    def "reduce - resolvers #resolverOne || #resolverTwo == #expected"() {
        given:
        FixResult one = FixResult.builder()
                .fixesApplied(true)
                .fixCount(2)
                .noResolver(resolverOne)
                .messages(["lombok.config": ["Fixed up"]])
                .formattedMessages(["lombok.config :: Fixed up"])
                .build()
        FixResult two = FixResult.builder().noResolver(resolverTwo).build()

        when:
        FixResult reduced = FixResult.reduce(one, two)

        then:
        reduced
        reduced.fixesApplied
        reduced.fixCount == 2
        reduced.noResolver == expected
        !reduced.error
        reduced.messages.size() == 1
        reduced.formattedMessages.size() == 1

        where:
        resolverOne | resolverTwo || expected
        true        | false       || true
        true        | true        || true
        false       | true        || true
        false       | false       || false
    }

    @Unroll
    def "reduce - error #errorOne && #errorTwo == #expected"() {
        given:
        FixResult one = FixResult.builder()
                .fixesApplied(true)
                .fixCount(2)
                .error(errorOne)
                .messages(["lombok.config": ["Fixed up"]])
                .formattedMessages(["lombok.config :: Fixed up"])
                .build()
        FixResult two = FixResult.builder().error(errorTwo).build()

        when:
        FixResult reduced = FixResult.reduce(one, two)

        then:
        reduced
        reduced.fixesApplied
        reduced.fixCount == 2
        reduced.error == expected
        reduced.messages.size() == 1
        reduced.formattedMessages.size() == 1

        where:
        errorOne | errorTwo || expected
        true     | false    || false
        true     | true     || true
        false    | true     || false
        false    | false    || false
    }

    @Unroll
    def "reduce - fixesApplied #fixesAppliedOne || #fixesAppliedTwo == #expected"() {
        given:
        FixResult one = FixResult.builder()
                .fixCount(2)
                .fixesApplied(fixesAppliedOne)
                .messages(["lombok.config": ["Fixed up"]])
                .formattedMessages(["lombok.config :: Fixed up"])
                .build()
        FixResult two = FixResult.builder().fixesApplied(fixesAppliedTwo).build()

        when:
        FixResult reduced = FixResult.reduce(one, two)

        then:
        reduced
        reduced.fixesApplied == expected
        reduced.fixCount == 2
        !reduced.error
        reduced.messages.size() == 1
        reduced.formattedMessages.size() == 1

        where:
        fixesAppliedOne | fixesAppliedTwo || expected
        true            | false           || true
        true            | true            || true
        false           | true            || true
        false           | false           || false
    }

    def "reduce - one fix, one no fix"() {
        given:
        FixResult one = FixResult.builder()
                .fixesApplied(true)
                .fixCount(2)
                .messages(["lombok.config": ["Fixed up"]])
                .formattedMessages(["lombok.config :: Fixed up"])
                .build()
        FixResult two = FixResult.builder().build()

        when:
        FixResult reduced = FixResult.reduce(one, two)

        then:
        reduced
        reduced.fixesApplied
        reduced.fixCount == 2
        !reduced.noResolver
        !reduced.error
        reduced.messages.size() == 1
        reduced.formattedMessages.size() == 1
    }

    def "reduce - fixes - same file"() {
        given:
        FixResult one = FixResult.builder()
                .fixesApplied(true)
                .fixCount(2)
                .messages(["lombok.config": ["Fixed up 1", "Fixed up 2"]])
                .formattedMessages(["lombok.config :: Fixed up 1", "lombok.config :: Fixed up 2"])
                .build()
        FixResult two = FixResult.builder()
                .fixesApplied(true)
                .fixCount(1)
                .messages(["lombok.config": ["Fixed up 3"]])
                .formattedMessages(["lombok.config :: Fixed up 3"])
                .build()

        when:
        FixResult reduced = FixResult.reduce(one, two)

        then:
        reduced
        reduced.fixesApplied
        reduced.fixCount == 3
        !reduced.noResolver
        !reduced.error
        reduced.messages.size() == 1
        reduced.messages["lombok.config"].size() == 3
        reduced.formattedMessages.size() == 3
    }

    def "reduce - fixes - same file - no duplicates"() {
        given:
        FixResult one = FixResult.builder()
                .fixesApplied(true)
                .fixCount(1)
                .messages(["lombok.config": ["Fixed up"]])
                .formattedMessages(["lombok.config :: Fixed up"])
                .build()
        FixResult two = FixResult.builder()
                .fixesApplied(true)
                .fixCount(1)
                .messages(["lombok.config": ["Fixed up"]])
                .formattedMessages(["lombok.config :: Fixed up"])
                .build()

        when:
        FixResult reduced = FixResult.reduce(one, two)

        then:
        reduced
        reduced.fixesApplied
        reduced.fixCount == 1
        !reduced.noResolver
        !reduced.error
        reduced.messages.size() == 1
        reduced.formattedMessages.size() == 1
    }

    def "reduce - fixes - one error"() {
        given:
        FixResult one = FixResult.builder()
                .fixesApplied(true)
                .fixCount(1)
                .messages(["lombok.config": ["Fixed up"]])
                .formattedMessages(["lombok.config :: Fixed up"])
                .build()
        FixResult two = FixResult.builder()
                .error(true)
                .errorCount(1)
                .messages(["pom.xml": ["Unable to parse pom.xml file"]])
                .formattedMessages(["pom.xml :: Unable to parse pom.xml file"])
                .build()

        when:
        FixResult reduced = FixResult.reduce(one, two)

        then:
        reduced
        reduced.fixesApplied
        reduced.fixCount == 1
        !reduced.noResolver
        !reduced.error
        reduced.errorCount == 1
        reduced.messages.size() == 2
        reduced.formattedMessages.size() == 2
    }

    def "reduce - fixes - both errors"() {
        given:
        FixResult one = FixResult.builder()
                .error(true)
                .errorCount(1)
                .messages(["lombok.config": ["Invalid format"]])
                .formattedMessages(["lombok.config :: Invalid format"])
                .build()
        FixResult two = FixResult.builder()
                .error(true)
                .errorCount(1)
                .messages(["pom.xml": ["Unable to parse pom.xml file"]])
                .formattedMessages(["pom.xml :: Unable to parse pom.xml file"])
                .build()

        when:
        FixResult reduced = FixResult.reduce(one, two)

        then:
        reduced
        !reduced.fixesApplied
        reduced.fixCount == 0
        !reduced.noResolver
        reduced.error
        reduced.errorCount == 2
        reduced.messages.size() == 2
        reduced.formattedMessages.size() == 2
    }

    @Unroll
    def "test nulls #one #two #expected"() {
        when:
        FixResult reduced = FixResult.reduce(one, two)

        then:
        reduced.error == expected

        where:
        one                                      | two                                      || expected
        FixResult.builder().error(false).build() | null                                     || false
        null                                     | FixResult.builder().error(false).build() || false
        null                                     | null                                     || true
    }

}
