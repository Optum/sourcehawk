package com.optum.sourcehawk.core.data

import spock.lang.Specification
import spock.lang.Unroll

class RemoteRefSpec extends Specification {

    def "builder and toString"() {
        given:
        String namespace = "namespace"
        String repository = "repository"
        String ref = "ref"

        when:
        RemoteRef remoteRef = RemoteRef.builder()
                .namespace(namespace)
                .repository(repository)
                .ref(ref)
                .build()

        then:
        remoteRef
        remoteRef.namespace == namespace
        remoteRef.repository == repository
        remoteRef.ref == ref

        and:
        remoteRef.toString() == "namespace/repository@ref"
    }

    def "parse"() {
        given:
        String rawReference = "namespace/repository@ref"

        when:
        RemoteRef remoteRef = RemoteRef.parse(rawReference, "main")

        then:
        remoteRef
        remoteRef.namespace == "namespace"
        remoteRef.repository == "repository"
        remoteRef.ref == "ref"

        and:
        remoteRef.toString() == rawReference
    }

    @Unroll
    def "parse - no ref - defaulted (#type)"() {
        given:
        String rawReference = "namespace/repository"

        when:
        RemoteRef remoteRef = RemoteRef.parse(rawReference, "main")

        then:
        remoteRef
        remoteRef.namespace == "namespace"
        remoteRef.repository == "repository"
        remoteRef.ref == "main"

        and:
        remoteRef.toString() == "${rawReference}@main"
    }

    @Unroll
    def "parse - invalid (throws IllegalArgumentException)"() {
        when:
        RemoteRef.parse(rawReference, "main")

        then:
        thrown(IllegalArgumentException)

        where:
        rawReference << [ "project", "owner/", "project/repo@" ]
    }

}
