package com.optum.sourcehawk.enforcer.file.docker


import com.optum.sourcehawk.enforcer.EnforcerResult
import org.spockframework.util.IoUtil
import spock.lang.Specification
import spock.lang.Unroll

class DockerfileFromImageEqualsSpec extends Specification {

    def "equals"() {
        expect:
        DockerfileFromImageEquals.equals("hub.docker.com")
    }

    def "enforce - null input stream"() {
        when:
        DockerfileFromImageEquals.equals("library/alpine").enforceInternal(null)

        then:
        thrown(NullPointerException)
    }

    def "enforce (passed)"() {
        given:
        DockerfileFromImageEquals DockerfileFromImageEquals = DockerfileFromImageEquals.equals('image')
        InputStream fileInputStream = IoUtil.getResourceAsStream('/Dockerfile-default')

        when:
        EnforcerResult result = DockerfileFromImageEquals.enforce(fileInputStream)

        then:
        result
        result.passed
        !result.messages
    }

    def "enforce (failed - missing FROM line)"() {
        given:
        DockerfileFromImageEquals DockerfileFromImageEquals = DockerfileFromImageEquals.equals('hub.docker.com')
        InputStream fileInputStream = IoUtil.getResourceAsStream('/Dockerfile-noFrom')

        when:
        EnforcerResult result = DockerfileFromImageEquals.enforce(fileInputStream)

        then:
        result
        !result.passed
        result.messages
        result.messages[0] == "Dockerfile is missing FROM line"
    }

    @Unroll
    def "enforce (passed - #fileSuffix)"() {
        given:
        DockerfileFromImageEquals DockerfileFromImageEquals = DockerfileFromImageEquals.equals(image)
        InputStream fileInputStream = IoUtil.getResourceAsStream("/Dockerfile-${fileSuffix}")

        when:
        EnforcerResult result = DockerfileFromImageEquals.enforce(fileInputStream)

        then:
        result
        result.passed
        !result.messages

        where:
        fileSuffix   | image
        'noFromHost' | 'library/centos'
        'scratch'    | 'scratch'
    }

    def "enforce (failed - incorrect FROM image)"() {
        given:
        DockerfileFromImageEquals DockerfileFromImageEquals = DockerfileFromImageEquals.equals('image2')
        InputStream fileInputStream = IoUtil.getResourceAsStream('/Dockerfile-default')

        when:
        EnforcerResult result = DockerfileFromImageEquals.enforce(fileInputStream)

        then:
        result
        !result.passed
        result.messages
        result.messages[0] == "Dockerfile FROM image [image] does not equal [image2]"
    }

    def "enforce (null input stream)"() {
        given:
        DockerfileFromImageEquals DockerfileFromImageEquals = DockerfileFromImageEquals.equals('hub.docker.com')

        when:
        DockerfileFromImageEquals.enforce(null)

        then:
        thrown(NullPointerException)
    }

}
