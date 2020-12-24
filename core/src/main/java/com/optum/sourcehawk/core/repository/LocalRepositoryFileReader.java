package com.optum.sourcehawk.core.repository;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * A repository file reader implementation which reads from file system
 *
 * @author Brian Wyka
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LocalRepositoryFileReader implements RepositoryFileReader {

    /**
     * The directory in which repository files should be read from
     */
    private final Path directory;

    /**
     * Creates an instance of the repository file reader with the provided directory context
     *
     * @param directory the directory context (root of repository)
     * @return the repository file reader
     */
    public static LocalRepositoryFileReader create(@NonNull final Path directory) {
        return new LocalRepositoryFileReader(directory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists(final String repositoryFilePath) {
        return Files.exists(directory.resolve(Paths.get(repositoryFilePath)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<InputStream> read(@NonNull final String repositoryFilePath) throws IOException {
        return getInputStream(directory.resolve(Paths.get(repositoryFilePath)));
    }

    /**
     * Get the {@link InputStream} from the {@link File} reference
     *
     * @param repositoryFilePath the file
     * @return the input stream if found, otherwise {@link Optional#empty()}
     * @throws IOException if any error occurs reading file
     */
    private static Optional<InputStream> getInputStream(final Path repositoryFilePath) throws IOException {
        if (Files.exists(repositoryFilePath)) {
            return Optional.of(Files.newInputStream(repositoryFilePath));
        }
        return Optional.empty();
    }

}
