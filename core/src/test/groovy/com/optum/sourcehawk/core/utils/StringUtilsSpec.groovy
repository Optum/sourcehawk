package com.optum.sourcehawk.core.utils


import org.codehaus.groovy.runtime.GStringImpl
import spock.lang.Specification
import spock.lang.Unroll

class StringUtilsSpec extends Specification {

    @Unroll
    def "isBlankOrEmpty - true"() {
        expect:
        StringUtils.isBlankOrEmpty(value)

        where:
        value << ["", " ", "\n", "\r\n", "\n ", " \r", null]
    }

    @Unroll
    def "isBlankOrEmpty - false"() {
        expect:
        !StringUtils.isBlankOrEmpty(value)

        where:
        value << ["a", " a", "a ", "a\r\n", "\na", "null"]
    }

    @Unroll
    def "isNotBlankOrEmpty - false"() {
        expect:
        !StringUtils.isNotBlankOrEmpty(value)

        where:
        value << ["", " ", "\n", "\r\n", "\n ", " \r", null]
    }

    @Unroll
    def "isNotBlankOrEmpty - true"() {
        expect:
        StringUtils.isNotBlankOrEmpty(value)

        where:
        value << ["a", " a", "a ", "a\r\n", "\na", "null"]
    }

    def "startsWith - true"() {
        expect:
        StringUtils.startsWith("Hello World", "Hello")
        StringUtils.startsWith("123456", "123")
        StringUtils.startsWith("a", "a")
    }

    def "startsWith - false"() {
        expect:
        !StringUtils.startsWith("Hello World", "World")
        !StringUtils.startsWith("123456", "456")
        !StringUtils.startsWith("a", "b")
        !StringUtils.startsWith(null, "123")
        !StringUtils.startsWith("", "123")
        !StringUtils.startsWith("abc", null)
        !StringUtils.startsWith("abc", "")
    }

    def "equals - equal (same object)"() {
        given:
        String string = "hello world"

        expect:
        StringUtils.equals(string, string)
    }

    @Unroll
    def "equals - equal"() {
        expect:
        StringUtils.equals(one, two)

        where:
        one                                                           | two
        null                                                          | null
        ""                                                            | ""
        " "                                                           | " "
        "abc"                                                         | "abc"
        new GStringImpl(new Object[]{"hello"}, new String[]{"hello"}) | new GStringImpl(new Object[]{"hello"}, new String[]{"hello"})
        "hellohello"                                                  | new GStringImpl(new Object[]{"hello"}, new String[]{"hello"})
        new GStringImpl(new Object[]{"hello"}, new String[]{"hello"}) | "hellohello"
    }

    @Unroll
    def "equals - not equal"() {
        expect:
        !StringUtils.equals(one, two)

        where:
        one                                                           | two
        null                                                          | ""
        null                                                          | "null"
        "null"                                                        | null
        "abc"                                                         | " abc"
        "123"                                                         | "456"
        " "                                                           | "  "
        ""                                                            | " "
        new GStringImpl(new Object[]{"hello"}, new String[]{"hello"}) | new GStringImpl(new Object[]{"world"}, new String[]{"world"})
    }

    @Unroll
    def "removeNewLines - empty"() {
        expect:
        StringUtils.removeNewLines(string) == string

        where:
        string << [null, "", " "]
    }

    @Unroll
    def "defaultString #string or #defaultStr is #expected"() {
        expect:
        StringUtils.defaultString(string, defaultStr) == expected

        where:
        string | defaultStr || expected
        null   | "test"     || "test"
        ""     | "test"     || ""
        " "    | "test"     || " "
        null   | null       || null
        ""     | null       || ""
        " "    | null       || " "
    }

    def "removeNewLines - one character only -either carriage return or new line"() {
        expect:
        StringUtils.removeNewLines('\r') == ""
        StringUtils.removeNewLines('\n') == ""
    }

    @Unroll
    def "removeNewLines - removed"() {
        expect:
        StringUtils.removeNewLines(input) == output

        where:
        input             | output
        "Hello world"     | "Hello world"
        "Hello world\n"   | "Hello world"
        "Hello world\r"   | "Hello world"
        "Hello world\r\n" | "Hello world"
    }

    @Unroll
    def "isUrl - blank/empty - false"() {
        expect:
        !StringUtils.isUrl(url)

        where:
        url << [null, "", " "]
    }

    @Unroll
    def "isUrl - #url - true"() {
        expect:
        StringUtils.isUrl(url)

        where:
        url << [
                "http://example.com",
                "https://example.com"
        ]
    }

    @Unroll
    def "isUrl - #url - false"() {
        expect:
        !StringUtils.isUrl(url)

        where:
        url << [
                "/path/to/file",
                "relative/path/to/file"
        ]
    }

}
