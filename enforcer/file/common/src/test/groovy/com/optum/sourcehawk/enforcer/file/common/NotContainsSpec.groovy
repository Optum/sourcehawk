package com.optum.sourcehawk.enforcer.file.common


import com.optum.sourcehawk.enforcer.EnforcerResult
import org.spockframework.util.IoUtil
import spock.lang.Specification
import spock.lang.Unroll

class NotContainsSpec extends Specification {

    def "equals"() {
        expect:
        NotContains.substring('Hello')
    }

    def "enforce - null input stream"() {
        when:
        NotContains.substring("abc").enforceInternal(null)

        then:
        thrown(NullPointerException)
    }

    @Unroll
    def "enforce - #expectedSubstring (passed)"() {
        given:
        NotContains notContains = NotContains.substring(expectedSubstring)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/file.txt')

        when:
        EnforcerResult result = notContains.enforce(fileInputStream)

        then:
        result
        result.passed
        !result.messages

        where:
        expectedSubstring << [
                'imaginary',
                'I should not include'
        ]
    }

    @Unroll
    def "enforce - #expectedSubstring (failed)"() {
        given:
        NotContains notContains = NotContains.substring(expectedSubstring)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/file.txt')

        when:
        EnforcerResult result = notContains.enforce(fileInputStream)

        then:
        result
        !result.passed
        result.messages
        result.messages[0] == "File does contain the sub string [$expectedSubstring]"

        where:
        expectedSubstring << [
                'special',
                'Perhaps I should include'
        ]
    }

}
