package com.optum.sourcehawk.cli

import spock.lang.Specification

class CommandOptionsSpec extends Specification {

    def "private constructor"() {
        expect:
        new CommandOptions()
    }

}
