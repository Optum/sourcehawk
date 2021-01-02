package com.optum.sourcehawk.core.repository

import com.optum.sourcehawk.core.data.RemoteRef
import org.mockserver.configuration.ConfigurationProperties
import org.mockserver.integration.ClientAndServer
import org.mockserver.matchers.Times
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class BitbucketRepositoryFileReaderSpec extends Specification {

    @Shared
    @AutoCleanup
    ClientAndServer clientAndServer

    @Shared
    String baseUrl

    def setupSpec() {
        clientAndServer = ClientAndServer.startClientAndServer("http://127.0.0.1", 8122)
        ConfigurationProperties.logLevel("WARN")
        baseUrl = "${clientAndServer.remoteAddress.hostString}:${clientAndServer.port}"
    }

    def setup() {
        clientAndServer.reset()
    }

    def "constructors"() {
        expect:
        new BitbucketRepositoryFileReader(null, RemoteRef.parse(RemoteRef.Type.BITBUCKET, "project/repo@master"))
        new BitbucketRepositoryFileReader("abc", "https://bitbucket.example.com/", RemoteRef.parse(RemoteRef.Type.BITBUCKET, "project/repo@master"))
    }

    def "supportsGlobPatterns"() {
        given:
        BitbucketRepositoryFileReader reader = new BitbucketRepositoryFileReader(null, RemoteRef.parse(RemoteRef.Type.BITBUCKET, "project/repo@master"))

        when:
        boolean supportsGlobPatterns = reader.supportsGlobPatterns()

        then:
        !supportsGlobPatterns
    }

    def "exists - found"() {
        given:
        RemoteRef remoteRef = RemoteRef.parse(RemoteRef.Type.BITBUCKET, "project/repo@master")
        BitbucketRepositoryFileReader reader = new BitbucketRepositoryFileReader(null, baseUrl, remoteRef)
        clientAndServer
                .when(HttpRequest.request()
                        .withMethod("HEAD")
                        .withPath("/project/repo/raw/master/README.md"),
                        Times.exactly(1))
                .respond(HttpResponse.response().withStatusCode(200))

        when:
        boolean exists = reader.exists("README.md")

        then:
        exists
    }

    def "exists - not found"() {
        given:
        RemoteRef remoteRef = RemoteRef.parse(RemoteRef.Type.BITBUCKET, "project/repo@master")
        BitbucketRepositoryFileReader reader = new BitbucketRepositoryFileReader(null, baseUrl, remoteRef)
        clientAndServer
                .when(HttpRequest.request()
                        .withMethod("HEAD")
                        .withPath("/project/repo/raw/master/README.md"),
                        Times.exactly(1))
                .respond(HttpResponse.notFoundResponse())

        when:
        boolean exists = reader.exists("README.md")

        then:
        !exists
    }

    def "read - found"() {
        given:
        RemoteRef remoteRef = RemoteRef.parse(RemoteRef.Type.BITBUCKET, "project/repo@master")
        BitbucketRepositoryFileReader reader = new BitbucketRepositoryFileReader(null, baseUrl, remoteRef)
        clientAndServer
                .when(HttpRequest.request()
                        .withMethod("HEAD")
                        .withPath("/project/repo/raw/master/README.md"),
                        Times.exactly(1))
                .respond(HttpResponse.response().withStatusCode(200))
        clientAndServer
                .when(HttpRequest.request()
                        .withMethod("GET")
                        .withPath("/project/repo/raw/master/README.md"),
                        Times.exactly(2))
                .respond(HttpResponse.response().withStatusCode(200).withBody("# Title".bytes))

        when:
        Optional<InputStream> inputStreamOptional = reader.read("README.md")

        then:
        inputStreamOptional
        inputStreamOptional.isPresent()

        when:
        inputStreamOptional = reader.read("/README.md")

        then:
        inputStreamOptional
        inputStreamOptional.isPresent()
    }

    def "read - not found"() {
        given:
        RemoteRef remoteRef = RemoteRef.parse(RemoteRef.Type.BITBUCKET, "project/repo@master")
        BitbucketRepositoryFileReader reader = new BitbucketRepositoryFileReader(null, baseUrl, remoteRef)
        clientAndServer
                .when(HttpRequest.request()
                        .withMethod("HEAD")
                        .withPath("/project/repo/raw/master/README.md"),
                        Times.exactly(1))
                .respond(HttpResponse.notFoundResponse())

        when:
        Optional<InputStream> inputStreamOptional = reader.read("README.md")

        then:
        !inputStreamOptional
    }

    def "constructBaseUrl"() {
        given:
        RemoteRef remoteRef = RemoteRef.parse(RemoteRef.Type.BITBUCKET, "project/repo@master")

        when:
        String baseUrl = BitbucketRepositoryFileReader.constructBaseUrl(remoteRef, RemoteRef.Type.BITBUCKET.baseUrl)

        then:
        baseUrl == "https://bitbucket.org/project/repo/raw/master/"
    }

    def "constructor - null parameter"() {
        when:
        new BitbucketRepositoryFileReader("abc", null)

        then:
        thrown(NullPointerException)

        when:
        new BitbucketRepositoryFileReader(null, null)

        then:
        thrown(NullPointerException)

        when:
        new BitbucketRepositoryFileReader("abc", null, RemoteRef.parse(RemoteRef.Type.BITBUCKET, "project/repo@master"))

        then:
        thrown(NullPointerException)

        when:
        new BitbucketRepositoryFileReader(null, null, RemoteRef.parse(RemoteRef.Type.BITBUCKET, "project/repo@master"))

        then:
        thrown(NullPointerException)

        when:
        new BitbucketRepositoryFileReader("abc", null, null)

        then:
        thrown(NullPointerException)

        when:
        new BitbucketRepositoryFileReader(null, null, null)

        then:
        thrown(NullPointerException)
    }

}
