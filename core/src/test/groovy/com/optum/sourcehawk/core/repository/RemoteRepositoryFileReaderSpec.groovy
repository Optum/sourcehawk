package com.optum.sourcehawk.core.repository

import spock.lang.Specification


class RemoteRepositoryFileReaderSpec extends Specification {

    def "constructor"() {
        expect:
        new GenericRemoteRepositoryFileReader()
        new TrailingSlashRemoteRepositoryFileReader()
    }

    def "constructor - null argument"() {
        when:
        new InvalidRemoteRepositoryFileReader()

        then:
        thrown(NullPointerException)

        when:
        new InvalidRemoteRepositoryFileReader2()

        then:
        thrown(NullPointerException)
    }

    private static class GenericRemoteRepositoryFileReader extends RemoteRepositoryFileReader {

        protected GenericRemoteRepositoryFileReader() {
            super("https://optum.github.io")
        }
    }

    private static class TrailingSlashRemoteRepositoryFileReader extends RemoteRepositoryFileReader {

        protected TrailingSlashRemoteRepositoryFileReader() {
            super("https://optum.github.io/")
        }
    }

    private static class InvalidRemoteRepositoryFileReader extends RemoteRepositoryFileReader {

        protected InvalidRemoteRepositoryFileReader() {
            super(null)
        }
    }

    private static class InvalidRemoteRepositoryFileReader2 extends RemoteRepositoryFileReader {

        protected InvalidRemoteRepositoryFileReader2() {
            super(null, Collections.<String, String>emptyMap())
        }
    }

}