package com.optum.sourcehawk.core.repository

import spock.lang.Specification
import spock.lang.Unroll

class GithubRepositoryFileReaderSpec extends Specification {

    // TODO: mock server

    def "public github > exists (file found)"() {
        given:
        String owner = "optum"
        String repo = "sourcehawk"
        String ref = "main"

        when:
        GithubRepositoryFileReader githubRepositoryFileReader = new GithubRepositoryFileReader(null, owner, repo, ref)

        then:
        githubRepositoryFileReader

        when:
        boolean exists = githubRepositoryFileReader.exists("README.md")

        then:
        exists
    }

    def "public github - exists (not found)"() {
        when:
        GithubRepositoryFileReader githubRepositoryFileReader = new GithubRepositoryFileReader(null, "optum", "sourcehawk", "nope")

        then:
        githubRepositoryFileReader

        when:
        boolean exists = githubRepositoryFileReader.exists("abc.txt")

        then:
        !exists
    }

    def "public github > read (file found)"() {
        given:
        String owner = "optum"
        String repo = "sourcehawk"
        String ref = "main"

        when:
        GithubRepositoryFileReader githubRepositoryFileReader = new GithubRepositoryFileReader(null, owner, repo, ref)

        then:
        githubRepositoryFileReader

        when:
        Optional<InputStream> inputStream = githubRepositoryFileReader.read("README.md")

        then:
        inputStream
        inputStream.isPresent()

        when:
        inputStream = githubRepositoryFileReader.read("/README.md")

        then:
        inputStream
        inputStream.isPresent()
    }

    @Unroll
    def "public github - read (not found) - #owner/#repo/#ref - #repositoryFilePath"() {
        when:
        GithubRepositoryFileReader githubRepositoryFileReader = new GithubRepositoryFileReader(null, owner, repo, ref)

        then:
        githubRepositoryFileReader

        when:
        Optional<InputStream> inputStream = githubRepositoryFileReader.read(repositoryFilePath)

        then:
        !inputStream

        where:
        owner    | repo          | ref       | repositoryFilePath
        "optum2" | "sourcehawk"  | "main"    | "README.md"
        "optum"  | "sourcehawk2" | "main"    | "README.md"
        "optum"  | "sourcehawk"  | "main2"   | "README.md"
        "optum"  | "sourcehawk"  | "main"    | "README2.md"
    }

    def "constructBaseUrl - public github"() {
        given:
        String githubUrl = "https://raw.githubusercontent.com"
        String owner = "owner"
        String repo = "repo"
        String ref = "main"

        when:
        String baseUrl = GithubRepositoryFileReader.constructBaseUrl(githubUrl, false, owner, repo, ref)

        then:
        baseUrl == "https://raw.githubusercontent.com/owner/repo/main/"
    }

    @Unroll
    def "constructBaseUrl - enterprise github"() {
        given:
        String owner = "owner"
        String repo = "repo"
        String ref = "main"

        when:
        String baseUrl = GithubRepositoryFileReader.constructBaseUrl(githubUrl, true, owner, repo, ref)

        then:
        baseUrl == "https://github.example.com/raw/owner/repo/main/"

        where:
        githubUrl << ["https://github.example.com", "https://github.example.com/"]
    }

    def "constructRequestProperties"() {
        when:
        Map<String, String> requestProperties = GithubRepositoryFileReader.constructRequestProperties("abc")

        then:
        requestProperties
        requestProperties.size() == 2
        requestProperties["Accept"] == "text/plain"
        requestProperties["Authorization"] == "token abc"
    }

    def "constructRequestProperties - null"() {
        when:
        Map<String, String> requestProperties = GithubRepositoryFileReader.constructRequestProperties(null)

        then:
        requestProperties
        requestProperties.size() == 1
        requestProperties["Accept"] == "text/plain"
    }

    def "constructor - enterprise"() {
        expect:
        new GithubRepositoryFileReader(null, "https://github.example.com", "owner", "repo", "ref")
        new GithubRepositoryFileReader("abc", "https://github.example.com", "owner", "repo", "ref")
    }

    def "constructors - null parameter"() {
        when:
        new GithubRepositoryFileReader("abc", null, "owner", "repo", "ref")

        then:
        thrown(NullPointerException)

        when:
        new GithubRepositoryFileReader("abc", "https://github.example.com", null, "repo", "ref")

        then:
        thrown(NullPointerException)

        when:
        new GithubRepositoryFileReader("abc", "https://github.example.com", "owner", null, "ref")

        then:
        thrown(NullPointerException)

        when:
        new GithubRepositoryFileReader("abc", "https://github.example.com", "owner", "repo", null)

        then:
        thrown(NullPointerException)

        when:
        new GithubRepositoryFileReader("abc", null, "repo", "ref")

        then:
        thrown(NullPointerException)

        when:
        new GithubRepositoryFileReader("abc","owner", null, "ref")

        then:
        thrown(NullPointerException)

        when:
        new GithubRepositoryFileReader("abc","owner", "repo", null)

        then:
        thrown(NullPointerException)
    }

}
