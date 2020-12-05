package com.optum.sourcehawk.exec

import com.optum.sourcehawk.core.configuration.SourcehawkConfiguration
import sun.nio.ch.ChannelInputStream

class ConfigurationReaderSpec extends FileBaseSpecification {

    def "readConfiguration - found"() {
        given:
        String configurationFileLocation = "sourcehawk.yml"

        when:
        Optional<SourcehawkConfiguration> sourcehawkConfigurationOptional = ConfigurationReader.readConfiguration(repositoryRoot, configurationFileLocation)

        then:
        sourcehawkConfigurationOptional
        sourcehawkConfigurationOptional.isPresent()
    }

    def "obtainInputStream - URL configuration file"() {
        given:
        String configurationFileLocation = "https://raw.githubusercontent.com/optum/sourcehawk-parent/main/.sourcehawk/config.yml"

        when:
        InputStream inputStream = ConfigurationReader.obtainInputStream(repositoryRoot, configurationFileLocation)

        then:
        inputStream
        inputStream.class.simpleName == "HttpInputStream"
    }

    def "obtainInputStream - absolute file"() {
        given:
        String configurationFileLocation = repositoryRoot.resolve("sourcehawk.yml").toAbsolutePath().toString()

        when:
        InputStream inputStream = ConfigurationReader.obtainInputStream(repositoryRoot, configurationFileLocation)

        then:
        inputStream instanceof ChannelInputStream
    }

    def "obtainInputStream - relative file"() {
        given:
        String configurationFileLocation = "sourcehawk.yml"

        when:
        InputStream inputStream = ConfigurationReader.obtainInputStream(repositoryRoot, configurationFileLocation)

        then:
        inputStream
        inputStream instanceof ChannelInputStream
    }

    def "merge files"() {
        given:
        LinkedHashSet set = [] as LinkedHashSet
        set << ConfigurationReader.parseConfiguration(testResourcesRoot.resolve("sourcehawk-simple.yml"))
        set << ConfigurationReader.parseConfiguration(testResourcesRoot.resolve("sourcehawk-simple2.yml"))

        when:
        SourcehawkConfiguration configuration = ConfigurationReader.merge(set)

        then:
        configuration.fileProtocols.size() == 4
    }

    def "merge files with dupes"() {
        given:
        LinkedHashSet set = [] as LinkedHashSet
        set << ConfigurationReader.parseConfiguration(testResourcesRoot.resolve("sourcehawk-simple.yml"))
        set << ConfigurationReader.parseConfiguration(testResourcesRoot.resolve("sourcehawk-simple2.yml"))
        set << ConfigurationReader.parseConfiguration(testResourcesRoot.resolve("sourcehawk-simple3.yml"))
        set << ConfigurationReader.parseConfiguration(testResourcesRoot.resolve("sourcehawk-simple3.yml"))

        when:
        SourcehawkConfiguration configuration = ConfigurationReader.merge(set)

        then:
        configuration.fileProtocols.size() == 4

        and:
        configuration.fileProtocols.find{ it.name == "Readme Config"}
        configuration.fileProtocols.find{ it.name == "Lombok"}
        configuration.fileProtocols.find{ it.name == "Lombok Different"}
        configuration.fileProtocols.find{ it.name == "Gitignore Config"}

        configuration.fileProtocols.find{ it.name == "Readme Config"}.enforcers.size() == 0
        configuration.fileProtocols.find{ it.name == "Lombok"}.enforcers.size() == 2
        configuration.fileProtocols.find{ it.name == "Lombok Different"}.enforcers.size() == 2
        configuration.fileProtocols.find{ it.name == "Gitignore Config"}.enforcers.size() == 0
    }

    def "merge files - null"() {
        given:
        LinkedHashSet set = [] as LinkedHashSet
        set << ConfigurationReader.parseConfiguration(testResourcesRoot.resolve("sourcehawk-simple.yml"))
        set << null

        when:
        SourcehawkConfiguration configuration = ConfigurationReader.merge(set)

        then:
        configuration.fileProtocols.size() == 2
    }
}
