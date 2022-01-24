package com.optum.sourcehawk.core.repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * A reader responsible for reading repository files
 *
 * @author Brian Wyka
 */
public interface RepositoryFileReader {

    /**
     *  Determine if the reader supports glob patterns
     *
     * @return true if reader supports glob patterns, false otherwise
     */
    default boolean supportsGlobPatterns() {
        return false;
    }

    /**
     * Determine if the file at the given path exists
     *
     * @param repositoryFilePath the repository file path
     * @return true if file exists, false otherwise
     * @throws IOException if any error occurs determining if file exists
     */
    boolean exists(final String repositoryFilePath) throws IOException;

    /**
     * Read a file from the given repository file path
     *
     * @param repositoryFilePath the repository file path
     * @return the {@link InputStream} reference if it exists, {@link Optional#empty()} otherwise
     * @throws IOException if any error occurs obtaining the input stream
     */
    Optional<InputStream> read(final String repositoryFilePath) throws IOException;

    /**
     * Get a string representation of the absolute location of {@code repositoryFilePath}
     *
     * @param repositoryFilePath the repository file path
     * @return the absolute location
     */
    String getAbsoluteLocation(final String repositoryFilePath);

}
