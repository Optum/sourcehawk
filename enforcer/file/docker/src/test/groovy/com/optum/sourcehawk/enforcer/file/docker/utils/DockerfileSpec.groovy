package com.optum.sourcehawk.enforcer.file.docker.utils

import spock.lang.Specification

class DockerfileSpec extends Specification {

    def "private constructor"() {
        expect:
        new Dockerfile()
    }

    def "FromToken (builder) - defaults"() {
        when:
        Dockerfile.FromToken fromToken = Dockerfile.FromToken.builder()
                .rawValue("image")
                .image("image")
                .build()

        then:
        fromToken
        fromToken.rawValue == "image"
        !fromToken.registryHost
        fromToken.image == "image"
        !fromToken.tag
    }

    def "FromToken (builder) - provided"() {
        when:
        Dockerfile.FromToken fromToken = Dockerfile.FromToken.builder()
                .rawValue("docker.io/image:1.0.0")
                .registryHost("docker.io")
                .image("image")
                .tag("1.0.0")
                .build()

        then:
        fromToken
        fromToken.rawValue == "docker.io/image:1.0.0"
        fromToken.registryHost == "docker.io"
        fromToken.image == "image"
        fromToken.tag == "1.0.0"
    }

    def "FromToken (builder) - NPE"() {
        when:
        Dockerfile.FromToken.builder().build()

        then:
        thrown(NullPointerException)

        when:
        Dockerfile.FromToken.builder()
                .rawValue("image")
                .build()

        then:
        thrown(NullPointerException)

        when:
        Dockerfile.FromToken.builder()
                .image("image")
                .build()

        then:
        thrown(NullPointerException)
    }

    def "FromToken (equals / toString)"() {
        given:
        Dockerfile.FromToken fromTokeOne = Dockerfile.FromToken.builder()
                .rawValue("image")
                .image("image")
                .build()
        Dockerfile.FromToken fromTokeTwo = Dockerfile.FromToken.builder()
                .rawValue("image")
                .image("image")
                .build()

        expect:
        fromTokeOne == fromTokeTwo
        fromTokeOne.toString() == fromTokeTwo.toString()
        fromTokeOne.hashCode() == fromTokeTwo.hashCode()
    }

}
