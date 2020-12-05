package com.optum.sourcehawk.exec

import spock.lang.Specification

class ExecLoggersSpec extends Specification {

    def "constructor"() {
        when:
        new ExecLoggers()

        then:
        thrown(UnsupportedOperationException)
    }

    def "loggers"() {
        expect:
        ExecLoggers.CONSOLE_RAW
        ExecLoggers.HIGHLIGHT
        ExecLoggers.MESSAGE
        ExecLoggers.MESSAGE_ANSI
    }

}
