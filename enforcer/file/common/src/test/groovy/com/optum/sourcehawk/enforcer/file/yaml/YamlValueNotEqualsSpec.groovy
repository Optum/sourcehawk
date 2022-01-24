package com.optum.sourcehawk.enforcer.file.yaml


import com.optum.sourcehawk.enforcer.EnforcerResult
import org.spockframework.util.IoUtil
import spock.lang.Specification
import spock.lang.Unroll

class YamlValueNotEqualsSpec extends Specification {

    def "equals"() {
        expect:
        YamlValueNotEquals.equals('/key', 'value')
        YamlValueNotEquals.equals(['/key': 'value'])
    }

    def "enforce - null input stream"() {
        when:
        YamlValueNotEquals.equals('/foo', "bar").enforceInternal(null)

        then:
        thrown(NullPointerException)
    }

    @Unroll
    def "enforce - #pointerExpression = #expectedValue (passed)"() {
        given:
        YamlValueNotEquals yamlPathNotEquals = YamlValueNotEquals.equals(pointerExpression, expectedValue)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/bicycle.yml')

        when:
        EnforcerResult result = yamlPathNotEquals.enforce(fileInputStream)

        then:
        result
        result.passed
        !result.messages

        where:
        pointerExpression | expectedValue
        '/make'           | 'Trek'
        '/size/value'     | 61
        '/components/0'   | 'brakes'
    }

    def "enforce - map (passed)"() {
        given:
        def map = [
                '/make'        : 'Trek',
                '/size/value'  : 61,
                '/components/0': 'brakes'
        ]
        YamlValueNotEquals yamlPathNotEquals = YamlValueNotEquals.equals(map)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/bicycle.yml')

        when:
        EnforcerResult result = yamlPathNotEquals.enforce(fileInputStream)

        then:
        result
        result.passed
        !result.messages
    }

    @Unroll
    def "enforce - #pointerExpression = #expectedValue (failed - incorrect value)"() {
        given:
        YamlValueNotEquals yamlPathNotEquals = YamlValueNotEquals.equals(pointerExpression, expectedValue)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/bicycle.yml')

        when:
        EnforcerResult result = yamlPathNotEquals.enforce(fileInputStream)

        then:
        result
        !result.passed
        result.messages
        result.messages[0] == "Execution of pointer expression [$pointerExpression] yielded result [$actualValue] which does equal [$expectedValue]"

        where:
        pointerExpression | actualValue  | expectedValue
        '/make'           | 'Raleigh'    | 'Raleigh'
        '/size/value'     | 60           | 60
        '/components/0'   | 'handlebars' | 'handlebars'
    }

    @Unroll
    def "enforce - #pointerExpression = #expectedValue (failed - missing)"() {
        given:
        YamlValueNotEquals yamlPathNotEquals = YamlValueNotEquals.equals(pointerExpression, expectedValue)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/bicycle.yml')

        when:
        EnforcerResult result = yamlPathNotEquals.enforce(fileInputStream)

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
    def "enforce - #pointerExpression (failed - pointer expression error)"() {
        given:
        YamlValueNotEquals yamlPathNotEquals = YamlValueNotEquals.equals(pointerExpression, 'road')
        InputStream fileInputStream = IoUtil.getResourceAsStream('/bicycle.yml')

        when:
        EnforcerResult result = yamlPathNotEquals.enforce(fileInputStream)

        then:
        result
        !result.passed
        result.messages
        result.messages[0].startsWith("Execution of pointer expression [$pointerExpression] yielded error")

        where:
        pointerExpression << ['.', '$']
    }

}
