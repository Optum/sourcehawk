package com.optum.sourcehawk.exec.scan

import com.optum.sourcehawk.core.repository.RepositoryFileReader
import com.optum.sourcehawk.core.scan.ScanResult
import com.optum.sourcehawk.core.protocol.file.FileProtocol
import com.optum.sourcehawk.exec.ConfigurationException
import com.optum.sourcehawk.exec.ExecOptions
import com.optum.sourcehawk.exec.FileBaseSpecification
import com.optum.sourcehawk.exec.scan.ScanExecutor

class ScanExecutorSpec extends FileBaseSpecification {

    def "scan - defaults"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .build()

        when:
        ScanResult scanResult = ScanExecutor.scan(execOptions)

        then:
        scanResult
        scanResult.passed
    }

    def "scan - absolute configuration file"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .configurationFileLocation(repositoryRoot.resolve("sourcehawk.yml").toAbsolutePath().toString())
                .build()

        when:
        ScanResult scanResult = ScanExecutor.scan(execOptions)

        then:
        scanResult
        scanResult.passed
    }

    def "scan - local override"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .configurationFileLocation(repositoryRoot.resolve(".test/override.yml").toString())
                .build()

        when:
        ScanResult scanResult = ScanExecutor.scan(execOptions)

        then:
        scanResult
        scanResult.passed
    }

    def "scan - local override - glob pattern"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .configurationFileLocation(repositoryRoot.resolve(".test/glob-example.yml").toString())
                .build()

        when:
        ScanResult scanResult = ScanExecutor.scan(execOptions)

        then:
        scanResult
        !scanResult.passed
    }

    def "scan - bad url"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .configurationFileLocation(repositoryRoot.resolve(".test/bad-url.yml").toString())
                .build()

        when:
        ScanExecutor.scan(execOptions)

        then:
        thrown(ConfigurationException)
    }


    def "scan - local relative"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .configurationFileLocation(repositoryRoot.resolve(".test/local.yml").toString())
                .build()

        when:
        ScanResult scanResult = ScanExecutor.scan(execOptions)

        then:
        scanResult
        scanResult.passed
    }

    def "scan - URL configuration file"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .configurationFileLocation("https://raw.githubusercontent.com/optum/sourcehawk-parent/main/.sourcehawk/config.yml")
                .build()

        when:
        ScanResult scanResult = ScanExecutor.scan(execOptions)

        then:
        scanResult
        scanResult.passed
    }

    def "scan - relative configuration file - configuration file not found"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .configurationFileLocation("Sourcehawk")
                .build()

        when:
        ScanResult scanResult = ScanExecutor.scan(execOptions)

        then:
        scanResult
        !scanResult.passed
    }

    def "scan - file not found - warning (no enforcers)"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .configurationFileLocation(testResourcesRoot.resolve("sourcehawk-file-not-found.yml").toString())
                .build()

        when:
        ScanResult scanResult = ScanExecutor.scan(execOptions)

        then:
        scanResult
        scanResult.passed

        and:
        noExceptionThrown()
    }

    def "scan - file not found  - fail on warnings (with enforcers)"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .configurationFileLocation(testResourcesRoot.resolve("sourcehawk-file-not-found-enforcers.yml").toString())
                .failOnWarnings(true)
                .build()

        when:
        ScanResult scanResult = ScanExecutor.scan(execOptions)

        then:
        scanResult
        !scanResult.passed

        and:
        noExceptionThrown()
    }

    def "scan - no enforcers"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .configurationFileLocation(testResourcesRoot.resolve("sourcehawk-no-enforcers.yml").toString())
                .build()

        when:
        ScanResult scanResult = ScanExecutor.scan(execOptions)

        then:
        scanResult
        scanResult.passed

        and:
        noExceptionThrown()
    }

    def "scan - enforcer has failed result"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .configurationFileLocation(testResourcesRoot.resolve("sourcehawk-failed-enforcer.yml").toString())
                .build()

        when:
        ScanResult scanResult = ScanExecutor.scan(execOptions)

        then:
        scanResult
        !scanResult.passed

        and:
        noExceptionThrown()
    }

    def "scan - enforcer has failed result (only warning)"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .configurationFileLocation(testResourcesRoot.resolve("sourcehawk-failed-enforcer-only-warning.yml").toString())
                .failOnWarnings(true)
                .build()

        when:
        ScanResult scanResult = ScanExecutor.scan(execOptions)

        then:
        scanResult
        !scanResult.passed

        and:
        noExceptionThrown()
    }

    def "scan - enforcer has passed result (only warning)"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .configurationFileLocation(testResourcesRoot.resolve("sourcehawk-failed-enforcer-only-warning.yml").toString())
                .build()

        when:
        ScanResult scanResult = ScanExecutor.scan(execOptions)

        then:
        scanResult
        scanResult.passed

        and:
        noExceptionThrown()
    }

    def "scan - bad path for file"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .configurationFileLocation(testResourcesRoot.resolve(".i-like-scans.yml").toString())
                .build()

        when:
        ScanResult scanResult = ScanExecutor.scan(execOptions)

        then:
        scanResult
        !scanResult.passed

        and:
        noExceptionThrown()
    }

    def "enforceFileExists - repository file reader error"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .build()
        RepositoryFileReader mockRepositoryFileReader = Mock()
        FileProtocol fileProtocol = FileProtocol.builder()
                .name("bicycle")
                .repositoryPath("/two/wheels")
                .severity("ERROR")
                .build()

        when:
        ScanResult scanResult = ScanExecutor.enforceFileExists(execOptions, mockRepositoryFileReader, fileProtocol)

        then:
        1 * mockRepositoryFileReader.read(_ as String) >> {
            throw new IOException("BOOM")
        }
        0 * _

        and:
        scanResult
        !scanResult.passed
    }

    def "enforceFileProtocol - enforcer conversion error"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .build()
        RepositoryFileReader mockRepositoryFileReader = Mock()
        FileProtocol fileProtocol = FileProtocol.builder()
                .name("bicycle")
                .repositoryPath("/two/wheels")
                .severity("ERROR")
                .enforcers([["bad": "enforcer"]])
                .build()

        when:
        ScanResult scanResult = ScanExecutor.enforceFileProtocol(execOptions, mockRepositoryFileReader, fileProtocol)

        then:
        0 * _

        and:
        scanResult
        !scanResult.passed
    }

}
