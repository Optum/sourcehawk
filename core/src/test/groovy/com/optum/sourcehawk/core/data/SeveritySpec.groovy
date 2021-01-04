package com.optum.sourcehawk.core.data

import com.optum.sourcehawk.core.data.Severity
import spock.lang.Specification
import spock.lang.Unroll

class SeveritySpec extends Specification {

    @Unroll
    def "parse - valid"() {
        expect:
        Severity.parse(name)

        where:
        name << ['info', 'INFO', 'warning', 'WARNING', 'error', 'ERROR']
    }

    @Unroll
    def "parse - invalid - returns default"() {
        expect:
        Severity.parse(name) == Severity.ERROR

        where:
        name << ['', ' ', null, 'information', 'WR', 'err', 'no', 'warn']
    }

}
