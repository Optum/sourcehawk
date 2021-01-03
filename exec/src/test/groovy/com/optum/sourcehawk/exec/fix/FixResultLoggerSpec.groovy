package com.optum.sourcehawk.exec.fix

import com.optum.sourcehawk.core.data.OutputFormat
import com.optum.sourcehawk.core.data.Verbosity
import com.optum.sourcehawk.core.result.FixResult
import com.optum.sourcehawk.exec.ExecOptions
import spock.lang.Specification
import spock.lang.Unroll

class FixResultLoggerSpec extends Specification {

    @Unroll
    def "log - #format (fixes applied)"(OutputFormat format) {
        given:
        FixResult fixResult = FixResult.builder()
                .fixesApplied(true)
                .fixCount(1)
                .messages(["file.ext": [FixResult.MessageDescriptor.builder().repositoryPath("file.ext").message("Fixed").build() ]])
                .formattedMessages([ "file.ext :: Fixed" ])
                .build()
        ExecOptions execOptions = ExecOptions.builder()
                .outputFormat(format)
                .build()

        when:
        FixResultLogger.create(false).log(fixResult, execOptions)

        then:
        noExceptionThrown()

        where:
        format << OutputFormat.values()
    }

    @Unroll
    def "log - #format (fixes applied - dry run)"(OutputFormat format) {
        given:
        FixResult fixResult = FixResult.builder()
                .fixesApplied(true)
                .fixCount(1)
                .messages(["file.ext": [FixResult.MessageDescriptor.builder().repositoryPath("file.ext").message("Fixed").build() ]])
                .formattedMessages([ "file.ext :: Fixed" ])
                .build()
        ExecOptions execOptions = ExecOptions.builder()
                .outputFormat(format)
                .build()

        when:
        FixResultLogger.create(true).log(fixResult, execOptions)

        then:
        noExceptionThrown()

        where:
        format << OutputFormat.values()
    }

    @Unroll
    def "log - #format (error)"(OutputFormat format) {
        given:
        FixResult fixResult = FixResult.builder()
                .error(true)
                .errorCount(1)
                .messages(["file.ext": [FixResult.MessageDescriptor.builder().repositoryPath("file.ext").message("Error!").build() ]])
                .formattedMessages([ "file.ext :: Error!" ])
                .build()
        ExecOptions execOptions = ExecOptions.builder()
                .outputFormat(format)
                .build()

        when:
        FixResultLogger.create(false).log(fixResult, execOptions)

        then:
        noExceptionThrown()

        where:
        format << OutputFormat.values()
    }

    @Unroll
    def "log - #format (error - dry run)"(OutputFormat format) {
        given:
        FixResult fixResult = FixResult.builder()
                .error(true)
                .errorCount(1)
                .messages(["file.ext": [FixResult.MessageDescriptor.builder().repositoryPath("file.ext").message("Error!").build() ]])
                .formattedMessages([ "file.ext :: Error!" ])
                .build()
        ExecOptions execOptions = ExecOptions.builder()
                .outputFormat(format)
                .build()

        when:
        FixResultLogger.create(true).log(fixResult, execOptions)

        then:
        noExceptionThrown()

        where:
        format << OutputFormat.values()
    }

    def "formatJson"() {
        expect:
        FixResultLogger.create(false).formatJson(null)
        FixResultLogger.create(false).formatJson(FixResult.builder().build())
    }

    def "formatMarkdown - fixes applied (HIGH Verbosity)"() {
        given:
        FixResult fixResult = FixResult.builder()
                .fixesApplied(true)
                .fixCount(1)
                .messages(["file.ext": [FixResult.MessageDescriptor.builder().repositoryPath("file.ext").message("Fixed").build() ]])
                .formattedMessages([ "file.ext :: Fixed" ])
                .build()

        when:
        String markdown =FixResultLogger.create(false).formatMarkdown(fixResult, Verbosity.HIGH)

        then:
        markdown
        markdown == """## Sourcehawk

1 Fix(es) applied without any errors

### Results

* file.ext :: Fixed
"""
    }

    def "formatMarkdown - fixes applied (LOW Verbosity)"() {
        given:
        FixResult fixResult = FixResult.builder()
                .fixesApplied(true)
                .fixCount(1)
                .messages(["file.ext": [FixResult.MessageDescriptor.builder().repositoryPath("file.ext").message("Fixed").build() ]])
                .formattedMessages([ "file.ext :: Fixed" ])
                .build()

        when:
        String markdown =FixResultLogger.create(false).formatMarkdown(fixResult, Verbosity.LOW)

        then:
        markdown
        markdown == """## Sourcehawk

1 Fix(es) applied without any errors"""
    }

    def "formatMarkdown - error (HIGH Verbosity)"() {
        given:
        FixResult fixResult = FixResult.builder()
                .error(true)
                .errorCount(1)
                .messages(["file.ext": [FixResult.MessageDescriptor.builder().repositoryPath("file.ext").message("Error").build() ]])
                .formattedMessages([ "file.ext :: Error" ])
                .build()

        when:
        String markdown =FixResultLogger.create(false).formatMarkdown(fixResult, Verbosity.HIGH)

        then:
        markdown
        markdown == """## Sourcehawk

Fixes unable to be applied. Error(s): 1

### Results

* file.ext :: Error
"""
    }

    def "formatMarkdown - fixes applied - dry run (HIGH Verbosity)"() {
        given:
        FixResult fixResult = FixResult.builder()
                .fixesApplied(true)
                .fixCount(1)
                .messages(["file.ext": [FixResult.MessageDescriptor.builder().repositoryPath("file.ext").message("Fixed").build() ]])
                .formattedMessages([ "file.ext :: Fixed" ])
                .build()

        when:
        String markdown =FixResultLogger.create(true).formatMarkdown(fixResult, Verbosity.HIGH)

        then:
        markdown
        markdown == """## Sourcehawk

1 Fix(es) would have been applied without any errors

### Results

* file.ext :: Fixed
"""
    }

    def "formatMarkdown - fixes applied - dry run (LOW Verbosity)"() {
        given:
        FixResult fixResult = FixResult.builder()
                .fixesApplied(true)
                .fixCount(1)
                .messages(["file.ext": [FixResult.MessageDescriptor.builder().repositoryPath("file.ext").message("Fixed").build() ]])
                .formattedMessages([ "file.ext :: Fixed" ])
                .build()

        when:
        String markdown =FixResultLogger.create(true).formatMarkdown(fixResult, Verbosity.LOW)

        then:
        markdown
        markdown == """## Sourcehawk

1 Fix(es) would have been applied without any errors"""
    }

    def "formatMarkdown - error - dry run (HIGH Verbosity)"() {
        given:
        FixResult fixResult = FixResult.builder()
                .error(true)
                .errorCount(1)
                .messages(["file.ext": [FixResult.MessageDescriptor.builder().repositoryPath("file.ext").message("Error").build() ]])
                .formattedMessages([ "file.ext :: Error" ])
                .build()

        when:
        String markdown =FixResultLogger.create(true).formatMarkdown(fixResult, Verbosity.HIGH)

        then:
        markdown
        markdown == """## Sourcehawk

Fixes would not have been applied. Error(s): 1

### Results

* file.ext :: Error
"""
    }
    
}
