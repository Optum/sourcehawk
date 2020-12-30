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
import spock.lang.Unroll

class GithubRepositoryFileReaderSpec extends Specification {

    @Shared
    @AutoCleanup
    ClientAndServer clientAndServer

    @Shared
    String enterpriseUrl

    def setupSpec() {
        clientAndServer = ClientAndServer.startClientAndServer("http://127.0.0.1", 8121)
        ConfigurationProperties.logLevel("WARN")
        enterpriseUrl = "${clientAndServer.remoteAddress.hostString}:${clientAndServer.port}"
    }

    def setup() {
        clientAndServer.reset()
    }

    def "supportsGlobPatterns"() {
        given:
        RemoteRef remoteRef = RemoteRef.parse(RemoteRef.Type.GITHUB, "owner/repo@main")
        GithubRepositoryFileReader githubRepositoryFileReader = new GithubRepositoryFileReader(null, enterpriseUrl, remoteRef)

        when:
        boolean supportsGlobPatterns = githubRepositoryFileReader.supportsGlobPatterns()

        then:
        !supportsGlobPatterns
    }

    def "exists (found)"() {
        given:
        RemoteRef remoteRef = RemoteRef.parse(RemoteRef.Type.GITHUB, "owner/repo@main")
        GithubRepositoryFileReader githubRepositoryFileReader = new GithubRepositoryFileReader(null, enterpriseUrl, remoteRef)
        clientAndServer
                .when(HttpRequest.request()
                        .withMethod("HEAD")
                        .withPath("/raw/owner/repo/main/README.md"),
                        Times.exactly(1))
                .respond(HttpResponse.response().withStatusCode(200))

        when:
        boolean exists = githubRepositoryFileReader.exists("README.md")

        then:
        exists
    }

    def "exists (not found)"() {
        given:
        RemoteRef remoteRef = RemoteRef.parse(RemoteRef.Type.GITHUB, "owner/repo@nope")
        GithubRepositoryFileReader githubRepositoryFileReader = new GithubRepositoryFileReader(null, enterpriseUrl, remoteRef)
        clientAndServer
                .when(HttpRequest.request()
                        .withMethod("HEAD")
                        .withPath("/raw/owner/repo/main/README.md"),
                        Times.exactly(1))
                .respond(HttpResponse.notFoundResponse())

        when:
        boolean exists = githubRepositoryFileReader.exists("README.md")

        then:
        !exists
    }

    def "read (found)"() {
        given:
        RemoteRef remoteRef = RemoteRef.parse(RemoteRef.Type.GITHUB, "owner/repo@main")
        GithubRepositoryFileReader githubRepositoryFileReader = new GithubRepositoryFileReader(null, enterpriseUrl, remoteRef)
        clientAndServer
                .when(HttpRequest.request()
                        .withMethod("HEAD")
                        .withPath("/raw/owner/repo/main/README.md"),
                        Times.exactly(1))
                .respond(HttpResponse.response().withStatusCode(200))
        clientAndServer
                .when(HttpRequest.request()
                        .withMethod("GET")
                        .withPath("/raw/owner/repo/main/README.md"),
                        Times.exactly(2))
                .respond(HttpResponse.response().withStatusCode(200).withBody("# Title".bytes))

        when:
        Optional<InputStream> inputStream = githubRepositoryFileReader.read("README.md")

        then:
        inputStream
        inputStream.isPresent()

        when:
        inputStream = githubRepositoryFileReader.read("/README.md")

        then:
        inputStream
        inputStream.isPresent()
    }

    def "read (not found)"() {
        given:
        RemoteRef remoteRef = RemoteRef.parse(RemoteRef.Type.GITHUB, "owner/repo@master")
        GithubRepositoryFileReader githubRepositoryFileReader = new GithubRepositoryFileReader(null, remoteRef)
        clientAndServer
                .when(HttpRequest.request()
                        .withMethod("HEAD")
                        .withPath("/raw/owner/repo/master/README.md"),
                        Times.exactly(1))
                .respond(HttpResponse.notFoundResponse())

        when:
        Optional<InputStream> inputStream = githubRepositoryFileReader.read("README.md")

        then:
        !inputStream
        !inputStream.isPresent()
    }

    def "constructBaseUrl - public github"() {
        given:
        String githubUrl = "https://raw.githubusercontent.com"
        RemoteRef remoteRef = RemoteRef.parse(RemoteRef.Type.GITHUB, "owner/repo@main")

        when:
        String baseUrl = GithubRepositoryFileReader.constructBaseUrl(githubUrl, false, remoteRef)

        then:
        baseUrl == "https://raw.githubusercontent.com/owner/repo/main/"
    }

    @Unroll
    def "constructBaseUrl - enterprise github"() {
        given:
        RemoteRef remoteRef = RemoteRef.parse(RemoteRef.Type.GITHUB, "owner/repo@main")

        when:
        String baseUrl = GithubRepositoryFileReader.constructBaseUrl(githubUrl, true, remoteRef)

        then:
        baseUrl == "https://github.example.com/raw/owner/repo/main/"

        where:
        githubUrl << ["https://github.example.com", "https://github.example.com/"]
    }

    def "constructRequestProperties"() {
        when:
        Map<String, String> requestProperties = GithubRepositoryFileReader.constructRequestProperties("token ", "abc")

        then:
        requestProperties
        requestProperties.size() == 2
        requestProperties["Accept"] == "text/plain"
        requestProperties["Authorization"] == "token abc"
    }

    def "constructRequestProperties - null"() {
        when:
        Map<String, String> requestProperties = GithubRepositoryFileReader.constructRequestProperties("token ", null)

        then:
        requestProperties
        requestProperties.size() == 1
        requestProperties["Accept"] == "text/plain"
    }

    def "constructor - enterprise"() {
        given:
        RemoteRef remoteRef = RemoteRef.parse(RemoteRef.Type.GITHUB, "owner/repo@main")

        expect:
        new GithubRepositoryFileReader(null, "https://github.example.com", remoteRef)
        new GithubRepositoryFileReader("abc", "https://github.example.com", remoteRef)
    }

    def "constructors - null parameter"() {
        when:
        new GithubRepositoryFileReader(null, null, null)

        then:
        thrown(NullPointerException)

        when:
        new GithubRepositoryFileReader("abc", null, null)

        then:
        thrown(NullPointerException)

        when:
        new GithubRepositoryFileReader("abc", null, RemoteRef.parse(RemoteRef.Type.GITHUB, "owner/repo@main"))

        then:
        thrown(NullPointerException)

        when:
        new GithubRepositoryFileReader("abc", "https://github.example.com", null)

        then:
        thrown(NullPointerException)

        when:
        new GithubRepositoryFileReader(null, "https://github.example.com", null)

        then:
        thrown(NullPointerException)

        when:
        new GithubRepositoryFileReader(null, null, RemoteRef.parse(RemoteRef.Type.GITHUB, "owner/repo@main"))

        then:
        thrown(NullPointerException)

        when:
        new GithubRepositoryFileReader("abc", null)

        then:
        thrown(NullPointerException)

        when:
        new GithubRepositoryFileReader(null, null)

        then:
        thrown(NullPointerException)
    }

}
