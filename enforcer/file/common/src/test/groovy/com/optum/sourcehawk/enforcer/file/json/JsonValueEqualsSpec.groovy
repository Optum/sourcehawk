package com.optum.sourcehawk.enforcer.file.json


import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.optum.sourcehawk.enforcer.EnforcerResult
import com.optum.sourcehawk.enforcer.ResolverResult
import org.spockframework.util.IoUtil
import spock.lang.Specification
import spock.lang.Unroll

import java.util.function.Function

class JsonValueEqualsSpec extends Specification {

    def "equals"() {
        expect:
        JsonValueEquals.equals('/key', 'value')
    }

    def "enforce - null input stream"() {
        when:
        JsonValueEquals.equals('/foo', "bar").enforceInternal(null)

        then:
        thrown(NullPointerException)
    }

    @Unroll
    def "enforce - #pointerExpression = #expectedValue (passed)"() {
        given:
        JsonValueEquals jsonPathEquals = JsonValueEquals.equals(pointerExpression, expectedValue)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/bicycle.json')

        when:
        EnforcerResult result = jsonPathEquals.enforce(fileInputStream)

        then:
        result
        result.passed
        !result.messages

        where:
        pointerExpression | expectedValue
        '/make'           | 'Raleigh'
        '/size/value'     | 60
        '/components/0'   | 'handlebars'
    }

    def "enforce - map (passed)"() {
        given:
        def map = [
                '/make'               : 'Raleigh',
                '/size/value'         : 60,
                '/components/0'      : 'handlebars'
        ]
        JsonValueEquals jsonPathEquals = JsonValueEquals.equals(map)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/bicycle.json')

        when:
        EnforcerResult result = jsonPathEquals.enforce(fileInputStream)

        then:
        result
        result.passed
        !result.messages
    }

    @Unroll
    def "enforce - #pointerExpression = #expectedValue (failed - incorrect value)"() {
        given:
        JsonValueEquals jsonPathEquals = JsonValueEquals.equals(pointerExpression, expectedValue)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/bicycle.json')

        when:
        EnforcerResult result = jsonPathEquals.enforce(fileInputStream)

        then:
        result
        !result.passed
        result.messages
        result.messages[0] == "Execution of pointer expression [$pointerExpression] yielded result [$actualValue] which is not equal to [$expectedValue]"

        where:
        pointerExpression | actualValue  | expectedValue
        '/make'           | 'Raleigh'    | 'Schwinn'
        '/size/value'     | 60           | 58
        '/components/0'   | 'handlebars' | 'brakes'
    }

    @Unroll
    def "enforce - #pointerExpression = #expectedValue (failed - missing)"() {
        given:
        JsonValueEquals jsonPathEquals = JsonValueEquals.equals(pointerExpression, expectedValue)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/bicycle.json')

        when:
        EnforcerResult result = jsonPathEquals.enforce(fileInputStream)

        then:
        result
        !result.passed
        result.messages
        result.messages[0] == "Execution of pointer expression [$pointerExpression] yielded no result"

        where:
        pointerExpression | expectedValue
        '/class'          | 'road'
        '/components/8'   | 'calipers'
    }

    @Unroll
    def "enforce - #pointerExpression = #expectedValue (failed - pointer expression error)"() {
        given:
        JsonValueEquals jsonPathEquals = JsonValueEquals.equals(pointerExpression, expectedValue)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/bicycle.json')

        when:
        EnforcerResult result = jsonPathEquals.enforce(fileInputStream)

        then:
        result
        !result.passed
        result.messages
        result.messages[0].startsWith("Execution of pointer expression [$pointerExpression] yielded error")

        where:
        pointerExpression | expectedValue
        '*'  | 'road'
    }

    @Unroll
    def "enforce - #pointerExpression = #expectedValue (failed - null parse)"() {
        given:
        JsonValueEquals jsonPathEquals = JsonValueEquals.equals(pointerExpression, expectedValue)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/bicycle-bad.json')

        when:
        EnforcerResult result = jsonPathEquals.enforce(fileInputStream)

        then:
        result
        !result.passed
        result.messages
        result.messages[0].startsWith("Reading or parsing file resulted in error")

        where:
        pointerExpression | expectedValue
        '//'              | 'road'
    }

    @Unroll
    def "resolve - no updates required"() {
        given:
        JsonValueEquals jsonPathEquals = JsonValueEquals.equals(pointerExpression, expectedValue)
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
        pointerExpression | expectedValue
        '/make'           | 'Raleigh'
        '/size/value'     | 60
        '/components/0'   | 'handlebars'
    }

    @Unroll
    def "resolve - updates applied (pointer expression found)"() {
        given:
        JsonValueEquals jsonPathEquals = JsonValueEquals.equals(pointerExpression, expectedValue)
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
        result.messages[0] == "Pointer expression [$pointerExpression] has been updated with value [$expectedValue]"

        and:
        stringWriter.toString()

        where:
        pointerExpression | expectedValue
        '/make'           | 'Cinelli'
        '/size/value'     | 61
        '/components/0'   | 'stem'
    }

    @Unroll
    def "resolve - updates applied (pointer expression not found)"() {
        given:
        JsonValueEquals jsonPathEquals = JsonValueEquals.equals(pointerExpression, expectedValue)
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
        result.messages[0] == "Pointer expression [$pointerExpression] which was missing, has been set with value [$expectedValue]"

        when:
        Map jsonObject = new ObjectMapper().readValue(stringWriter.toString(), Map)

        then:
        expectedJsonAssertion.apply(jsonObject)

        where:
        pointerExpression | expectedValue | expectedJsonAssertion
        '/rating'         | 98            | { json -> json.rating == 98 } as Function<Map, Boolean>
//        '/notes/0'        | 'Note 1'      | { json -> json.notes[0] == "Note 1" } as Function<Map, Boolean> (array addition not yet supported)
//        '/child/notes/0'  | 'Child Note'  | { json -> json['child.notes'][0] == "Child Note" } as Function<Map, Boolean> (array addition not yet supported)
    }

    def "resolve - error"() {
        given:
        String pointerExpression = '$$'
        JsonValueEquals jsonPathEquals = JsonValueEquals.equals(pointerExpression, "doesn't matter")
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
        result.messages[0].startsWith("Execution of pointer expression [$pointerExpression] yielded error")

        and:
        !stringWriter.toString()
    }

    def "resolve - closed input stream"() {
        given:
        InputStream inputStream = IoUtil.getResourceAsStream('/bicycle.json')
        inputStream.close()

        when:
        ResolverResult resolverResult = JsonValueEquals.equals('/key', "doesn't matter").resolve(inputStream, new StringWriter())

        then:
        resolverResult
        !resolverResult.updatesApplied
        resolverResult.fixCount == 0
        resolverResult.error
        resolverResult.errorCount == 1
        resolverResult.messages
        resolverResult.messages.size() == 1
        resolverResult.messages[0].startsWith("Reading or parsing file resulted in error")
    }

    def "resolve - null input stream or writer"() {
        when:
        JsonValueEquals.equals('/key', "doesn't matter").resolve(null, new StringWriter())

        then:
        thrown(NullPointerException)

        when:
        JsonValueEquals.equals('/key', "doesn't matter").resolve( IoUtil.getResourceAsStream('/bicycle.json'), null)

        then:
        thrown(NullPointerException)
    }

    @Unroll
    def "updateObjectNodeValue - #type"() {
        given:
        ObjectNode parentObjectNode = JsonNodeFactory.instance.objectNode()
        String childNodeName = "child"

        when:
        JsonValueEquals.updateObjectNodeValue(parentObjectNode, childNodeName, expectedValue)

        then:
        noExceptionThrown()

        where:
        type       | expectedValue
        String     | "hello"
        Boolean    | true
        Integer    | (int) 34
        Long       | (long) 28
        Double     | (double) 20.4
        Float      | (float) 100.7345
        Short      | (short) 1
        BigInteger | BigInteger.ONE
        BigDecimal | BigDecimal.ZERO
    }

    @Unroll
    def "updateArrayNodeValue - #type (missing = false)"() {
        given:
        ArrayNode parentArrayNode = JsonNodeFactory.instance.arrayNode(10)
        parentArrayNode.add(expectedValue)
        parentArrayNode.add(expectedValue)
        parentArrayNode.add(expectedValue)
        parentArrayNode.add(expectedValue)
        parentArrayNode.add(expectedValue)
        parentArrayNode.add(expectedValue)
        int childNodeIndex = 3

        when:
        JsonValueEquals.updateArrayNodeValue(parentArrayNode, childNodeIndex, expectedValue, false)

        then:
        noExceptionThrown()

        where:
        type       | expectedValue
        String     | "hello"
        Boolean    | true
        Integer    | (int) 34
        Long       | (long) 28
        Short      | (short) 1
        BigInteger | BigInteger.ONE
        BigDecimal | BigDecimal.ZERO
    }

    @Unroll
    def "updateArrayNodeValue - #type (missing = true)"() {
        given:
        ArrayNode parentArrayNode = JsonNodeFactory.instance.arrayNode(10)
        int childNodeIndex = 3

        when:
        JsonValueEquals.updateArrayNodeValue(parentArrayNode, childNodeIndex, expectedValue, true)

        then:
        noExceptionThrown()

        where:
        type       | expectedValue
        String     | "hello"
        Boolean    | true
        Integer    | (int) 34
        Long       | (long) 28
        Short      | (short) 1
        BigInteger | BigInteger.ONE
        BigDecimal | BigDecimal.ZERO
    }

}
