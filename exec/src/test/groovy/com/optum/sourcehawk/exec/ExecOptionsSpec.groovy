package com.optum.sourcehawk.exec


import com.optum.sourcehawk.core.scan.Verbosity
import spock.lang.Specification

import java.nio.file.Paths

class ExecOptionsSpec extends Specification {

    def "builder - defaults"() {
        given:
        ExecOptions.ExecOptionsBuilder builder = ExecOptions.builder()

        when:
        ExecOptions execOptions = builder.build()

        then:
        execOptions
        execOptions.repositoryRoot == Paths.get(".")
        execOptions.configurationFileLocation == "sourcehawk.yml"
        execOptions.verbosity == Verbosity.HIGH
        !execOptions.failOnWarnings
        !execOptions.github

        and:
        execOptions == ExecOptions.builder().build()
        execOptions.hashCode() == ExecOptions.builder().build().hashCode()

        and:
        execOptions.toString()
    }

    def "builder - custom"() {
        given:
        ExecOptions.ExecOptionsBuilder builder = ExecOptions.builder()
                .repositoryRoot(Paths.get("/"))
                .configurationFileLocation("Sourcehawk")
                .verbosity(Verbosity.ZERO)
                .failOnWarnings(true)

        when:
        ExecOptions execOptions = builder.build()

        then:
        execOptions
        execOptions.repositoryRoot == Paths.get("/")
        execOptions.configurationFileLocation == "Sourcehawk"
        execOptions.verbosity == Verbosity.ZERO
        execOptions.failOnWarnings
        !execOptions.github
    }

    def "builder - github"() {
        given:
        ExecOptions.ExecOptionsBuilder builder = ExecOptions.builder()
                .repositoryRoot(Paths.get("/"))
                .configurationFileLocation("Sourcehawk")
                .verbosity(Verbosity.ZERO)
                .failOnWarnings(true)
                .github(ExecOptions.GithubOptions.builder()
                        .coords("owner/repo")
                        .ref("ref")
                        .build())

        when:
        ExecOptions execOptions = builder.build()

        then:
        execOptions
        execOptions.repositoryRoot == Paths.get("/")
        execOptions.configurationFileLocation == "Sourcehawk"
        execOptions.verbosity == Verbosity.ZERO
        execOptions.failOnWarnings
        execOptions.github
        execOptions.github.coords == "owner/repo"
        execOptions.github.ref == "ref"
    }

    def "builder - github - optionals"() {
        given:
        ExecOptions.ExecOptionsBuilder builder = ExecOptions.builder()
                .repositoryRoot(Paths.get("/"))
                .configurationFileLocation("Sourcehawk")
                .verbosity(Verbosity.ZERO)
                .failOnWarnings(true)
                .github(ExecOptions.GithubOptions.builder()
                        .token("token")
                        .coords("owner/repo")
                        .ref("ref")
                        .enterpriseUrl(new URL("https://github.example.com"))
                        .build())

        when:
        ExecOptions execOptions = builder.build()

        then:
        execOptions
        execOptions.repositoryRoot == Paths.get("/")
        execOptions.configurationFileLocation == "Sourcehawk"
        execOptions.verbosity == Verbosity.ZERO
        execOptions.failOnWarnings
        execOptions.github
        execOptions.github.coords == "owner/repo"
        execOptions.github.ref == "ref"
    }

    def "builder - NPE"() {
        when:
        ExecOptions.builder()
                .repositoryRoot(null)

        then:
        thrown(NullPointerException)

        when:
        ExecOptions.builder()
                .verbosity(null)

        then:
        thrown(NullPointerException)

        when:
        ExecOptions.builder()
                .configurationFileLocation(null)

        then:
        thrown(NullPointerException)

        when:
        ExecOptions.builder()
                .outputFormat(null)

        then:
        thrown(NullPointerException)
    }

    def "builder - github - NPE"() {
        when:
        ExecOptions.GithubOptions.builder()
                .build()

        then:
        thrown(NullPointerException)
    }

}
