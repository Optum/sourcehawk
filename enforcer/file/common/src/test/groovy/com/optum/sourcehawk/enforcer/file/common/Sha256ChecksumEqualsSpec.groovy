package com.optum.sourcehawk.enforcer.file.common


import com.optum.sourcehawk.enforcer.EnforcerResult
import org.spockframework.util.IoUtil
import spock.lang.Specification

class Sha256ChecksumEqualsSpec extends Specification {

    def "equals"() {
        expect:
        Sha256ChecksumEquals.equals("checksum")
    }

    def "enforce - null input stream"() {
        when:
        Sha256ChecksumEquals.equals("abc").enforceInternal(null)

        then:
        thrown(NullPointerException)
    }

    def "enforce (passed))"() {
        given:
        String expectedChecksum = "a6179a1feff6949517fab1d18804a35d25d807c597fcba21a6b4c3e919af6e6f"
        Sha256ChecksumEquals sha256ChecksumEquals = Sha256ChecksumEquals.equals(expectedChecksum)
        InputStream fileInputStream = IoUtil.getResourceAsStream("/checksum.txt")

        when:
        EnforcerResult enforcerResult = sha256ChecksumEquals.enforce(fileInputStream)

        then:
        enforcerResult
        enforcerResult.passed
        !enforcerResult.messages
    }

    def "enforce regex (passed))"() {
        given:
        String expectedChecksum = "a6179a.*"
        Sha256ChecksumEquals sha256ChecksumEquals = Sha256ChecksumEquals.equals(expectedChecksum)
        InputStream fileInputStream = IoUtil.getResourceAsStream("/checksum.txt")

        when:
        EnforcerResult enforcerResult = sha256ChecksumEquals.enforce(fileInputStream)

        then:
        enforcerResult
        enforcerResult.passed
        !enforcerResult.messages
    }

    def "enforce regex negate (passed))"() {
        given:
        String expectedChecksum = "^(?:(?!abc123).)*\$"
        Sha256ChecksumEquals sha256ChecksumEquals = Sha256ChecksumEquals.equals(expectedChecksum)
        InputStream fileInputStream = IoUtil.getResourceAsStream("/checksum.txt")

        when:
        EnforcerResult enforcerResult = sha256ChecksumEquals.enforce(fileInputStream)

        then:
        enforcerResult
        enforcerResult.passed
        !enforcerResult.messages
    }

    def "enforce (failed))"() {
        given:
        String expectedChecksum = "123"
        Sha256ChecksumEquals sha256ChecksumEquals = Sha256ChecksumEquals.equals(expectedChecksum)
        InputStream fileInputStream = IoUtil.getResourceAsStream("/checksum.txt")

        when:
        EnforcerResult enforcerResult = sha256ChecksumEquals.enforce(fileInputStream)

        then:
        enforcerResult
        !enforcerResult.passed
        enforcerResult.messages
        enforcerResult.messages[0] == 'The SHA-256 checksum of the file does not match'
    }

}
