package com.optum.sourcehawk.cli

import org.spockframework.util.IoUtil
import picocli.CommandLine
import spock.lang.Specification

class SourcehawkVersionProviderSpec extends Specification {

    CommandLine.IVersionProvider versionProvider = new Sourcehawk.VersionProvider()

    def "VERSION"() {
        expect:
        Sourcehawk.VersionProvider.VERSION
    }

    def "getVersion"() {
        when:
        versionProvider.getVersion()

        then:
        noExceptionThrown()
    }

    def "loadProperties - input stream null"() {
        when:
        Optional<Properties> properties = Sourcehawk.VersionProvider.loadProperties(null)

        then:
        properties == Optional.empty()
    }

    def "loadProperties - input stream closed"() {
        given:
        InputStream inputStream = IoUtil.getResourceAsStream("/sourcehawk.properties")
        inputStream.close()

        when:
        Optional<Properties> properties = Sourcehawk.VersionProvider.loadProperties(inputStream)

        then:
        properties == Optional.empty()
    }

}
