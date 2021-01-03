package com.optum.sourcehawk.core.data

import com.optum.sourcehawk.core.data.Verbosity
import spock.lang.Specification
import spock.lang.Unroll

class VerbositySpec extends Specification {

    @Unroll
    def "parse - valid"() {
        expect:
        Verbosity.parse(name)

        where:
        name << ['zero', 'l', 'med', 'high', 'h', 'L', 'M', 'medium', '0', 'hi', 'LO']
    }

    @Unroll
    def "parse - invalid - returns default"() {
        expect:
        Verbosity.parse(name) == Verbosity.HIGH

        where:
        name << ['', ' ', null, 'HGH', '00', 'A', 'media', 'L2', 'loo']
    }

}
