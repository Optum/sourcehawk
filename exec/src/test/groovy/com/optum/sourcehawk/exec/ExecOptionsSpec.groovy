package com.optum.sourcehawk.exec

import com.optum.sourcehawk.core.data.RemoteRef

import com.optum.sourcehawk.core.repository.LocalRepositoryFileReader
import com.optum.sourcehawk.core.data.Verbosity
import com.optum.sourcehawk.core.repository.RemoteRepositoryFileReader
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
        !execOptions.tags
        !execOptions.failOnWarnings
        execOptions.repositoryFileReader instanceof LocalRepositoryFileReader
        !execOptions.remoteRef

        and:
        execOptions.toString()
    }

    def "builder - custom"() {
        given:
        ExecOptions.ExecOptionsBuilder builder = ExecOptions.builder()
                .repositoryRoot(Paths.get("/"))
                .configurationFileLocation("Sourcehawk")
                .verbosity(Verbosity.ZERO)
                .tags(["foo", "bar"])
                .failOnWarnings(true)

        when:
        ExecOptions execOptions = builder.build()

        then:
        execOptions
        execOptions.repositoryRoot == Paths.get("/")
        execOptions.configurationFileLocation == "Sourcehawk"
        execOptions.verbosity == Verbosity.ZERO
        execOptions.tags == ["foo", "bar"]
        execOptions.failOnWarnings
        execOptions.repositoryFileReader instanceof LocalRepositoryFileReader
        !execOptions.remoteRef
    }

    def "builder - remote"() {
        given:
        RemoteRef remoteRef = RemoteRef.parse("owner/repo", "main")
        ExecOptions.ExecOptionsBuilder builder = ExecOptions.builder()
                .repositoryRoot(Paths.get("/"))
                .configurationFileLocation("Sourcehawk")
                .verbosity(Verbosity.ZERO)
                .failOnWarnings(true)
                .repositoryFileReader(new RemoteRepositoryFileReader("https://raw.githubusercontent.com/owner/repo/main/%s", Collections.emptyMap()))
                .remoteRef(remoteRef)

        when:
        ExecOptions execOptions = builder.build()

        then:
        execOptions
        execOptions.repositoryRoot == Paths.get("/")
        execOptions.configurationFileLocation == "Sourcehawk"
        execOptions.verbosity == Verbosity.ZERO
        !execOptions.tags
        execOptions.failOnWarnings
        execOptions.repositoryFileReader instanceof RemoteRepositoryFileReader
        execOptions.remoteRef == remoteRef
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
                .tags(null)

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

        when:
        ExecOptions.builder()
                .repositoryFileReader(null)

        then:
        thrown(NullPointerException)
    }

}
