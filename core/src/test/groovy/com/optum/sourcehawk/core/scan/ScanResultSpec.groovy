package com.optum.sourcehawk.core.scan

import spock.lang.Specification

class ScanResultSpec extends Specification {

    def "builder"() {
        given:
        ScanResult.ScanResultBuilder scanResultBuilder = ScanResult.builder()
                .passed(false)
                .warningCount(1)
                .errorCount(1)
                .messages(Collections.singletonMap("file", Collections.singleton(new ScanResult.MessageDescriptor("WARN", "file", "message"))))
                .formattedMessages(Collections.singleton("bad"))

        when:
        ScanResult scanResult = scanResultBuilder.build()

        then:
        scanResult
        scanResult.toString()
        scanResult.hashCode() == ScanResult.builder()
                .passed(false)
                .warningCount(1)
                .errorCount(1)
                .messages(Collections.singletonMap("file", Collections.singleton(new ScanResult.MessageDescriptor("WARN", "file", "message"))))
                .formattedMessages(Collections.singleton("bad"))
                .build()
                .hashCode()
    }

    def "isPassedWithNoWarnings"() {
        given:
        ScanResult scanResult = ScanResult.builder()
                .passed(true)
                .warningCount(1)
                .errorCount(0)
                .messages(Collections.singletonMap("file", Collections.singleton(new ScanResult.MessageDescriptor("WARN", "file", "message"))))
                .formattedMessages(Collections.singleton("bad"))
                .build()

        when:
        boolean passedWithNoWarnings = scanResult.isPassedWithNoWarnings()

        then:
        !passedWithNoWarnings

        when:
        passedWithNoWarnings = ScanResult.passed().isPassedWithNoWarnings()

        then:
        passedWithNoWarnings
    }

    def "passed"() {
        when:
        ScanResult scanResult = ScanResult.passed()

        then:
        scanResult
        scanResult.passed
    }

    def "failed - no warnings"() {
        when:
        ScanResult scanResult = ScanResult.builder().build()

        then:
        scanResult
        !scanResult.passedWithNoWarnings
    }

    def "reduce - passed > failed"() {
        given:
        ScanResult one = ScanResult.passed()
        ScanResult two = ScanResult.builder()
                .passed(false)
                .warningCount(1)
                .errorCount(1)
                .messages(Collections.singletonMap("file", Collections.singleton(new ScanResult.MessageDescriptor("WARN", "file", "message"))))
                .formattedMessages(Collections.singleton("bad"))
                .build()

        when:
        ScanResult scanResult = ScanResult.reduce(one, two)

        then:
        scanResult
        !scanResult.passed
        scanResult.warningCount == 1
        scanResult.errorCount == 1
        scanResult.messages.size() == 1
        scanResult.formattedMessages.size() == 1
    }

    def "reduce - passed > passed"() {
        given:
        ScanResult one = ScanResult.passed()
        ScanResult two = ScanResult.passed()

        when:
        ScanResult scanResult = ScanResult.reduce(one, two)

        then:
        scanResult
        scanResult.passed
    }

    def "reduce - failed > failed"() {
        given:
        ScanResult one = ScanResult.builder()
                .passed(false)
                .warningCount(1)
                .errorCount(1)
                .messages(Collections.singletonMap("file", Collections.singleton(new ScanResult.MessageDescriptor("WARN", "file", "message"))))
                .formattedMessages(Collections.singleton("bad"))
                .build()
        ScanResult two = ScanResult.builder()
                .passed(false)
                .warningCount(1)
                .errorCount(2)
                .messages(Collections.singletonMap("file2", Collections.singleton(new ScanResult.MessageDescriptor("WARN", "file", "message"))))
                .formattedMessages(Collections.singleton("bad2"))
                .build()

        when:
        ScanResult scanResult = ScanResult.reduce(one, two)

        then:
        scanResult
        !scanResult.passed
        scanResult.warningCount == 2
        scanResult.errorCount == 3
        scanResult.messages.size() == 2
        scanResult.formattedMessages.size() == 2
    }

    def "reduce - failed > failed (merged messages)"() {
        given:
        ScanResult one = ScanResult.builder()
                .passed(false)
                .warningCount(1)
                .errorCount(1)
                .messages(Collections.singletonMap("file", Collections.singleton(new ScanResult.MessageDescriptor("WARN", "file", "message"))))
                .formattedMessages(Collections.singleton("bad"))
                .build()
        ScanResult two = ScanResult.builder()
                .passed(false)
                .warningCount(1)
                .errorCount(2)
                .messages(Collections.singletonMap("file", Collections.singleton(new ScanResult.MessageDescriptor("WARN", "file", "message"))))
                .formattedMessages(Collections.singleton("bad"))
                .build()

        when:
        ScanResult scanResult = ScanResult.reduce(one, two)

        then:
        scanResult
        !scanResult.passed
        scanResult.warningCount == 2
        scanResult.errorCount == 3
        scanResult.messages.size() == 1
        scanResult.formattedMessages.size() == 1
    }

}
