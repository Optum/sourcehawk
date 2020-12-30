package com.optum.sourcehawk.core.repository;

import com.optum.sourcehawk.core.data.RemoteRef;
import lombok.NonNull;
import lombok.val;

/**
 * A {@link RemoteRepositoryFileReader} implementation which reads files from remote Bitbucket repositories
 *
 * @author Brian Wyka
 */
public class BitbucketRepositoryFileReader extends RemoteRepositoryFileReader {

    /**
     * The authorization token prefix
     */
    private static final String AUTHORIZATION_TOKEN_PREFIX = "Bearer ";

    /**
     * Constructs the Bitbucket repository file reader
     *
     * @param token the token
     * @param bitbucketBaseUrl the Bitbucket base URL
     * @param remoteRef the remote ref
     */
    public BitbucketRepositoryFileReader(final String token, @NonNull final String bitbucketBaseUrl, final RemoteRef remoteRef) {
        super(constructBaseUrl(remoteRef, bitbucketBaseUrl), constructRequestProperties(AUTHORIZATION_TOKEN_PREFIX, token));
    }

    /**
     * Constructs an instance of this reader with the provided project, repo, and ref
     *
     * @param token the github token (optional)
     * @param remoteRef the remote reference
     */
    public BitbucketRepositoryFileReader(final String token, @NonNull final RemoteRef remoteRef) {
        this(token, RemoteRef.Type.BITBUCKET.getBaseUrl(), remoteRef);
    }

    /**
     * Construct the base URL of the Bitbucket repository based on the provided project, repo, and ref
     *
     * @param remoteRef the remote reference
     * @param bitbucketBaseUrl the base URL
     * @return the constructed base URL with a trailing {@value #SEPARATOR}
     */
    public static String constructBaseUrl(final RemoteRef remoteRef, final String bitbucketBaseUrl) {
        val baseUrl = bitbucketBaseUrl.endsWith(SEPARATOR) ? bitbucketBaseUrl.substring(0, bitbucketBaseUrl.length() - 1) : bitbucketBaseUrl;
        return String.format("%s/%s/%s/raw/%s/", baseUrl, remoteRef.getNamespace(), remoteRef.getRepository(), remoteRef.getRef());
    }

}
