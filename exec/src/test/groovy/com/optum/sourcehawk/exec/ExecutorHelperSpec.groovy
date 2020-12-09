package com.optum.sourcehawk.exec

import com.optum.sourcehawk.core.repository.GithubRepositoryFileReader
import com.optum.sourcehawk.core.repository.LocalRepositoryFileReader

class ExecutorHelperSpec extends FileBaseSpecification {

    def "private constructor"() {
        when:
        new ExecutorHelper()

        then:
        thrown(UnsupportedOperationException)
    }

    def "getRepositoryFileReader - local"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .build()

        expect:
        ExecutorHelper.getRepositoryFileReader(execOptions) instanceof LocalRepositoryFileReader
    }

    def "getRepositoryFileReader - github"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .github(ExecOptions.GithubOptions.builder()
                        .coords("owner/repo")
                        .ref("ref")
                        .build())
                .build()

        expect:
        ExecutorHelper.getRepositoryFileReader(execOptions) instanceof GithubRepositoryFileReader
    }

    def "getRepositoryFileReader - github enterprise"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .github(ExecOptions.GithubOptions.builder()
                        .coords("owner/repo")
                        .ref("ref")
                        .enterpriseUrl(new URL("https://github.example.com"))
                        .build())
                .build()

        expect:
        ExecutorHelper.getRepositoryFileReader(execOptions) instanceof GithubRepositoryFileReader
    }

}
