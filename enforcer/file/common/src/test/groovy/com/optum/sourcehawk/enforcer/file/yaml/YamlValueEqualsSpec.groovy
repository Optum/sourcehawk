package com.optum.sourcehawk.enforcer.file.yaml


import com.optum.sourcehawk.enforcer.EnforcerResult
import org.spockframework.util.IoUtil
import spock.lang.Specification
import spock.lang.Unroll

class YamlValueEqualsSpec extends Specification {

    def "equals"() {
        expect:
        YamlValueEquals.equals('/key', 'value')
        YamlValueEquals.equals(['/key': 'value'])
    }

    def "enforce - null input stream"() {
        when:
        YamlValueEquals.equals('/foo', "bar").enforceInternal(null)

        then:
        thrown(NullPointerException)
    }

    @Unroll
    def "enforce - #pointerExpression = #expectedValue (passed)"() {
        given:
        YamlValueEquals yamlPathEquals = YamlValueEquals.equals(pointerExpression, expectedValue)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/bicycle.yml')

        when:
        EnforcerResult result = yamlPathEquals.enforce(fileInputStream)

        then:
        result
        result.passed
        !result.messages

        where:
        pointerExpression | expectedValue
        '/make'           | 'Raleigh'
        '/size/value'     | 60
        '/components/0'   | 'handlebars'
        '/components/0'   | 'handle.*'
        '/components/0'   | '^(?:(?!pedals).)*$'
    }

    def "enforce - map (passed)"() {
        given:
        def map = [
                '/make'        : 'Raleigh',
                '/size/value'  : 60,
                '/components/0': 'handlebars'
        ]
        YamlValueEquals yamlPathEquals = YamlValueEquals.equals(map)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/bicycle.yml')

        when:
        EnforcerResult result = yamlPathEquals.enforce(fileInputStream)

        then:
        result
        result.passed
        !result.messages
    }

    @Unroll
    def "enforce - #pointerExpression = #expectedValue (failed - incorrect value)"() {
        given:
        YamlValueEquals yamlPathEquals = YamlValueEquals.equals(pointerExpression, expectedValue)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/bicycle.yml')

        when:
        EnforcerResult result = yamlPathEquals.enforce(fileInputStream)

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
        YamlValueEquals yamlPathEquals = YamlValueEquals.equals(pointerExpression, expectedValue)
        InputStream fileInputStream = IoUtil.getResourceAsStream('/bicycle.yml')

        when:
        EnforcerResult result = yamlPathEquals.enforce(fileInputStream)

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
        YamlValueEquals yamlPathEquals = YamlValueEquals.equals(pointerExpression, 'road')
        InputStream fileInputStream = IoUtil.getResourceAsStream('/bicycle.yml')

        when:
        EnforcerResult result = yamlPathEquals.enforce(fileInputStream)

        then:
        result
        !result.passed
        result.messages
        result.messages[0].startsWith("Execution of pointer expression [$pointerExpression] yielded error")

        where:
        pointerExpression << ['.', '$']
    }
    
}
