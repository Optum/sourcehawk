package com.optum.sourcehawk.core.protocol.file

import spock.lang.Specification

class FileProtocolSpec extends Specification {

    def "builder - defaults"() {
        given:
        FileProtocol fileProtocol = FileProtocol.builder()
                .name("fp")
                .repositoryPath("path/to/file.txt")
                .build()

        expect:
        fileProtocol
        fileProtocol.name == "fp"
        fileProtocol.repositoryPath == "path/to/file.txt"
    }

    def "builder - overrides"() {
        given:
        FileProtocol fileProtocol = FileProtocol.builder()
                .name("name")
                .description("description")
                .repositoryPath("path/to/file.txt")
                .group("group")
                .required(false)
                .tags(["tag"] as String[])
                .severity("WARNING")
                .enforcers([["enforcer": "dummy"]])
                .build()

        expect:
        fileProtocol
        fileProtocol.name == "name"
        fileProtocol.description == "description"
        fileProtocol.repositoryPath == "path/to/file.txt"
        fileProtocol.group == "group"
        !fileProtocol.required
        fileProtocol.tags
        fileProtocol.severity == "WARNING"
        fileProtocol.enforcers
    }

    def "toString and equals"() {
        given:
        FileProtocol fp1 = FileProtocol.builder()
                .name("name")
                .description("description")
                .repositoryPath("path/to/file.txt")
                .group("group")
                .required(false)
                .tags(["tag"] as String[])
                .severity("WARNING")
                .enforcers([["enforcer": "dummy"]])
                .build()
        FileProtocol fp2 = FileProtocol.builder()
                .name("name")
                .description("description")
                .repositoryPath("path/to/file.txt")
                .group("group")
                .required(false)
                .tags(["tag"] as String[])
                .severity("WARNING")
                .enforcers([["enforcer": "dummy"]])
                .build()

        expect:
        fp1.toString() == fp2.toString()
        fp1 == fp2
        fp1.equals(fp2)
        fp1.hashCode() == fp2.hashCode()
    }

}
