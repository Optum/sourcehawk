package com.optum.sourcehawk.core.repository

import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

class LocalRepositoryFileWriterSpec extends Specification {

    def "create"() {
        expect:
        LocalRepositoryFileWriter.writer()
    }

    def "write text to file and read it"() {
        given:
        String fileName = UUID.randomUUID().toString()
        File fileResource = File.createTempFile(fileName, ".txt")
        Path repositoryRoot = Paths.get(fileResource.getParentFile().getAbsolutePath())
        RepositoryFileWriter repositoryFileWriter = LocalRepositoryFileWriter.writer()
        RepositoryFileReader repositoryFileReader = LocalRepositoryFileReader.create(repositoryRoot)

        when:
        Optional<InputStream> fileInputStream = repositoryFileReader.read(fileResource.getAbsoluteFile().toString())
        String fileText = fileInputStream.get().text

        then:
        fileInputStream.isPresent()
        fileText.isEmpty()

        when:
        repositoryFileWriter.write(fileResource.getAbsolutePath(), "hello".bytes)

        then:
        notThrown(Exception)

        when:
        fileInputStream = repositoryFileReader.read(fileResource.getAbsolutePath())
        String helloFileText = fileInputStream.get().text

        then:
        fileInputStream
        fileInputStream.isPresent()
        helloFileText == "hello"
    }

    def "write text to file and read it if not already exists"() {
        given:
        String fileName = UUID.randomUUID().toString() + ".txt"
        File dirResource = File.createTempDir()
        Path repositoryRoot = Paths.get(dirResource.getAbsolutePath())
        RepositoryFileWriter repositoryFileWriter = LocalRepositoryFileWriter.writer()
        RepositoryFileReader repositoryFileReader = LocalRepositoryFileReader.create(repositoryRoot)

        when:
        Optional<InputStream> fileInputStream = repositoryFileReader.read(fileName)

        then:
        !fileInputStream.isPresent()

        when:

        repositoryFileWriter.write(dirResource.getAbsolutePath() + "/" + fileName, "hello".bytes)

        then:
        notThrown(Exception)

        when:
        fileInputStream = repositoryFileReader.read(fileName)
        String helloFileText = fileInputStream.get().text

        then:
        fileInputStream
        fileInputStream.isPresent()
        helloFileText == "hello"
    }

    def "write text to file using null values"() {
        given:
        String fileName = UUID.randomUUID().toString() + ".txt"
        File dirResource = File.createTempDir()
        Path repositoryRoot = Paths.get(dirResource.getAbsolutePath())
        RepositoryFileWriter repositoryFileWriter = LocalRepositoryFileWriter.writer()
        RepositoryFileReader repositoryFileReader = LocalRepositoryFileReader.create(repositoryRoot)

        when:
        Optional<InputStream> fileInputStream = repositoryFileReader.read(fileName)

        then:
        !fileInputStream.isPresent()

        when:
        repositoryFileWriter.write(path, content)

        then:
        thrown(Exception)

        where:
        path     | content
        "target" | null
        null     | null
        null     | "test".bytes
    }

}
