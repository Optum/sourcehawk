package com.optum.sourcehawk.core.repository


import org.mockserver.configuration.ConfigurationProperties
import org.mockserver.integration.ClientAndServer
import org.mockserver.matchers.Times
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class RemoteRepositoryFileReaderSpec extends Specification {

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

    def "supportsGlobPatterns"() {
        given:
        RepositoryFileReader reader = new RemoteRepositoryFileReader("rawFileUrlTemplate", Collections.emptyMap())

        when:
        boolean supportsGlobPatterns = reader.supportsGlobPatterns()

        then:
        !supportsGlobPatterns
    }

    def "exists - found"() {
        given:
        String rawFileUrlTemplate = "$baseUrl/project/repo/raw/master/%s"
        RepositoryFileReader reader = new RemoteRepositoryFileReader(rawFileUrlTemplate, Collections.emptyMap())
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
        String rawFileUrlTemplate = "$baseUrl/project/repo/raw/master/%s"
        RepositoryFileReader reader = new RemoteRepositoryFileReader(rawFileUrlTemplate, Collections.emptyMap())
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
        String rawFileUrlTemplate = "$baseUrl/raw/project/repo/main/%s"
        RepositoryFileReader reader = new RemoteRepositoryFileReader(rawFileUrlTemplate, Collections.emptyMap())
        clientAndServer
                .when(HttpRequest.request()
                        .withMethod("HEAD")
                        .withPath("/raw/project/repo/main/README.md"),
                        Times.exactly(1))
                .respond(HttpResponse.response().withStatusCode(200))
        clientAndServer
                .when(HttpRequest.request()
                        .withMethod("GET")
                        .withPath("/raw/project/repo/main/README.md"),
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
        String rawFileUrlTemplate = "$baseUrl/raw/project/repo/main/%s"
        RepositoryFileReader reader = new RemoteRepositoryFileReader(rawFileUrlTemplate, Collections.emptyMap())
        clientAndServer
                .when(HttpRequest.request()
                        .withMethod("HEAD")
                        .withPath("/raw/project/repo/main/README.md"),
                        Times.exactly(1))
                .respond(HttpResponse.notFoundResponse())

        when:
        Optional<InputStream> inputStreamOptional = reader.read("README.md")

        then:
        !inputStreamOptional.isPresent()
    }

    def "getAbsoluteLocation"() {
        given:
        String rawFileUrlTemplate = "$baseUrl/raw/project/repo/main/%s"
        RepositoryFileReader reader = new RemoteRepositoryFileReader(rawFileUrlTemplate, Collections.emptyMap())
        String repositoryFilePath = "README.md"

        when:
        String absoluteLocation = reader.getAbsoluteLocation(repositoryFilePath)

        then:
        absoluteLocation
        absoluteLocation == "$baseUrl/raw/project/repo/main/README.md"


        when:
        repositoryFilePath = "/path/to/file.txt"
        absoluteLocation = reader.getAbsoluteLocation(repositoryFilePath)

        then:
        absoluteLocation
        absoluteLocation == "$baseUrl/raw/project/repo/main/path/to/file.txt"
    }

    def "constructor - null parameter"() {
        when:
        new RemoteRepositoryFileReader("abc", null)

        then:
        thrown(NullPointerException)

        when:
        new RemoteRepositoryFileReader(null, Collections.emptyMap())

        then:
        thrown(NullPointerException)

        when:
        new RemoteRepositoryFileReader(null, null)

        then:
        thrown(NullPointerException)
    }

}