package com.optum.sourcehawk.enforcer.file.docker


import com.optum.sourcehawk.enforcer.EnforcerResult
import org.spockframework.util.IoUtil
import spock.lang.Specification

class DockerfileFromRegistryEqualsSpec extends Specification {

    def "equals"() {
        expect:
        DockerfileFromRegistryEquals.equals("hub.docker.com")
    }

    def "enforce - null input stream"() {
        when:
        DockerfileFromRegistryEquals.equals("docker.io").enforceInternal(null)

        then:
        thrown(NullPointerException)
    }

    def "enforce (passed)"() {
        given:
        DockerfileFromRegistryEquals dockerfileFromHostEquals = DockerfileFromRegistryEquals.equals('hub.docker.com')
        InputStream fileInputStream = IoUtil.getResourceAsStream('/Dockerfile-default')

        when:
        EnforcerResult result = dockerfileFromHostEquals.enforce(fileInputStream)

        then:
        result
        result.passed
        !result.messages
    }

    def "enforce (failed - missing FROM line)"() {
        given:
        DockerfileFromRegistryEquals dockerfileFromHostEquals = DockerfileFromRegistryEquals.equals('hub.docker.com')
        InputStream fileInputStream = IoUtil.getResourceAsStream('/Dockerfile-noFrom')

        when:
        EnforcerResult result = dockerfileFromHostEquals.enforce(fileInputStream)

        then:
        result
        !result.passed
        result.messages
        result.messages.size() == 1
        result.messages[0] == "Dockerfile is missing FROM line"
    }

    def "enforce (failed - missing FROM host)"() {
        given:
        DockerfileFromRegistryEquals dockerfileFromHostEquals = DockerfileFromRegistryEquals.equals('hub.docker.com')
        InputStream fileInputStream = IoUtil.getResourceAsStream("/Dockerfile-noFromHost")

        when:
        EnforcerResult result = dockerfileFromHostEquals.enforce(fileInputStream)

        then:
        result
        !result.passed
        result.messages
        result.messages.size() == 1
        result.messages[0] == "Dockerfile FROM [library/centos:1.0.0] is missing host prefix"
    }

    def "enforce (failed - incorrect FROM host)"() {
        given:
        DockerfileFromRegistryEquals dockerfileFromHostEquals = DockerfileFromRegistryEquals.equals('sub.docker.com')
        InputStream fileInputStream = IoUtil.getResourceAsStream('/Dockerfile-default')

        when:
        EnforcerResult result = dockerfileFromHostEquals.enforce(fileInputStream)

        then:
        result
        !result.passed
        result.messages
        result.messages.size() == 1
        result.messages[0] == "Dockerfile FROM host [hub.docker.com] does not equal [sub.docker.com]"
    }

    def "enforce (failed - multiples missing FROM host)"() {
        given:
        DockerfileFromRegistryEquals dockerfileFromHostEquals = DockerfileFromRegistryEquals.equals('sub.docker.com')
        InputStream fileInputStream = IoUtil.getResourceAsStream('/Dockerfile-multipleFromsNoTags')

        when:
        EnforcerResult result = dockerfileFromHostEquals.enforce(fileInputStream)

        then:
        result
        !result.passed
        result.messages
        result.messages.size() == 2
        result.messages[0] == "Dockerfile FROM [node] is missing host prefix"
        result.messages[1] == "Dockerfile FROM [nginx] is missing host prefix"
    }

    def "enforce (null input stream)"() {
        given:
        DockerfileFromRegistryEquals dockerfileFromHostEquals = DockerfileFromRegistryEquals.equals('hub.docker.com')

        when:
        dockerfileFromHostEquals.enforce(null)

        then:
        thrown(NullPointerException)
    }

}
