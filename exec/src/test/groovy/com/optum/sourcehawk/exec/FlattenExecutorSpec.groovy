package com.optum.sourcehawk.exec

import com.optum.sourcehawk.core.scan.FlattenResult

class FlattenExecutorSpec extends FileBaseSpecification {

    def "flatten - handleException"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(testResourcesRoot)
                .configurationFileLocation(testResourcesRoot.toString() + "/sourcehawk-override.yml")
                .repositoryRoot(repositoryRoot)
                .build()

        when:
        FlattenResult flattenResult = FlattenExecutor.handleException(execOptions, new IOException())

        then:
        flattenResult.error
    }

    def "flatten - with overrides"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(testResourcesRoot)
                .configurationFileLocation(testResourcesRoot.toString() + "/sourcehawk-override.yml")
                .repositoryRoot(repositoryRoot)
                .build()

        when:
        FlattenResult flattenResult = FlattenExecutor.flatten(execOptions)

        then:
        flattenResult
        !flattenResult.error
        flattenResult.content
        new String(flattenResult.content) == getFlattenedConfig()
    }

    def "flatten - absolute configuration file"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .configurationFileLocation(repositoryRoot.resolve("sourcehawk.yml").toAbsolutePath().toString())
                .build()

        when:
        FlattenResult flattenResult = FlattenExecutor.flatten(execOptions)

        then:
        flattenResult
        !flattenResult.error
        flattenResult.content
    }

    def "flatten - local override"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .configurationFileLocation(repositoryRoot.resolve(".test/override.yml").toString())
                .build()

        when:
        FlattenResult flattenResult = FlattenExecutor.flatten(execOptions)
        println new String(flattenResult.content)

        then:
        flattenResult
        !flattenResult.error
        flattenResult.content
    }

    def "flatten - bad url"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .configurationFileLocation(repositoryRoot.resolve(".test/bad-url.yml").toString())
                .build()

        when:
        FlattenExecutor.flatten(execOptions)

        then:
        thrown(ConfigurationException)
    }


    def "flatten - local relative"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .configurationFileLocation(repositoryRoot.resolve(".test/local.yml").toString())
                .build()

        when:
        FlattenResult flattenResult = FlattenExecutor.flatten(execOptions)

        then:
        flattenResult
        !flattenResult.error
        flattenResult.content
    }

    def "flatten - URL configuration file"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .configurationFileLocation("https://raw.githubusercontent.com/optum/sourcehawk-parent/main/.sourcehawk/config.yml")
                .build()

        when:
        FlattenResult flattenResult = FlattenExecutor.flatten(execOptions)

        then:
        flattenResult
        !flattenResult.error
        flattenResult.content
    }

    def "flatten - relative configuration file - configuration file not found"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .configurationFileLocation("Sourcehawk")
                .build()

        when:
        FlattenResult flattenResult = FlattenExecutor.flatten(execOptions)

        then:
        flattenResult
        flattenResult.error
    }

    def "flatten - file not found (no enforcers)"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .configurationFileLocation(testResourcesRoot.resolve("sourcehawk-file-not-found.yml").toString())
                .build()

        when:
        FlattenResult flattenResult = FlattenExecutor.flatten(execOptions)

        then:
        flattenResult
        !flattenResult.error
        flattenResult.content

        and:
        noExceptionThrown()
    }

    def "flatten - bad file"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .configurationFileLocation(testResourcesRoot.resolve("sourcehawk-invalid-protocol.yml").toString())
                .build()

        when:
        FlattenResult flattenResult = FlattenExecutor.flatten(execOptions)

        then:
        flattenResult
        flattenResult.error
        !flattenResult.content

        and:
        noExceptionThrown()
    }

    def "flatten - file not found (with enforcers)"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .configurationFileLocation(testResourcesRoot.resolve("sourcehawk-file-not-found-enforcers.yml").toString())
                .build()

        when:
        FlattenResult flattenResult = FlattenExecutor.flatten(execOptions)

        then:
        flattenResult
        !flattenResult.error
        flattenResult.content

        and:
        noExceptionThrown()
    }

    def "flatten - no enforcers"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .configurationFileLocation(testResourcesRoot.resolve("sourcehawk-no-enforcers.yml").toString())
                .build()

        when:
        FlattenResult flattenResult = FlattenExecutor.flatten(execOptions)

        then:
        flattenResult
        !flattenResult.error
        flattenResult.content

        and:
        noExceptionThrown()
    }

    // TODO: temporary testing directory

    static String getFlattenedConfig() {
        """---
fileProtocols:
- name: "Maven Banned Deps"
  description: "Maven pom.xml banned deps check"
  group: "maven"
  repositoryPath: "pom.xml"
  required: true
  tags:
  - "maven"
  - "dependencies"
  severity: "ERROR"
  enforcers:
  - enforcer: ".maven.MavenBannedProperties"
    banned-properties:
      maven.test.skip: true
      enforcer.skip: true
      jacoco.skip: true
      special.skip: true
- name: "Maven Pom"
  description: "maven pom.xml has correct configuration"
  group: "maven"
  repositoryPath: "pom.xml"
  required: true
  tags:
  - "maven"
  severity: "ERROR"
  enforcers:
  - enforcer: ".xml.XPathEquals"
    expectations:
      //project/ciManagement/system/text(): "github"
- name: "License"
  description: null
  group: null
  repositoryPath: "LICENSE"
  required: true
  tags: []
  severity: "ERROR"
  enforcers: []
- name: "Individual Contributor License"
  description: null
  group: null
  repositoryPath: "INDIVIDUAL_CONTRIBUTOR_LICENSE.md"
  required: true
  tags: []
  severity: "ERROR"
  enforcers:
  - enforcer: ".common.ContainsLineMatchingAt"
    expected-line-pattern: "(.*)Individual Contributor License Agreement(.*)"
    expected-line-number: 1
- name: "Lombok Config"
  description: "Lombok Configured appropriately"
  group: "lombok"
  repositoryPath: "lombok.config"
  required: true
  tags:
  - "lombok"
  - "config"
  severity: "WARNING"
  enforcers:
  - enforcer: ".common.StringPropertyEquals"
    property-name: "config.stopBubbling"
    expected-property-value: false
  - enforcer: ".common.StringPropertyEquals"
    property-name: "lombok.anyConstructor.addConstructorProperties"
    expected-property-value: false
- name: "Notice"
  description: null
  group: null
  repositoryPath: "NOTICE.txt"
  required: true
  tags: []
  severity: "ERROR"
  enforcers:
  - enforcer: ".common.ContainsLineAt"
    expected-line: "sourcehawk"
    expected-line-number: 1
  - enforcer: ".common.ContainsLineAt"
    expected-line: "Copyright 2020 Optum"
    expected-line-number: 3
  - enforcer: ".common.ContainsLine"
    expected-line: "Project Description:"
  - enforcer: ".common.ContainsLine"
    expected-line: "@brianwyka - Project Lead"
  - enforcer: ".common.ContainsLine"
    expected-line: "@ctoestriech - Project Lead"
- name: "Maven Wrapper"
  description: "Maven Wrapper exists"
  group: null
  repositoryPath: ".mvn/wrapper/maven-wrapper.jar"
  required: true
  tags: []
  severity: "ERROR"
  enforcers: []
- name: "Maven Wrapper"
  description: "Maven build wrapper"
  group: "lombok"
  repositoryPath: ".mvn/wrapper/maven-wrapper.properties"
  required: true
  tags:
  - "lombok"
  - "config"
  severity: "ERROR"
  enforcers:
  - enforcer: ".common.StringPropertyEquals"
    property-name: "distributionUrl"
    expected-property-value: "https://apache.claz.org/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.zip"
- name: "Code of Conduct"
  description: null
  group: null
  repositoryPath: "CODE_OF_CONDUCT.md"
  required: true
  tags: []
  severity: "ERROR"
  enforcers:
  - enforcer: ".common.ContainsLine"
    expected-line: "# Contributor Covenant Code of Conduct"
  - enforcer: ".common.ContainsLine"
    expected-line: "[homepage]: http://contributor-covenant.org"
  - enforcer: ".common.ContainsLine"
    expected-line: "[email]: mailto:opensource@optum.com"
- name: "Attribution"
  description: null
  group: null
  repositoryPath: "attribution.txt"
  required: true
  tags: []
  severity: "ERROR"
  enforcers: []
- name: "Contributing"
  description: null
  group: null
  repositoryPath: "CONTRIBUTING.md"
  required: true
  tags: []
  severity: "ERROR"
  enforcers:
  - enforcer: ".common.ContainsLine"
    expected-line: "[email]: mailto:opensource@optum.com"
"""
    }
}
