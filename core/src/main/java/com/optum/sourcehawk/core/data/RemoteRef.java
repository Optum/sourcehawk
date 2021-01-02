package com.optum.sourcehawk.core.data;

import com.optum.sourcehawk.core.utils.StringUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.val;

import java.util.Optional;

/**
 * Remote reference definition
 *
 * @author Brian Wyka
 */
@Value
@Builder
public class RemoteRef {

    private static final char COORDINATES_DELIMITER = '/';
    private static final char REF_DELIMITER = '@';
    private static final String PARSE_ERROR_PREFIX = "Invalid remote reference";

    /**
     * The type of the remote reference
     */
    @NonNull
    Type type;

    /**
     * The remote namespace, such as Github owner / organization, or Bitbucket project
     */
    @NonNull
    String namespace;

    /**
     * The remote repository name
     */
    @NonNull
    String repository;

    /**
     * The reference, such as tag, branch, commit ID
     */
    @NonNull
    String ref;

    /**
     * Create the remote ref from the type and raw reference
     * 
     * @param type the type of the remote ref
     * @param rawRemoteRef the raw remote reference
     * @return the remote reference
     */
    public static RemoteRef parse(final Type type, final String rawRemoteRef) {
        if (rawRemoteRef.indexOf(COORDINATES_DELIMITER) == -1) {
            val message = String.format("%s, must contain '%s' separator between %s and repository", PARSE_ERROR_PREFIX, COORDINATES_DELIMITER, type.getNamespaceType());
            throw new IllegalArgumentException(message);
        }
        val remoteRefBuilder = builder();
        final String rawCoordinates;
        if (rawRemoteRef.indexOf(REF_DELIMITER) == -1) {
            rawCoordinates = rawRemoteRef;
            remoteRefBuilder.ref(type.getDefaultBranch());
        } else {
            val refDelimiterIndex= rawRemoteRef.indexOf(REF_DELIMITER);
            rawCoordinates = rawRemoteRef.substring(0, refDelimiterIndex);
            val ref = Optional.of(rawRemoteRef.substring(refDelimiterIndex + 1))
                    .filter(StringUtils::isNotBlankOrEmpty)
                    .orElseThrow(() -> new IllegalArgumentException(String.format("%s, ref must be provided after '%s'", PARSE_ERROR_PREFIX, REF_DELIMITER)));
            remoteRefBuilder.ref(ref);
        }
        val coordinates = rawCoordinates.split(String.valueOf(COORDINATES_DELIMITER));
        if (coordinates.length < 2) {
            throw new IllegalArgumentException(PARSE_ERROR_PREFIX + ", repository must not be empty");
        }
        return remoteRefBuilder.type(type)
                .namespace(coordinates[0])
                .repository(coordinates[1])
                .build();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format("[%s] %s%s%s%s%s", type.name(), namespace, COORDINATES_DELIMITER, repository, REF_DELIMITER, ref);
    }

    /**
     * The type of the remote reference
     * 
     * @author Brian Wyka
     */
    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public enum Type {
        
        BITBUCKET("https://bitbucket.org", "master", "project"),
        GITHUB("https://raw.githubusercontent.com", "main", "owner");

        /**
         * The base URL
         */
        private final String baseUrl;

        /**
         * The name of the default branch
         */
        private final String defaultBranch;

        /**
         * The namespace type
         */
        private final String namespaceType;
        
    }

}
