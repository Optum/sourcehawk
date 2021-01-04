package com.optum.sourcehawk.exec.config

import com.optum.sourcehawk.core.result.FlattenConfigResult
import com.optum.sourcehawk.exec.FileBaseSpecification

class FlattenConfigLoggerSpec extends FileBaseSpecification {

    def "handleDryRunOutput"() {
        when:
        FlattenConfigResultLogger.handleConsoleOutput(null)

        then:
        notThrown(Exception)

        when:
        FlattenConfigResultLogger.handleConsoleOutput(FlattenConfigResult.success(null))

        then:
        notThrown(Exception)

        when:
        FlattenConfigResultLogger.handleConsoleOutput(FlattenConfigResult.error(null))

        then:
        notThrown(Exception)

        when:
        FlattenConfigResultLogger.handleConsoleOutput(FlattenConfigResult.error("test"))

        then:
        notThrown(Exception)

        when:
        FlattenConfigResultLogger.handleConsoleOutput(FlattenConfigResult.builder().error(true).content("hello".bytes).build())

        then:
        notThrown(Exception)
    }

    def "handleFileSystemOutput"() {
        when:
        FlattenConfigResultLogger.handleFileSystemOutput(null, null)

        then:
        notThrown(Exception)
    }
}
