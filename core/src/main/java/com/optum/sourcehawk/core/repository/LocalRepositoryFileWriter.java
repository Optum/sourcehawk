package com.optum.sourcehawk.core.repository;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * A repository file reader implementation which reads from file system
 *
 * @author Christian Oestreich
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LocalRepositoryFileWriter implements RepositoryFileWriter {

    /**
     * Creates an instance of the repository file reader with the provided directory context
     *
     * @return the repository file reader
     */
    public static LocalRepositoryFileWriter writer() {
        return new LocalRepositoryFileWriter();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(@NonNull final String repositoryFilePath, @NonNull final byte[] content) throws IOException {
        Files.write(Paths.get(repositoryFilePath), content);
    }

}
