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

    def "resolveRepositoryFileReader - local"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .repositoryRoot(repositoryRoot)
                .build()

        expect:
        ExecutorHelper.resolveRepositoryFileReader(execOptions) instanceof LocalRepositoryFileReader
    }

    def "resolveRepositoryFileReader - github"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .github(ExecOptions.GithubOptions.builder()
                        .owner("owner")
                        .repository("repo")
                        .ref("ref")
                        .build())
                .build()

        expect:
        ExecutorHelper.resolveRepositoryFileReader(execOptions) instanceof GithubRepositoryFileReader
    }

    def "resolveRepositoryFileReader - github enterprise"() {
        given:
        ExecOptions execOptions = ExecOptions.builder()
                .github(ExecOptions.GithubOptions.builder()
                        .owner("owner")
                        .repository("repo")
                        .ref("ref")
                        .enterpriseUrl(new URL("https://github.example.com"))
                        .build())
                .build()

        expect:
        ExecutorHelper.resolveRepositoryFileReader(execOptions) instanceof GithubRepositoryFileReader
    }

}
