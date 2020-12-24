package com.optum.sourcehawk.core.repository;

import com.optum.sourcehawk.core.utils.StringUtils;
import lombok.NonNull;
import lombok.val;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link RemoteRepositoryFileReader} implementation which reads files from remote Github repositories
 *
 * @author Brian Wyka
 */
public class GithubRepositoryFileReader extends RemoteRepositoryFileReader {

    /**
     * The default public Github raw URL
     */
    public static final String DEFAULT_BASE_URL = "https://raw.githubusercontent.com";

    /**
     * Constructs an instance of this reader with the provided Github Enterprise URL
     *
     * @param token the github token (optional)
     * @param githubEnterpriseUrl the Github enterprise URL
     * @param owner the Github owner
     * @param repo the Github repository
     * @param ref the Github ref, i.e. - main, v2.3, ab436dea2, etc...
     */
    public GithubRepositoryFileReader(final String token, @NonNull final String githubEnterpriseUrl, @NonNull final String owner,
                                      @NonNull final String repo, @NonNull final String ref) {
        super(constructBaseUrl(githubEnterpriseUrl, true, owner, repo, ref), constructRequestProperties(token));
    }

    /**
     * Constructs an instance of this reader with the provided owner, repo, and ref
     *
     * @param token the github token (optional)
     * @param owner the Github owner
     * @param repo the Github repository
     * @param ref the Github ref, i.e. - main, v2.3, ab436dea2, etc...
     */
    public GithubRepositoryFileReader(final String token, @NonNull final String owner, @NonNull final String repo, @NonNull final String ref) {
        super(constructBaseUrl(DEFAULT_BASE_URL, false, owner, repo, ref), constructRequestProperties(token));
    }

    /**
     * Construct the base URL of the Github repository based on the provided owner, repo, and ref
     *
     * @param githubUrl the Github URL
     * @param githubEnterprise true if Github Enterprise, false otherwise
     * @param owner the Github owner
     * @param repo the Github repo
     * @param ref the Github ref
     * @return the constructed base URL with a trailing {@value #SEPARATOR}
     */
    public static String constructBaseUrl(final String githubUrl, final boolean githubEnterprise, final String owner, final String repo, final String ref) {
        final String baseUrl;
        if (githubUrl.endsWith(SEPARATOR)) {
            baseUrl = githubUrl;
        } else {
            baseUrl = githubUrl + SEPARATOR;
        }
        final String githubBaseUrl;
        if (githubEnterprise) {
            githubBaseUrl = baseUrl + "raw" + SEPARATOR;
        } else {
            githubBaseUrl = baseUrl;
        }
        return githubBaseUrl + owner + SEPARATOR + repo + SEPARATOR + ref + SEPARATOR;
    }

    /**
     * Construct the request properties for the provided github token
     *
     * @param githubToken the github token
     * @return the request properties
     */
    private static Map<String, String> constructRequestProperties(final String githubToken) {
        val requestProperties = new HashMap<String, String>();
        requestProperties.put("Accept", "text/plain");
        if (StringUtils.isNotBlankOrEmpty(githubToken)) {
            requestProperties.put("Authorization", String.format("token %s", githubToken));
        }
        return requestProperties;
    }

}
