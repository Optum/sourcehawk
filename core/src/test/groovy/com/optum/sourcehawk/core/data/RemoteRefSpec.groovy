package com.optum.sourcehawk.core.data

import spock.lang.Specification
import spock.lang.Unroll

class RemoteRefSpec extends Specification {

    def "builder and toString"() {
        given:
        RemoteRef.Type type = RemoteRef.Type.GITHUB
        String namespace = "namespace"
        String repository = "repository"
        String ref = "ref"

        when:
        RemoteRef remoteRef = RemoteRef.builder()
                .type(type)
                .namespace(namespace)
                .repository(repository)
                .ref(ref)
                .build()

        then:
        remoteRef
        remoteRef.type == type
        remoteRef.namespace == namespace
        remoteRef.repository == repository
        remoteRef.ref == ref

        and:
        remoteRef.toString() == "[GITHUB] namespace/repository@ref"
    }

    def "parse"() {
        given:
        RemoteRef.Type type = RemoteRef.Type.GITHUB
        String rawReference = "namespace/repository@ref"

        when:
        RemoteRef remoteRef = RemoteRef.parse(type, rawReference)

        then:
        remoteRef
        remoteRef.type == type
        remoteRef.namespace == "namespace"
        remoteRef.repository == "repository"
        remoteRef.ref == "ref"

        and:
        remoteRef.toString() == "[GITHUB] ${rawReference}"
    }

    @Unroll
    def "parse - no ref - defaulted (#type)"() {
        given:
        String rawReference = "namespace/repository"

        when:
        RemoteRef remoteRef = RemoteRef.parse(type, rawReference)

        then:
        remoteRef
        remoteRef.type == type
        remoteRef.namespace == "namespace"
        remoteRef.repository == "repository"
        remoteRef.ref == expected

        and:
        remoteRef.toString() == "[${type.name()}] ${rawReference}@${expected}"

        where:
        type                     | expected
        RemoteRef.Type.GITHUB    | "main"
        RemoteRef.Type.BITBUCKET | "master"
    }

    @Unroll
    def "parse - invalid (throws IllegalArgumentException)"() {
        given:
        RemoteRef.Type type = RemoteRef.Type.GITHUB

        when:
        RemoteRef.parse(type, rawReference)

        then:
        thrown(IllegalArgumentException)

        where:
        rawReference << [ "project", "owner/", "project/repo@" ]
    }

}
