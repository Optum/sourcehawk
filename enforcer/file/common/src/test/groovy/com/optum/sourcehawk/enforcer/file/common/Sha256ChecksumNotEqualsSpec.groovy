package com.optum.sourcehawk.enforcer.file.common


import com.optum.sourcehawk.enforcer.EnforcerResult
import org.spockframework.util.IoUtil
import spock.lang.Specification

class Sha256ChecksumNotEqualsSpec extends Specification {

    def "equals"() {
        expect:
        Sha256ChecksumNotEquals.equals("checksum")
    }

    def "enforce - null input stream"() {
        when:
        Sha256ChecksumNotEquals.equals("abc").enforceInternal(null)

        then:
        thrown(NullPointerException)
    }

    def "enforce (passed))"() {
        given:
        String expectedChecksum = "a6179a1feff6949517fab1d18804a35d25d807c597fcba21a6b4c3e919af6e6f"
        Sha256ChecksumNotEquals sha256ChecksumNotEquals = Sha256ChecksumNotEquals.equals(expectedChecksum)
        InputStream fileInputStream = IoUtil.getResourceAsStream("/checksum.txt")

        when:
        EnforcerResult enforcerResult = sha256ChecksumNotEquals.enforce(fileInputStream)

        then:
        enforcerResult
        !enforcerResult.passed
        enforcerResult.messages
        enforcerResult.messages[0] == 'The SHA-256 checksum of the file does match'
    }

    def "enforce (failed))"() {
        given:
        String expectedChecksum = "123"
        Sha256ChecksumNotEquals sha256ChecksumNotEquals = Sha256ChecksumNotEquals.equals(expectedChecksum)
        InputStream fileInputStream = IoUtil.getResourceAsStream("/checksum.txt")

        when:
        EnforcerResult enforcerResult = sha256ChecksumNotEquals.enforce(fileInputStream)

        then:
        enforcerResult
        enforcerResult.passed
        !enforcerResult.messages
    }

}
