package com.optum.sourcehawk.exec;

import com.optum.sourcehawk.core.repository.GithubRepositoryFileReader;
import com.optum.sourcehawk.core.repository.LocalRepositoryFileReader;
import com.optum.sourcehawk.core.repository.RepositoryFileReader;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * A helper class for executors
 */
@UtilityClass
public class ExecutorHelper {

    /**
     * Get an instance of the repository file reader based on the exec options
     *
     * @param execOptions the exec options
     * @return the repository file reader
     */
    public RepositoryFileReader getRepositoryFileReader(final ExecOptions execOptions) {
        if (execOptions.getGithub() != null) {
            val githubCoordinates = execOptions.getGithub().getCoords().split("/");
            if (execOptions.getGithub().getEnterpriseUrl() != null) {
                return new GithubRepositoryFileReader(
                        execOptions.getGithub().getToken(),
                        execOptions.getGithub().getEnterpriseUrl().toString(),
                        githubCoordinates[0],
                        githubCoordinates[1],
                        execOptions.getGithub().getRef()
                );
            }
            return new GithubRepositoryFileReader(execOptions.getGithub().getToken(), githubCoordinates[0], githubCoordinates[1], execOptions.getGithub().getRef());
        }
        return LocalRepositoryFileReader.create(execOptions.getRepositoryRoot());
    }

}
