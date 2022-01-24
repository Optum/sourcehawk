package com.optum.sourcehawk.enforcer.file.common


import com.optum.sourcehawk.enforcer.EnforcerResult
import org.spockframework.util.IoUtil
import spock.lang.Specification
import spock.lang.Unroll

class NotContainsLineSpec extends Specification {

    def "equals"() {
        expect:
        NotContainsLine.contains('I am a line')
    }

    def "enforce - null input stream"() {
        when:
        NotContainsLine.contains("line").enforceInternal(null)

        then:
        thrown(NullPointerException)
    }

    @Unroll
    def "enforce - #expectedLine (passed)"() {
        given:
        NotContainsLine notContainsLine = NotContainsLine.contains(expectedLine)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/file.txt')

        when:
        EnforcerResult result = notContainsLine.enforce(fileInputStream)

        then:
        result
        !result.passed
        result.messages[0] == "File does contain the line [$expectedLine]"

        where:
        expectedLine << [
                '^ Here is a special character: $',
                'Perhaps I should include a double " and a single \' as well...'
        ]
    }

    @Unroll
    def "enforce - #expectedLine (failed)"() {
        given:
        NotContainsLine notContainsLine = NotContainsLine.contains(expectedLine)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/file.txt')

        when:
        EnforcerResult result = notContainsLine.enforce(fileInputStream)

        then:
        result
        result.passed
        !result.messages

        where:
        expectedLine << [
                'Here is a special character: $',
                'Perhaps I should include a double " and a single \' as well'
        ]
    }

}
