package com.optum.sourcehawk.exec

import com.optum.sourcehawk.core.scan.FlattenResult
import com.optum.sourcehawk.core.scan.OutputFormat
import com.optum.sourcehawk.core.scan.Verbosity
import spock.lang.Specification
import spock.lang.Unroll

class FlattenResultLoggerSpec extends Specification {

    @Unroll
    def "log - #format (passed)"(OutputFormat format) {
        given:
        FlattenResult flattenResult = FlattenResult.success("hello".bytes)
        ExecOptions execOptions = ExecOptions.builder()
                .outputFormat(format)
                .build()

        when:
        FlattenResultLogger.log(flattenResult, execOptions, "target", false)

        then:
        noExceptionThrown()

        where:
        format << OutputFormat.values()
    }

    @Unroll
    def "log - #verbosity (passed)"(Verbosity verbosity) {
        given:
        FlattenResult flattenResult = FlattenResult.success("hello".bytes)
        ExecOptions execOptions = ExecOptions.builder()
                .verbosity(verbosity)
                .build()

        when:
        FlattenResultLogger.log(flattenResult, execOptions, "target", false)

        then:
        noExceptionThrown()

        where:
        verbosity << Verbosity.values()
    }
}
