package com.optum.sourcehawk.core.repository;

import java.io.IOException;

/**
 * A reader responsible for reading repository files
 *
 * @author Christian Oestreich
 */
public interface RepositoryFileWriter {

    /**
     * Read a file from the given repository file path
     *
     * @param repositoryFilePath the repository file path
     * @param content            the output stream to write
     * @throws IOException if any error occurs obtaining the input stream
     */
    void write(final String repositoryFilePath, final byte[] content) throws IOException;

}
