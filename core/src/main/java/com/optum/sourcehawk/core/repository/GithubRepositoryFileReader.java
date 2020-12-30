package com.optum.sourcehawk.core.repository;

import com.optum.sourcehawk.core.data.RemoteRef;
import lombok.NonNull;
import lombok.val;

/**
 * A {@link RemoteRepositoryFileReader} implementation which reads files from remote Github repositories
 *
 * @author Brian Wyka
 */
public class GithubRepositoryFileReader extends RemoteRepositoryFileReader {

    /**
     * The authorization token prefix
     */
    private static final String AUTHORIZATION_TOKEN_PREFIX = "token ";

    /**
     * Constructs an instance of this reader with the provided Github Enterprise URL
     *
     * @param token the github token (optional)
     * @param githubEnterpriseUrl the Github enterprise URL
     * @param remoteRef the remote reference
     */
    public GithubRepositoryFileReader(final String token, @NonNull final String githubEnterpriseUrl, @NonNull final RemoteRef remoteRef) {
        super(constructBaseUrl(githubEnterpriseUrl, true, remoteRef), constructRequestProperties(AUTHORIZATION_TOKEN_PREFIX, token));
    }

    /**
     * Constructs an instance of this reader with the provided owner, repo, and ref
     *
     * @param token the github token (optional)
     * @param remoteRef the remote reference
     */
    public GithubRepositoryFileReader(final String token, @NonNull final RemoteRef remoteRef) {
        super(constructBaseUrl(RemoteRef.Type.GITHUB.getBaseUrl(), false, remoteRef), constructRequestProperties(AUTHORIZATION_TOKEN_PREFIX, token));
    }

    /**
     * Construct the base URL of the Github repository based on the provided owner, repo, and ref
     *
     * @param githubUrl the Github URL
     * @param githubEnterprise true if Github Enterprise, false otherwise
     * @param remoteRef the remote reference
     * @return the constructed base URL with a trailing {@value #SEPARATOR}
     */
    public static String constructBaseUrl(final String githubUrl, final boolean githubEnterprise, final RemoteRef remoteRef) {
        val baseUrl = githubUrl.endsWith(SEPARATOR) ? githubUrl.substring(0, githubUrl.length() - 1) : githubUrl;
        val githubBaseUrl = githubEnterprise ? baseUrl + "/raw" : baseUrl;
        return String.format("%s/%s/%s/%s/", githubBaseUrl, remoteRef.getNamespace(), remoteRef.getRepository(), remoteRef.getRef());
    }

}
