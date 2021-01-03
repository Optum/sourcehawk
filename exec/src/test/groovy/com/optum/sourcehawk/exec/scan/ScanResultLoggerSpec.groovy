package com.optum.sourcehawk.exec.scan

import com.optum.sourcehawk.core.data.OutputFormat
import com.optum.sourcehawk.core.data.Severity
import com.optum.sourcehawk.core.data.Verbosity
import com.optum.sourcehawk.core.result.ScanResult
import com.optum.sourcehawk.exec.ExecOptions
import spock.lang.Specification
import spock.lang.Unroll

class ScanResultLoggerSpec extends Specification {

    @Unroll
    def "log - #format (passed)"(OutputFormat format) {
        given:
        ScanResult scanResult = ScanResult.passed()
        ExecOptions execOptions = ExecOptions.builder()
                .outputFormat(format)
                .build()

        when:
        ScanResultLogger.create().log(scanResult, execOptions)

        then:
        noExceptionThrown()

        where:
        format << OutputFormat.values()
    }

    @Unroll
    def "log - #format (failed)"(OutputFormat format) {
        given:
        ScanResult.MessageDescriptor messageDescriptor = ScanResult.MessageDescriptor.builder()
                .severity(Severity.ERROR.name())
                .repositoryPath("file.ext")
                .message("WRONG!")
                .build()
        ScanResult scanResult = ScanResult.builder()
                .passed(false)
                .errorCount(1)
                .warningCount(0)
                .messages(["file.ext": [messageDescriptor]])
                .formattedMessages(["[ERROR] file.ext :: WRONG!"])
                .build()
        ExecOptions execOptions = ExecOptions.builder()
                .outputFormat(format)
                .build()

        when:
        ScanResultLogger.create().log(scanResult, execOptions)

        then:
        noExceptionThrown()

        where:
        format << OutputFormat.values()
    }

    def "formatJson"() {
        expect:
        ScanResultLogger.create().formatJson(null)
        ScanResultLogger.create().formatJson(ScanResult.builder().build())
    }

    def "formatMarkdown - passed (HIGH Verbosity)"() {
        given:
        ScanResult scanResult = ScanResult.passed()

        when:
        String markdown = ScanResultLogger.create().formatMarkdown(scanResult, Verbosity.HIGH)

        then:
        markdown
        markdown == """## Sourcehawk

Scan passed without any errors"""
    }

    def "formatMarkdown - passed with warnings (HIGH Verbosity)"() {
        given:
        ScanResult scanResult = ScanResult.builder()
                .passed(true)
                .warningCount(1)
                .messages(["file.txt": [ ScanResult.MessageDescriptor.builder().severity(Severity.WARNING.name()).repositoryPath("file.txt").message("msg").build() ]])
                .formattedMessages(["[WARNING] file.txt :: msg"])
                .build()

        when:
        String markdown = ScanResultLogger.create().formatMarkdown(scanResult, Verbosity.HIGH)

        then:
        markdown
        markdown == """## Sourcehawk

Scan passed. Errors: 0, Warning(s): 1

### Results

* [WARNING] file.txt :: msg
"""
    }

    def "formatMarkdown - failed (HIGH Verbosity)"() {
        given:
        ScanResult.MessageDescriptor messageDescriptor = ScanResult.MessageDescriptor.builder()
                .severity(Severity.ERROR.name())
                .repositoryPath("file.ext")
                .message("WRONG!")
                .build()
        ScanResult scanResult = ScanResult.builder()
                .passed(false)
                .errorCount(1)
                .warningCount(0)
                .messages(["file.ext": [messageDescriptor]])
                .formattedMessages(["[ERROR] file.ext :: WRONG!"])
                .build()

        when:
        String markdown = ScanResultLogger.create().formatMarkdown(scanResult, Verbosity.HIGH)

        then:
        markdown
        markdown == """## Sourcehawk

Scan resulted in failure. Error(s): 1, Warning(s): 0

### Results

* [ERROR] file.ext :: WRONG!
"""
    }

}
