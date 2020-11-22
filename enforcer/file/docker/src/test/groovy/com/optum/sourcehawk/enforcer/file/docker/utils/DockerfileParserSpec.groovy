package com.optum.sourcehawk.enforcer.file.docker.utils

import org.spockframework.util.IoUtil
import spock.lang.Specification

class DockerfileParserSpec extends Specification {

    def "constructor"() {
        expect:
        new DockerfileParser()
    }

    def "collectTokenValues - none found"() {
        given:
        InputStream dockerfileInputStream = IoUtil.getResourceAsStream("/Dockerfile-noFrom")

        when:
        Collection<String> tokenValues = DockerfileParser.collectTokenValues(dockerfileInputStream, "FROM ")

        then:
        !tokenValues
    }

    def "collectTokenValues - 1 found"() {
        given:
        InputStream dockerfileInputStream = IoUtil.getResourceAsStream("/Dockerfile-default")

        when:
        Collection<String> tokenValues = DockerfileParser.collectTokenValues(dockerfileInputStream, "FROM ")

        then:
        tokenValues
        tokenValues.size() == 1
        tokenValues[0] == "hub.docker.com/image:1.0.0"
    }

    def "collectTokenValues - multiple found"() {
        given:
        InputStream dockerfileInputStream = IoUtil.getResourceAsStream("/Dockerfile-multipleFromsNoTags")

        when:
        Collection<String> tokenValues = DockerfileParser.collectTokenValues(dockerfileInputStream, "FROM ")

        then:
        tokenValues
        tokenValues.size() == 3
        tokenValues[0] == "scratch"
        tokenValues[1] == "node"
        tokenValues[2] == "nginx"
    }

    def "parseFromToken - only image"() {
        given:
        String fromTokenString = "image"

        when:
        Dockerfile.FromToken fromToken = DockerfileParser.parseFromToken(fromTokenString)

        then:
        fromToken
        fromToken.rawValue == fromTokenString
        !fromToken.registryHost
        fromToken.image == fromTokenString
        !fromToken.tag
    }

    def "parseFromToken - image and tag"() {
        given:
        String fromTokenString = "image:1.0"

        when:
        Dockerfile.FromToken fromToken = DockerfileParser.parseFromToken(fromTokenString)

        then:
        fromToken
        fromToken.rawValue == fromTokenString
        !fromToken.registryHost
        fromToken.image == "image"
        fromToken.tag == "1.0"
    }

    def "parseFromToken - image with '/' and tag"() {
        given:
        String fromTokenString = "org/image:1.0"

        when:
        Dockerfile.FromToken fromToken = DockerfileParser.parseFromToken(fromTokenString)

        then:
        fromToken
        fromToken.rawValue == fromTokenString
        !fromToken.registryHost
        fromToken.image == "org/image"
        fromToken.tag == "1.0"
    }

    def "parseFromToken - registry host and image"() {
        given:
        String fromTokenString = "docker.io/image"

        when:
        Dockerfile.FromToken fromToken = DockerfileParser.parseFromToken(fromTokenString)

        then:
        fromToken
        fromToken.rawValue == fromTokenString
        fromToken.registryHost == "docker.io"
        fromToken.image == "image"
        !fromToken.tag
    }

    def "parseFromToken - registry host with port and image"() {
        given:
        String fromTokenString = "docker:8080/image"

        when:
        Dockerfile.FromToken fromToken = DockerfileParser.parseFromToken(fromTokenString)

        then:
        fromToken
        fromToken.rawValue == fromTokenString
        fromToken.registryHost == "docker:8080"
        fromToken.image == "image"
        !fromToken.tag
    }

    def "parseFromToken - registry host, image, and tag"() {
        given:
        String fromTokenString = "docker.io/image:1.0"

        when:
        Dockerfile.FromToken fromToken = DockerfileParser.parseFromToken(fromTokenString)

        then:
        fromToken
        fromToken.rawValue == fromTokenString
        fromToken.registryHost == "docker.io"
        fromToken.image == "image"
        fromToken.tag == "1.0"
    }

    def "parseFromToken - registry host with port, image with '/', and tag"() {
        given:
        String fromTokenString = "docker:8080/org/image:1.0"

        when:
        Dockerfile.FromToken fromToken = DockerfileParser.parseFromToken(fromTokenString)

        then:
        fromToken
        fromToken.rawValue == fromTokenString
        fromToken.registryHost == "docker:8080"
        fromToken.image == "org/image"
        fromToken.tag == "1.0"
    }

}
