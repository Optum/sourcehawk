package com.optum.sourcehawk.cli

import org.mockserver.configuration.ConfigurationProperties
import org.mockserver.integration.ClientAndServer
import org.mockserver.matchers.Times
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.spockframework.util.IoUtil
import picocli.CommandLine
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class ScanGithubCommandSpec extends Specification {

    @Shared
    @AutoCleanup
    ClientAndServer clientAndServer

    @Shared
    String enterpriseUrl

    def setupSpec() {
        clientAndServer = ClientAndServer.startClientAndServer("http://127.0.0.1", 8123)
        ConfigurationProperties.logLevel("WARN")
        enterpriseUrl = "${clientAndServer.remoteAddress.hostString}:${clientAndServer.port}"
    }

    def setup() {
        clientAndServer.reset()
    }

    def "commandLine.execute enterprise github - custom configuration file (passed)"() {
        given:
        CommandLine commandLine = new CommandLine(new ScanCommand())
        String[] args = ["-c", "target/test-classes/sourcehawk-basic.yml", "github", "-E", enterpriseUrl, "owner/repo" ]
        clientAndServer
                .when(HttpRequest.request()
                        .withMethod("HEAD")
                        .withPath("/raw/owner/repo/main/lombok.config"),
                        Times.exactly(2))
                .respond(HttpResponse.response().withStatusCode(200))
        clientAndServer
                .when(HttpRequest.request()
                        .withMethod("GET")
                        .withPath("/raw/owner/repo/main/lombok.config")
                        .withHeader("Accept", "text/plain"),
                        Times.exactly(2))
                .respond(HttpResponse.response()
                        .withStatusCode(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody(IoUtil.getResourceAsStream("/repo/lombok.config").text))

        when:
        int exitCode = commandLine.execute(args)

        then:
        exitCode == 0
    }

    def "commandLine.execute enterprise github - custom configuration file (failed)"() {
        given:
        CommandLine commandLine = new CommandLine(new ScanCommand())
        String[] args = ["-c", "target/test-classes/sourcehawk-basic2.yml", "github", "-E", enterpriseUrl, "owner/repo@develop" ]
        clientAndServer
                .when(HttpRequest.request()
                        .withMethod("HEAD")
                        .withPath("/raw/owner/repo/develop/lombok2.config"),
                        Times.exactly(2))
                .respond(HttpResponse.response().withStatusCode(200))
        clientAndServer
                .when(HttpRequest.request()
                        .withMethod("GET")
                        .withPath("/raw/owner/repo/develop/lombok2.config")
                        .withHeader("Accept", "text/plain"),
                        Times.exactly(2))
                .respond(HttpResponse.response()
                        .withStatusCode(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody(IoUtil.getResourceAsStream("/repo-updates/lombok.config").text))

        when:
        int exitCode = commandLine.execute(args)

        then:
        exitCode == 1
    }

    def "commandLine.execute enterprise github - configuration file not found (failed)"() {
        given:
        CommandLine commandLine = new CommandLine(new ScanCommand())
        String[] args = ["github", "-E", enterpriseUrl, "owner/repo@develop" ]
        clientAndServer
                .when(HttpRequest.request()
                        .withMethod("GET")
                        .withPath("/raw/owner/repo/develop/sourcehawk.yml")
                        .withHeader("Accept", "text/plain"),
                        Times.exactly(1))
                .respond(HttpResponse.notFoundResponse())

        when:
        int exitCode = commandLine.execute(args)

        then:
        exitCode == 1
    }

}