package com.optum.sourcehawk.enforcer.file.common


import com.optum.sourcehawk.enforcer.EnforcerResult
import org.spockframework.util.IoUtil
import spock.lang.Specification
import spock.lang.Unroll

class ContainsSpec extends Specification {

    def "equals"() {
        expect:
        Contains.substring('Hello')
    }

    def "enforce - null input stream"() {
        when:
        Contains.substring("abc").enforceInternal(null)

        then:
        thrown(NullPointerException)
    }

    @Unroll
    def "enforce - #expectedSubstring (passed)"() {
        given:
        Contains contains = Contains.substring(expectedSubstring)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/file.txt')

        when:
        EnforcerResult result = contains.enforce(fileInputStream)

        then:
        result
        result.passed
        !result.messages

        where:
        expectedSubstring << [
                'character',
                'I should include'
        ]
    }

    @Unroll
    def "enforce - NOT #expectedSubstring (passed)"() {
        given:
        Contains contains = Contains.substring(expectedSubstring)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/file.txt')

        when:
        EnforcerResult result = contains.enforce(fileInputStream)

        then:
        result
        !result.passed
        result.messages

        where:
        expectedSubstring << [
                '![character]',
                '![I should include]'
        ]
    }

    @Unroll
    def "enforce - #expectedSubstring (failed)"() {
        given:
        Contains contains = Contains.substring(expectedSubstring)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/file.txt')

        when:
        EnforcerResult result = contains.enforce(fileInputStream)

        then:
        result
        !result.passed
        result.messages
        result.messages[0] == "File contain the sub string [$expectedSubstring] failed"

        where:
        expectedSubstring << [
                'really weird string',
                'Perhaps you should'
        ]
    }

}
