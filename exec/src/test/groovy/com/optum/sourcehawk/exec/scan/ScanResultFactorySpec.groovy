package com.optum.sourcehawk.exec.scan

import com.optum.sourcehawk.core.scan.ScanResult
import com.optum.sourcehawk.core.scan.Severity
import com.optum.sourcehawk.enforcer.EnforcerResult
import com.optum.sourcehawk.enforcer.file.common.StringPropertyEquals
import com.optum.sourcehawk.core.protocol.file.FileProtocol
import com.optum.sourcehawk.exec.ExecOptions
import com.optum.sourcehawk.exec.FileBaseSpecification
import com.optum.sourcehawk.exec.scan.ScanResultFactory

import java.nio.file.Paths

class ScanResultFactorySpec extends FileBaseSpecification {

    def "enforcerResult - passed"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .build()
        String repositoryPath = "path/file.ext"
        EnforcerResult enforcerResult = EnforcerResult.passed()

        when:
        ScanResult scanResult = ScanResultFactory.enforcerResult(execOptions, repositoryPath, Severity.ERROR, enforcerResult)

        then:
        scanResult
        scanResult.passed
        scanResult.warningCount == 0
        scanResult.errorCount == 0
        scanResult.messages.size() == 0
        scanResult.formattedMessages.size() == 0
    }

    def "enforcerResult - failed (error)"() {
        given:
        String repositoryPath = "path/file.ext"
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(Paths.get(repositoryPath))
                .build()
        EnforcerResult enforcerResult = EnforcerResult.failed("Property [key] is null")

        when:
        ScanResult scanResult = ScanResultFactory.enforcerResult(execOptions, repositoryPath, Severity.ERROR, enforcerResult)

        then:
        scanResult
        !scanResult.passed
        scanResult.warningCount == 0
        scanResult.errorCount == 1
        scanResult.messages
        scanResult.messages.size() == 1
        scanResult.messages[repositoryPath]
        scanResult.messages[repositoryPath].size() == 1
        scanResult.formattedMessages
        scanResult.formattedMessages.size() == 1
        scanResult.formattedMessages[0] == "[ERROR] path/file.ext :: Property [key] is null"

        when:
        ScanResult.MessageDescriptor messageDescriptor = scanResult.messages[repositoryPath][0]

        then:
        messageDescriptor.repositoryPath == repositoryPath
        messageDescriptor.severity == Severity.ERROR.name()
        messageDescriptor.message == enforcerResult.messages[0]
    }

    def "enforcerResult - failed (warning)"() {
        given:
        String repositoryPath = "path/file.ext"
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(Paths.get(repositoryPath))
                .failOnWarnings(true)
                .build()
        EnforcerResult enforcerResult = EnforcerResult.builder()
                .passed(false)
                .messages(["Property [key] is null"])
                .build()

        when:
        ScanResult scanResult = ScanResultFactory.enforcerResult(execOptions, repositoryPath, Severity.WARNING, enforcerResult)

        then:
        scanResult
        !scanResult.passed
        scanResult.warningCount == 1
        scanResult.errorCount == 0
        scanResult.messages.size() == 1
        scanResult.messages[repositoryPath]
        scanResult.messages[repositoryPath].size() == 1
        scanResult.formattedMessages
        scanResult.formattedMessages.size() == 1
        scanResult.formattedMessages[0] == "[WARNING] path/file.ext :: Property [key] is null"

        when:
        ScanResult.MessageDescriptor messageDescriptor = scanResult.messages[repositoryPath][0]

        then:
        messageDescriptor.repositoryPath == repositoryPath
        messageDescriptor.severity == Severity.WARNING.name()
        messageDescriptor.message == enforcerResult.messages[0]
    }

    def "enforcerResult - passed (warning)"() {
        given:
        String repositoryPath = "path/file.ext"
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(Paths.get(repositoryPath))
                .build()
        EnforcerResult enforcerResult = EnforcerResult.builder()
                .passed(false)
                .messages(["Property [key] is null"])
                .build()

        when:
        ScanResult scanResult = ScanResultFactory.enforcerResult(execOptions, repositoryPath, Severity.WARNING, enforcerResult)

        then:
        scanResult
        scanResult.passed
        scanResult.warningCount == 1
        scanResult.errorCount == 0
        scanResult.messages.size() == 1
        scanResult.messages[repositoryPath]
        scanResult.messages[repositoryPath].size() == 1
        scanResult.formattedMessages
        scanResult.formattedMessages.size() == 1
        scanResult.formattedMessages[0] == "[WARNING] path/file.ext :: Property [key] is null"

        when:
        ScanResult.MessageDescriptor messageDescriptor = scanResult.messages[repositoryPath][0]

        then:
        messageDescriptor.repositoryPath == repositoryPath
        messageDescriptor.severity == Severity.WARNING.name()
        messageDescriptor.message == enforcerResult.messages[0]
    }

    def "error"() {
        given:
        String repositoryPath = "/repository/file/"
        String message = "Sourcehawk, we have a problem..."

        when:
        ScanResult scanResult = ScanResultFactory.error(repositoryPath, message)

        then:
        scanResult
        !scanResult.passed
        scanResult.errorCount == 1
        scanResult.warningCount == 0
        scanResult.formattedMessages
        scanResult.formattedMessages.size() == 1
        scanResult.formattedMessages[0] == message
        scanResult.messages
        scanResult.messages.size() == 1
        scanResult.messages[repositoryPath]
        scanResult.messages[repositoryPath].size() == 1

        when:
        ScanResult.MessageDescriptor messageDescriptor = scanResult.messages[repositoryPath][0]

        then:
        messageDescriptor
        messageDescriptor.repositoryPath == repositoryPath
        messageDescriptor.severity == Severity.ERROR.name()
        messageDescriptor.message == message
    }

    def "fileNotFound"() {
        given:
        Map<String, Object> enforcers = [
                "path/file.ext": StringPropertyEquals.equals("key", "value")
        ]
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .build()
        FileProtocol fileProtocol = FileProtocol.builder()
                .repositoryPath("path/file.ext")
                .name("protocol")
                .enforcers([ enforcers ])
                .build()

        when:
        ScanResult scanResult = ScanResultFactory.fileNotFound(execOptions, fileProtocol)

        then:
        scanResult
        !scanResult.passed
        scanResult.errorCount == 1
        scanResult.warningCount == 0
        scanResult.formattedMessages
        scanResult.formattedMessages.size() == 1
        scanResult.formattedMessages[0] == "[ERROR] path/file.ext :: File not found"
        scanResult.messages
        scanResult.messages.size() == 1
        scanResult.messages[fileProtocol.repositoryPath]
        scanResult.messages[fileProtocol.repositoryPath].size() == 1

        when:
        ScanResult.MessageDescriptor messageDescriptor = scanResult.messages[fileProtocol.repositoryPath][0]

        then:
        messageDescriptor
        messageDescriptor.repositoryPath == fileProtocol.repositoryPath
        messageDescriptor.severity == Severity.ERROR.name()
        messageDescriptor.message == "File not found"
    }

    def "fileNotFound - WARNING"() {
        given:
        Map<String, Object> enforcers = [
                "path/file.ext": StringPropertyEquals.equals("key", "value")
        ]
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .build()
        FileProtocol fileProtocol = FileProtocol.builder()
                .repositoryPath("path/file.ext")
                .name("protocol")
                .severity(Severity.WARNING.name())
                .enforcers([ enforcers ])
                .build()

        when:
        ScanResult scanResult = ScanResultFactory.fileNotFound(execOptions, fileProtocol)

        then:
        scanResult
        scanResult.passed
        scanResult.errorCount == 0
        scanResult.warningCount == 1
        scanResult.formattedMessages
        scanResult.formattedMessages.size() == 1
        scanResult.formattedMessages[0] == "[WARNING] path/file.ext :: File not found"
        scanResult.messages
        scanResult.messages.size() == 1
        scanResult.messages[fileProtocol.repositoryPath]
        scanResult.messages[fileProtocol.repositoryPath].size() == 1

        when:
        ScanResult.MessageDescriptor messageDescriptor = scanResult.messages[fileProtocol.repositoryPath][0]

        then:
        messageDescriptor
        messageDescriptor.repositoryPath == fileProtocol.repositoryPath
        messageDescriptor.severity == Severity.WARNING.name()
        messageDescriptor.message == "File not found"
    }

    def "acceptCount - RECOMMENDATION"() {
        given:
        ScanResult.ScanResultBuilder builder = ScanResult.builder()

        when:
        ScanResultFactory.acceptCount(builder, Severity.RECOMMENDATION, 5)

        then:
        noExceptionThrown()
    }

}
