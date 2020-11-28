package com.optum.sourcehawk.enforcer


import spock.lang.Specification

class ResolverResultSpec extends Specification {

    def "creators"() {
        expect:
        !ResolverResult.NO_UPDATES.updatesApplied
        ResolverResult.updatesApplied("Yep, been updated alright").updatesApplied
        ResolverResult.builder().updatesApplied(true).fixCount(1).error(false).errorCount(0).messages(["Done!"]).build()
        ResolverResult.error("ERROR")
    }

    def "builder"() {
        given:
        ResolverResult resolverResult = ResolverResult.builder()
                .updatesApplied(true)
                .fixCount(1)
                .error(false)
                .errorCount(0)
                .messages(["Done!"])
                .build()

        expect:
        resolverResult
        resolverResult.updatesApplied
        resolverResult.fixCount == 1
        !resolverResult.error
        resolverResult.errorCount == 0
        resolverResult.messages
        resolverResult.messages.size() == 1
        resolverResult.messages[0] == "Done!"
    }

    def "reduce - one updated applied and one no updated applied"() {
        given:
        ResolverResult one = ResolverResult.updatesApplied("Updates made")
        ResolverResult two = ResolverResult.NO_UPDATES

        when:
        ResolverResult resolverResult = ResolverResult.reduce(one, two)

        then:
        resolverResult
        resolverResult.updatesApplied
        resolverResult.fixCount == 1
        !resolverResult.error
        resolverResult.errorCount == 0
        resolverResult.messages
        resolverResult.messages.size() == 1
        resolverResult.messages[0] == one.messages[0]
    }

    def "reduce - both updates applied"() {
        given:
        ResolverResult one = ResolverResult.updatesApplied("Updates made")
        ResolverResult two = ResolverResult.updatesApplied("More updates made")

        when:
        ResolverResult resolverResult = ResolverResult.reduce(one, two)

        then:
        resolverResult
        resolverResult.updatesApplied
        resolverResult.fixCount == 2
        !resolverResult.error
        resolverResult.errorCount == 0
        resolverResult.messages
        resolverResult.messages.size() == 2
        resolverResult.messages[0] == one.messages[0]
        resolverResult.messages[1] == two.messages[0]
    }

    def "reduce - one updates applied and one error"() {
        given:
        ResolverResult one = ResolverResult.updatesApplied("Updates made")
        ResolverResult two = ResolverResult.error("BOOM")

        when:
        ResolverResult resolverResult = ResolverResult.reduce(one, two)

        then:
        resolverResult
        resolverResult.updatesApplied
        resolverResult.fixCount == 1
        !resolverResult.error
        resolverResult.errorCount == 1
        resolverResult.messages
        resolverResult.messages.size() == 2
        resolverResult.messages[0] == one.messages[0]
        resolverResult.messages[1] == two.messages[0]
    }

    def "reduce - no updates applied and one error"() {
        given:
        ResolverResult one = ResolverResult.NO_UPDATES
        ResolverResult two = ResolverResult.error("BOOM")

        when:
        ResolverResult resolverResult = ResolverResult.reduce(one, two)

        then:
        resolverResult
        !resolverResult.updatesApplied
        resolverResult.fixCount == 0
        !resolverResult.error
        resolverResult.errorCount == 1
        resolverResult.messages
        resolverResult.messages.size() == 1
        resolverResult.messages[0] == two.messages[0]
    }

    def "reduce - both error"() {
        given:
        ResolverResult one = ResolverResult.error("BOOM1")
        ResolverResult two = ResolverResult.error("BOOM2")

        when:
        ResolverResult resolverResult = ResolverResult.reduce(one, two)

        then:
        resolverResult
        !resolverResult.updatesApplied
        resolverResult.fixCount == 0
        resolverResult.error
        resolverResult.errorCount == 2
        resolverResult.messages
        resolverResult.messages.size() == 2
        resolverResult.messages[0] == two.messages[0]
        resolverResult.messages[1] == one.messages[0]
    }

    def "reduce - neither updates applied"() {
        given:
        ResolverResult one = ResolverResult.NO_UPDATES
        ResolverResult two = ResolverResult.NO_UPDATES

        when:
        ResolverResult resolverResult = ResolverResult.reduce(one, two)

        then:
        resolverResult
        !resolverResult.updatesApplied
        resolverResult.fixCount == 0
        !resolverResult.error
        resolverResult.errorCount == 0
        !resolverResult.messages
    }

    def "reduce - updated applied (duplicate messages)"() {
        given:
        ResolverResult one = ResolverResult.updatesApplied("Updates made")
        ResolverResult two = ResolverResult.updatesApplied("Updates made")

        when:
        ResolverResult resolverResult = ResolverResult.reduce(one, two)

        then:
        resolverResult
        resolverResult.updatesApplied
        resolverResult.fixCount == 1
        !resolverResult.error
        resolverResult.errorCount == 0
        resolverResult.messages
        resolverResult.messages.size() == 1
        resolverResult.messages[0] == one.messages[0]
    }

}
