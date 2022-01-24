package com.optum.sourcehawk.enforcer.file.common

import com.optum.sourcehawk.enforcer.EnforcerResult
import org.spockframework.util.IoUtil
import spock.lang.Specification

class ContentNotEqualsSpec extends Specification {

    def "string"() {
        expect:
        ContentNotEquals.string('file')
    }

    def "enforce - null input stream"() {
        when:
        ContentNotEquals.string("abc").enforceInternal(null)

        then:
        thrown(NullPointerException)
    }

    def "enforce (passed)"() {
        given:
        ContentNotEquals contentNotEquals = ContentNotEquals.string(IoUtil.getResourceAsStream('/file.txt').text)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/file.txt')

        when:
        EnforcerResult result = contentNotEquals.enforce(fileInputStream)

        then:
        result
        !result.passed
        result.messages
        result.messages[0] == "File contents do equal that of the expected file contents"
    }

    def "enforce (failed)"() {
        given:
        ContentNotEquals contentNotEquals = ContentNotEquals.string(IoUtil.getResourceAsStream('/file.txt').text)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/checksum.txt')

        when:
        EnforcerResult result = contentNotEquals.enforce(fileInputStream)

        then:
        result
        result.passed
        !result.messages
    }

    def "enforce - URL (passed)"() {
        given:
        ContentNotEquals notEquals = ContentNotEquals.url(new URL('https://raw.githubusercontent.com/optum/sourcehawk/main/README.md'))
        InputStream fileInputStream = new URL('https://raw.githubusercontent.com/optum/sourcehawk/main/README.md').openStream()

        when:
        EnforcerResult result = notEquals.enforce(fileInputStream)

        then:
        result
        !result.passed
        result.messages
        result.messages[0] == "File contents do equal that of the expected file contents"
    }

    def "enforce - URL (failed)"() {
        given:
        ContentNotEquals contentNotEquals = ContentNotEquals.url(new URL('https://raw.githubusercontent.com/optum/sourcehawk/main/README.md'))
        InputStream fileInputStream = IoUtil.getResourceAsStream('/checksum.txt')

        when:
        EnforcerResult result = contentNotEquals.enforce(fileInputStream)

        then:
        result
        result.passed
        !result.messages
    }

}
