package com.optum.sourcehawk.enforcer.file.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.optum.sourcehawk.enforcer.EnforcerResult
import com.optum.sourcehawk.enforcer.ResolverResult
import org.spockframework.util.IoUtil
import spock.lang.Specification
import spock.lang.Unroll

import java.util.function.Function

class JsonPathEqualsSpec extends Specification {

    def "equals"() {
        expect:
        JsonPathEquals.equals('$.key', 'value')
    }

    def "enforce - null input stream"() {
        when:
        JsonPathEquals.equals('$.foo', "bar").enforceInternal(null)

        then:
        thrown(NullPointerException)
    }

    @Unroll
    def "enforce - #query = #expectedValue (passed)"() {
        given:
        JsonPathEquals jsonPathEquals = JsonPathEquals.equals(query, expectedValue)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/bicycle.json')

        when:
        EnforcerResult result = jsonPathEquals.enforce(fileInputStream)

        then:
        result
        result.passed
        !result.messages

        where:
        query                   | expectedValue
        '$.make'                | 'Raleigh'
        '$.size.value'          | 60
        '$.components[0]'       | 'handlebars'
        '$.components.length()' | 6
    }

    def "enforce - map (passed)"() {
        given:
        def map = [
                '$.make'               : 'Raleigh',
                '$.size.value'         : 60,
                '$.components[0]'      : 'handlebars',
                '$.components.length()': 6
        ]
        JsonPathEquals jsonPathEquals = JsonPathEquals.equals(map)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/bicycle.json')

        when:
        EnforcerResult result = jsonPathEquals.enforce(fileInputStream)

        then:
        result
        result.passed
        !result.messages
    }

    @Unroll
    def "enforce - #query = #expectedValue (failed - incorrect value)"() {
        given:
        JsonPathEquals jsonPathEquals = JsonPathEquals.equals(query, expectedValue)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/bicycle.json')

        when:
        EnforcerResult result = jsonPathEquals.enforce(fileInputStream)

        then:
        result
        !result.passed
        result.messages
        result.messages[0] == "Execution of query [$query] yielded result [$actualValue] which is not equal to [$expectedValue]"

        where:
        query                   | actualValue  | expectedValue
        '$.make'                | 'Raleigh'    | 'Schwinn'
        '$.size.value'          | 60           | 58
        '$.components[0]'       | 'handlebars' | 'brakes'
        '$.components.length()' | 6            | 2
    }

    @Unroll
    def "enforce - #query = #expectedValue (failed - missing)"() {
        given:
        JsonPathEquals jsonPathEquals = JsonPathEquals.equals(query, expectedValue)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/bicycle.json')

        when:
        EnforcerResult result = jsonPathEquals.enforce(fileInputStream)

        then:
        result
        !result.passed
        result.messages
        result.messages[0] == "Execution of query [$query] yielded no result"

        where:
        query             | expectedValue
        '$.class'         | 'road'
        '$.components[8]' | 'calipers'
    }

    @Unroll
    def "enforce - #query = #expectedValue (failed - query error)"() {
        given:
        JsonPathEquals jsonPathEquals = JsonPathEquals.equals(query, expectedValue)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/bicycle.json')

        when:
        EnforcerResult result = jsonPathEquals.enforce(fileInputStream)

        then:
        result
        !result.passed
        result.messages
        result.messages[0].startsWith("Execution of query [$query] yielded error")

        where:
        query | expectedValue
        '$$'  | 'road'
    }

    @Unroll
    def "enforce - #query = #expectedValue (failed - null parse)"() {
        given:
        JsonPathEquals jsonPathEquals = JsonPathEquals.equals(query, expectedValue)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/bicycle-bad.json')

        when:
        EnforcerResult result = jsonPathEquals.enforce(fileInputStream)

        then:
        result
        !result.passed
        result.messages
        result.messages[0].startsWith("Query parsing resulted in error [net.minidev.json.parser.ParseException: Unexpected character (:) at position 74.")

        where:
        query | expectedValue
        '$$'  | 'road'
    }

    @Unroll
    def "resolve - no updates required"() {
        given:
        JsonPathEquals jsonPathEquals = JsonPathEquals.equals(query, expectedValue)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/bicycle.json')
        StringWriter stringWriter = new StringWriter()

        when:
        ResolverResult result = jsonPathEquals.resolve(fileInputStream, stringWriter)

        then:
        result
        !result.updatesApplied
        result.fixCount == 0
        !result.error
        result.errorCount == 0
        !result.messages

        and:
        !stringWriter.toString()

        where:
        query                   | expectedValue
        '$.make'                | 'Raleigh'
        '$.size.value'          | 60
        '$.components[0]'       | 'handlebars'
        '$.components.length()' | 6
    }

    @Unroll
    def "resolve - updates applied (query found)"() {
        given:
        JsonPathEquals jsonPathEquals = JsonPathEquals.equals(query, expectedValue)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/bicycle.json')
        StringWriter stringWriter = new StringWriter()

        when:
        ResolverResult result = jsonPathEquals.resolve(fileInputStream, stringWriter)

        then:
        result
        result.updatesApplied
        result.fixCount == 1
        !result.error
        result.errorCount == 0
        result.messages
        result.messages.size() == 1
        result.messages[0] == "Query [$query] has been updated with value [$expectedValue]"

        and:
        stringWriter.toString()

        where:
        query                   | expectedValue
        '$.make'                | 'Cinelli'
        '$.size.value'          | 61
        '$.components[0]'       | 'stem'
        '$.components.length()' | 5
    }

    @Unroll
    def "resolve - updates applied (query not found)"() {
        given:
        JsonPathEquals jsonPathEquals = JsonPathEquals.equals(query, expectedValue)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/bicycle.json')
        StringWriter stringWriter = new StringWriter()

        when:
        ResolverResult result = jsonPathEquals.resolve(fileInputStream, stringWriter)

        then:
        result
        result.updatesApplied
        result.fixCount == 1
        !result.error
        result.errorCount == 0
        result.messages
        result.messages.size() == 1
        result.messages[0] == "Query [$query] which was missing, has been updated with value [$expectedValue]"

        when:
        Map jsonObject = new ObjectMapper().readValue(stringWriter.toString(), Map)

        then:
        expectedJsonAssertion.apply(jsonObject)

        where:
        query                   | expectedValue | expectedJsonAssertion
        '$.rating'              | 98            | { json -> json.rating == 98 } as Function<Map, Boolean>
        '$.notes[0]'            | 'Note 1'      | { json -> json.notes[0] == "Note 1" } as Function<Map, Boolean>
        '$.child.notes[0]'      | 'Child Note'  | { json -> json['child.notes'][0] == "Child Note" } as Function<Map, Boolean>
    }

    def "resolve - error"() {
        given:
        String query = '$$'
        JsonPathEquals jsonPathEquals = JsonPathEquals.equals(query, "doesn't matter")
        InputStream fileInputStream = IoUtil.getResourceAsStream('/bicycle.json')
        StringWriter stringWriter = new StringWriter()

        when:
        ResolverResult result = jsonPathEquals.resolve(fileInputStream, stringWriter)

        then:
        result
        !result.updatesApplied
        result.fixCount == 0
        result.error
        result.errorCount == 1
        result.messages
        result.messages.size() == 1
        result.messages[0].startsWith("Execution of query [$query] yielded error")

        and:
        !stringWriter.toString()
    }

    def "resolve - closed input stream"() {
        given:
        InputStream inputStream = IoUtil.getResourceAsStream('/bicycle.json')
        inputStream.close()

        when:
        ResolverResult resolverResult = JsonPathEquals.equals('$.key', "doesn't matter").resolve(inputStream, new StringWriter())

        then:
        resolverResult
        !resolverResult.updatesApplied
        resolverResult.fixCount == 0
        resolverResult.error
        resolverResult.errorCount == 1
        resolverResult.messages
        resolverResult.messages.size() == 1
        resolverResult.messages[0] == "Query parsing resulted in error [net.minidev.json.parser.ParseException: Unexpected exception java.io.IOException: Stream closed occur at position -1.]"
    }

    def "resolve - null input stream or writer"() {
        when:
        JsonPathEquals.equals('$.key', "doesn't matter").resolve(null, new StringWriter())

        then:
        thrown(NullPointerException)

        when:
        JsonPathEquals.equals('$.key', "doesn't matter").resolve( IoUtil.getResourceAsStream('/bicycle.json'), null)

        then:
        thrown(NullPointerException)
    }

}
