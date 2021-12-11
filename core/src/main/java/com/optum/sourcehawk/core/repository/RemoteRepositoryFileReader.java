package com.optum.sourcehawk.core.repository;

import com.optum.sourcehawk.core.utils.StringUtils;
import lombok.NonNull;
import lombok.val;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A remote repository file reader which treats the repository file paths relative
 * to the raw file URL template provided during construction
 *
 * @author Brian Wyka
 */
public final class RemoteRepositoryFileReader implements RepositoryFileReader {

    /**
     * URL Separator
     */
    private static final String SEPARATOR = "/";

    /**
     * The raw file URL template.  Takes one parameter: The path of the file in the repository
     */
    private final String rawFileUrlTemplate;

    /**
     * The required request properties
     */
    private final Map<String, String> requestProperties;

    /**
     * Map of URLs to cache existence
     */
    private final Map<String, Boolean> urlExistenceCache = new HashMap<>();

    /**
     * Constructs an instance of this reader with the provided base URL
     *
     * @param rawFileUrlTemplate the raw file URL template
     * @param requestProperties the request properties required for connection
     */
    public RemoteRepositoryFileReader(@NonNull final String rawFileUrlTemplate, final Map<String, String> requestProperties) {
        this.rawFileUrlTemplate = rawFileUrlTemplate;
        this.requestProperties = requestProperties;
    }

    /**
     * Constructs an instance of this reader with the provided base URL
     *
     * @param rawFileUrlTemplate the base URL
     */
    protected RemoteRepositoryFileReader(@NonNull final String rawFileUrlTemplate) {
        this(rawFileUrlTemplate, Collections.emptyMap());
    }

    /** {@inheritDoc} */
    @Override
    public boolean exists(final String repositoryFilePath) throws IOException {
        val absoluteUrl = new URL(constructAbsoluteLocation(rawFileUrlTemplate, repositoryFilePath));
        val absoluteUrlString = absoluteUrl.toString();
        if (urlExistenceCache.containsKey(absoluteUrlString)) {
            return urlExistenceCache.get(absoluteUrlString);
        }
        val exists = urlExists(absoluteUrl);
        urlExistenceCache.put(absoluteUrlString, exists);
        return exists;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<InputStream> read(final String repositoryFilePath) throws IOException {
        val absoluteUrl = new URL(constructAbsoluteLocation(rawFileUrlTemplate, repositoryFilePath));
        if (exists(repositoryFilePath)) {
            val httpUrlConnection = (HttpURLConnection) absoluteUrl.openConnection();
            requestProperties.forEach(httpUrlConnection::setRequestProperty);
            return getInputStream(httpUrlConnection);
        }
        return Optional.empty();
    }

    /** {@inheritDoc} */
    @Override
    public String getAbsoluteLocation(final String repositoryFilePath) {
        return constructAbsoluteLocation(rawFileUrlTemplate, repositoryFilePath);
    }

    /**
     * Get the input stream from the {@link HttpURLConnection}
     *
     * @param httpUrlConnection the HTTP URL connection
     * @return the input stream if present
     * @throws IOException if any error occurs opening the input stream
     */
    private static Optional<InputStream> getInputStream(final HttpURLConnection httpUrlConnection) throws IOException {
        try {
            return Optional.of(httpUrlConnection.getInputStream());
        } catch (final FileNotFoundException ignored) {
            return Optional.empty();
        }
    }

    /**
     * Determine if the URL exists by sending a HEAD request to it
     *
     * @param url the URL to check
     * @return tre if exists, false otherwise
     * @throws IOException if any error occurs opening connection to URL or retrieving response code
     */
    private boolean urlExists(final URL url) throws IOException {
        val httpUrlConnection = (HttpURLConnection) url.openConnection();
        httpUrlConnection.setRequestMethod("HEAD");
        requestProperties.forEach(httpUrlConnection::setRequestProperty);
        return httpUrlConnection.getResponseCode() == HttpURLConnection.HTTP_OK;
    }

    /**
     * Construct the absolute location to the remote file
     *
     * @param rawFileUrlTemplate the raw file URL template
     * @param repositoryFilePath the repository file path
     * @return the absolute location to the repository file
     */
    private static String constructAbsoluteLocation(final String rawFileUrlTemplate, final String repositoryFilePath) {
        if (repositoryFilePath.startsWith(SEPARATOR)) {
            return String.format(rawFileUrlTemplate, repositoryFilePath.substring(1));
        }
        return String.format(rawFileUrlTemplate, repositoryFilePath);
    }

    /**
     * Construct the request properties for the provided authorization token
     *
     * @param authorizationPrefix the authorization request property prefix
     * @param authorizationToken the authorization token
     * @return the request properties
     */
    protected static Map<String, String> constructRequestProperties(final String authorizationPrefix, final String authorizationToken) {
        val requestProperties = new HashMap<String, String>();
        requestProperties.put("Accept", "text/plain");
        if (StringUtils.isNotBlankOrEmpty(authorizationToken)) {
            requestProperties.put("Authorization", authorizationPrefix + authorizationToken);
        }
        return requestProperties;
    }

}
