package com.optum.sourcehawk.core.utils

import spock.lang.Specification

class TrySpec extends Specification {

    def "private constructor"() {
        when:
        new Try()

        then:
        thrown(UnsupportedOperationException)
    }

    def "attemptOrDefault - success"() {
        when:
        String hello = Try.attemptOrDefault({ "world" }, { it.getMessage() })

        then:
        hello == "world"
    }

    def "attemptOrDefault - exception"() {
        when:
        String hello = Try.attemptOrDefault({ throw new IllegalStateException("BOOM") }, { it.getMessage() })

        then:
        hello == "BOOM"
    }

}
