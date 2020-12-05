package com.optum.sourcehawk.core.configuration


import com.optum.sourcehawk.core.protocol.file.FileProtocol
import spock.lang.Specification

class SourcehawkConfigurationSpec extends Specification {

    def "of"() {
        given:
        Set<String> configLocations = []
        Collection<FileProtocol> fileProtocols = Collections.emptySet()

        when:
        SourcehawkConfiguration sourceHawkConfiguration = SourcehawkConfiguration.of(configLocations, fileProtocols)

        then:
        sourceHawkConfiguration
        sourceHawkConfiguration.fileProtocols == fileProtocols
        sourceHawkConfiguration.configLocations == configLocations
    }

    def "of - null null null"() {
        when:
        SourcehawkConfiguration sourceHawkConfiguration = SourcehawkConfiguration.of(null, null)

        then:
        sourceHawkConfiguration
        !sourceHawkConfiguration.configLocations
        !sourceHawkConfiguration.fileProtocols
    }

}
