package com.optum.sourcehawk.enforcer.file.common

import com.optum.sourcehawk.enforcer.EnforcerResult
import org.spockframework.util.IoUtil
import spock.lang.Ignore
import spock.lang.Issue
import spock.lang.Specification

class ContentEqualsSpec extends Specification {

    def "string"() {
        expect:
        ContentEquals.string('file')
    }

    def "enforce - null input stream"() {
        when:
        ContentEquals.string("abc").enforceInternal(null)

        then:
        thrown(NullPointerException)
    }

    def "enforce (passed)"() {
        given:
        ContentEquals contentEquals = ContentEquals.string(IoUtil.getResourceAsStream('/file.txt').text)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/file.txt')

        when:
        EnforcerResult result = contentEquals.enforce(fileInputStream)

        then:
        result
        result.passed
        !result.messages
    }

    def "enforce (failed)"() {
        given:
        ContentEquals contentEquals = ContentEquals.string(IoUtil.getResourceAsStream('/file.txt').text)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/checksum.txt')

        when:
        EnforcerResult result = contentEquals.enforce(fileInputStream)

        then:
        result
        !result.passed
        result.messages
        result.messages[0] == "File contents do not equal that of the expected file contents"
    }

    def "enforce - URL (passed)"() {
        given:
        ContentEquals contentEquals = ContentEquals.url(new URL('https://raw.githubusercontent.com/optum/sourcehawk/main/README.md'))
        InputStream fileInputStream = new URL('https://raw.githubusercontent.com/optum/sourcehawk/main/README.md').openStream()

        when:
        EnforcerResult result = contentEquals.enforce(fileInputStream)

        then:
        result
        result.passed
        !result.messages
    }

    def "enforce - URL (failed)"() {
        given:
        ContentEquals contentEquals = ContentEquals.url(new URL('https://raw.githubusercontent.com/optum/sourcehawk/main/README.md'))
        InputStream fileInputStream = IoUtil.getResourceAsStream('/checksum.txt')

        when:
        EnforcerResult result = contentEquals.enforce(fileInputStream)

        then:
        result
        !result.passed
        result.messages
        result.messages[0] == "File contents do not equal that of the expected file contents"
    }

}
