package com.optum.sourcehawk.enforcer.file.common


import com.optum.sourcehawk.enforcer.EnforcerResult
import com.optum.sourcehawk.enforcer.ResolverResult
import org.spockframework.util.IoUtil
import spock.lang.Specification
import spock.lang.Unroll

class ContainsLineAtSpec extends Specification {

    def "equals"() {
        expect:
        ContainsLineAt.containsAt('I am a line', 1)
    }

    def "enforce - null input stream"() {
        given:
        ContainsLineAt containsLineAt = ContainsLineAt.containsAt("pattern", 1)

        when:
        containsLineAt.enforceInternal(null)

        then:
        thrown(NullPointerException)
    }

    @Unroll
    def "enforce - #expectedLine - #expectedLineNumber (passed)"() {
        given:
        ContainsLineAt containsLineAt = ContainsLineAt.containsAt(expectedLine, expectedLineNumber)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/file.txt')

        when:
        EnforcerResult result = containsLineAt.enforce(fileInputStream)

        then:
        result
        result.passed
        !result.messages

        where:
        expectedLine                                                     | expectedLineNumber
        '^(?:(?!include).)*$'                                            | 5
        '^ Here is a special character: $'                               | 5
        'Perhaps I should include a double " and a single \' as well...' | 7
        '.*I should include a double " and a single \' as well.*'        | 7
    }

    @Unroll
    def "enforce - NOT #expectedLine - #expectedLineNumber (passed)"() {
        given:
        ContainsLineAt containsLineAt = ContainsLineAt.containsAt(expectedLine, expectedLineNumber)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/file.txt')

        when:
        EnforcerResult result = containsLineAt.enforce(fileInputStream)

        then:
        result
        !result.passed
        result.messages

        where:
        expectedLine                                                        | expectedLineNumber
        '![\\^ Here is a special character: \\$]'                           | 5
        '^(?:(?!special character).)*$'                                     | 5
        '![Perhaps I should include a double " and a single \' as well...]' | 7
    }

    @Unroll
    def "enforce - #expectedLine (failed)"() {
        given:
        ContainsLineAt containsLineAt = ContainsLineAt.containsAt(expectedLine, expectedLineNumber)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/file.txt')

        when:
        EnforcerResult result = containsLineAt.enforce(fileInputStream)

        then:
        result
        !result.passed
        result.messages
        result.messages[0] == "File contains line [$expectedLine] at line number [$expectedLineNumber] failed"

        where:
        expectedLine                                                     | expectedLineNumber
        '^ Here is a special character: $'                               | 2
        'Perhaps I should include a double " and a single \' as well...' | 3
    }

    def "resolve - no updates applied"() {
        given:
        String expectedLine = 'and I have some new lines.'
        int expectedLineNumber = 3
        ContainsLineAt containsLineAt = ContainsLineAt.containsAt(expectedLine, expectedLineNumber)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/file.txt')
        Writer stringWriter = new StringWriter()

        when:
        ResolverResult resolverResult = containsLineAt.resolve(fileInputStream, stringWriter)

        then:
        resolverResult
        !resolverResult.updatesApplied
        !resolverResult.messages
    }

    def "resolve - updates applied (line does not match)"() {
        given:
        String expectedLine = 'I do not match the line given.'
        int expectedLineNumber = 3
        ContainsLineAt containsLineAt = ContainsLineAt.containsAt(expectedLine, expectedLineNumber)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/file.txt')
        Writer stringWriter = new StringWriter()

        when:
        ResolverResult resolverResult = containsLineAt.resolve(fileInputStream, stringWriter)

        then:
        resolverResult
        resolverResult.updatesApplied
        resolverResult.messages
        resolverResult.messages.size() == 1
        resolverResult.messages[0] == "File line number [$expectedLineNumber] has been updated to value [$expectedLine]"

        when:
        String[] updatedLines = stringWriter.toString().split("\n")

        then:
        updatedLines[expectedLineNumber - 1] == expectedLine
    }

}
