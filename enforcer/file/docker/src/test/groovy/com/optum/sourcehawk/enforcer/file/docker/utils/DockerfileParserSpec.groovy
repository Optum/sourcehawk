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

}
