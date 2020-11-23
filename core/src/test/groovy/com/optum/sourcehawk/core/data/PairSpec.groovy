package com.optum.sourcehawk.core.data

import spock.lang.Specification

class PairSpec extends Specification {

    def "of - same types"() {
        given:
        String left = "left"
        String right = "right"

        when:
        Pair<String, String> pair = Pair.of(left, right)

        then:
        pair
        pair.getLeft() == left
        pair.getRight() ==  right
    }

    def "of - different types"() {
        given:
        String left = "number"
        int right = 20

        when:
        Pair<String, Integer> pair = Pair.of(left, right)

        then:
        pair
        pair.getLeft() == left
        pair.getRight() == right
    }

    def "of - null key/value"() {
        when:
        Pair.of(null, "world")

        then:
        thrown(NullPointerException)

        when:
        Pair.of("hello", null)

        then:
        thrown(NullPointerException)
    }

}
