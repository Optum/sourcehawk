package com.optum.sourcehawk.exec;

import com.optum.sourcehawk.core.repository.GithubRepositoryFileReader;
import com.optum.sourcehawk.core.repository.LocalRepositoryFileReader;
import com.optum.sourcehawk.core.repository.RepositoryFileReader;
import lombok.experimental.UtilityClass;

/**
 * A helper class for executors
 *
 * @author Brian Wyka
 */
@UtilityClass
public class ExecutorHelper {

    /**
     * Get an instance of the repository file reader based on the exec options
     *
     * @param execOptions the exec options
     * @return the repository file reader
     */
    public RepositoryFileReader resolveRepositoryFileReader(final ExecOptions execOptions) {
        if (execOptions.getGithub() != null) {
            if (execOptions.getGithub().getEnterpriseUrl() != null) {
                return new GithubRepositoryFileReader(
                        execOptions.getGithub().getToken(),
                        execOptions.getGithub().getEnterpriseUrl().toString(),
                        execOptions.getGithub().getOwner(),
                        execOptions.getGithub().getRepository(),
                        execOptions.getGithub().getRef()
                );
            }
            return new GithubRepositoryFileReader(
                    execOptions.getGithub().getToken(),
                    execOptions.getGithub().getOwner(),
                    execOptions.getGithub().getRepository(),
                    execOptions.getGithub().getRef()
            );
        }
        return LocalRepositoryFileReader.create(execOptions.getRepositoryRoot());
    }

}
