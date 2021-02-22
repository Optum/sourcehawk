package com.optum.sourcehawk.core.data

import com.optum.sourcehawk.core.data.OutputFormat
import spock.lang.Specification
import spock.lang.Unroll

class OutputFormatSpec extends Specification {

    @Unroll
    def "parse - valid"() {
        expect:
        OutputFormat.parse(name)

        where:
        name << ['text', 'TEXT', 'json', 'JSON', 'markdown', 'MARKDOWN']
    }

    @Unroll
    def "parse - invalid - returns default"() {
        expect:
        OutputFormat.parse(name) == OutputFormat.TEXT

        where:
        name << ['', ' ', null, 'con', 'txt', 'js', 'md']
    }
    
}
