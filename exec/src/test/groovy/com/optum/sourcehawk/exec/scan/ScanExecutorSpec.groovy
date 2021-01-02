package com.optum.sourcehawk.exec.scan

import com.optum.sourcehawk.core.protocol.file.FileProtocol
import com.optum.sourcehawk.core.repository.LocalRepositoryFileReader
import com.optum.sourcehawk.core.repository.RepositoryFileReader
import com.optum.sourcehawk.core.scan.ScanResult
import com.optum.sourcehawk.exec.ConfigurationException
import com.optum.sourcehawk.exec.ExecOptions
import com.optum.sourcehawk.exec.FileBaseSpecification
import com.optum.sourcehawk.exec.scan.ScanExecutor

class ScanExecutorSpec extends FileBaseSpecification {

    def "scan - defaults"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .repositoryFileReader(LocalRepositoryFileReader.create(repositoryRoot))
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
                .repositoryFileReader(LocalRepositoryFileReader.create(repositoryRoot))
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
                .repositoryFileReader(LocalRepositoryFileReader.create(repositoryRoot))
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
                .repositoryFileReader(LocalRepositoryFileReader.create(repositoryRoot))
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
                .repositoryFileReader(LocalRepositoryFileReader.create(repositoryRoot))
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
                .repositoryFileReader(LocalRepositoryFileReader.create(repositoryRoot))
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
                .repositoryFileReader(LocalRepositoryFileReader.create(repositoryRoot))
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
                .repositoryFileReader(LocalRepositoryFileReader.create(repositoryRoot))
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
                .repositoryFileReader(LocalRepositoryFileReader.create(repositoryRoot))
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
                .repositoryFileReader(LocalRepositoryFileReader.create(repositoryRoot))
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
                .repositoryFileReader(LocalRepositoryFileReader.create(repositoryRoot))
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
                .repositoryFileReader(LocalRepositoryFileReader.create(repositoryRoot))
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
                .repositoryFileReader(LocalRepositoryFileReader.create(repositoryRoot))
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
                .repositoryFileReader(LocalRepositoryFileReader.create(repositoryRoot))
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
                .repositoryFileReader(LocalRepositoryFileReader.create(repositoryRoot))
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
        RepositoryFileReader mockRepositoryFileReader = Mock()
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .repositoryFileReader(mockRepositoryFileReader)
                .build()
        FileProtocol fileProtocol = FileProtocol.builder()
                .name("bicycle")
                .repositoryPath("/two/wheels")
                .severity("ERROR")
                .build()

        when:
        ScanResult scanResult = ScanExecutor.enforceFileExists(execOptions, fileProtocol)

        then:
        1 * mockRepositoryFileReader.exists(_ as String) >> {
            throw new IOException("BOOM")
        }
        0 * _

        and:
        scanResult
        !scanResult.passed
    }

    def "enforceFileProtocol - enforcer conversion error"() {
        given:
        RepositoryFileReader mockRepositoryFileReader = Mock()
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .repositoryFileReader(mockRepositoryFileReader)
                .build()
        FileProtocol fileProtocol = FileProtocol.builder()
                .name("bicycle")
                .repositoryPath("/two/wheels")
                .severity("ERROR")
                .enforcers([["bad": "enforcer"]])
                .build()

        when:
        ScanResult scanResult = ScanExecutor.enforceFileProtocol(execOptions, fileProtocol)

        then:
        0 * _

        and:
        scanResult
        !scanResult.passed
    }

    def "processFileProtocol - glob pattern no enforcers"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .repositoryFileReader(LocalRepositoryFileReader.create(repositoryRoot))
                .build()
        FileProtocol fileProtocol = FileProtocol.builder()
                .name("test")
                .repositoryPath("file.txt")
                .build()

        when:
        ScanResult scanResult = ScanExecutor.processFileProtocol(execOptions, fileProtocol)

        then:
        scanResult
        scanResult.errorCount == 1
    }

}
