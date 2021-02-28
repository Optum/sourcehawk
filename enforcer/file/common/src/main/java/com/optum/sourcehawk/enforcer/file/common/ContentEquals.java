package com.optum.sourcehawk.enforcer.file.common;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.optum.sourcehawk.enforcer.EnforcerResult;
import com.optum.sourcehawk.enforcer.file.AbstractFileEnforcer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.val;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;

/**
 * An enforcer which is responsible for enforcing that file contents match exactly
 *
 * @author Brian Wyka
 */
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = ContentEquals.Builder.class)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ContentEquals extends AbstractFileEnforcer {

    private static final String DEFAULT_MESSAGE = "File contents do not equal that of the expected file contents";

    /**
     * The expected file contents
     */
    private final String expectedFileContents;

    /**
     * The URL to use for comparison
     */
    private final URL expectedUrl;

    /**
     * Creates an instance of this enforcer to compare contents the provided contents
     *
     * @param expectedFileContents the expected file contents
     * @return the instance of this enforcer
     */
    @SuppressWarnings("squid:S1201")
    public static ContentEquals string(final String expectedFileContents) {
        return new ContentEquals(expectedFileContents, null);
    }

    /**
     * Creates an instance of this enforcer to compare contents with URL contents
     *
     * @param expectedUrl the URL in which to read expected content
     * @return the instance of this enforcer
     */
    public static ContentEquals url(final URL expectedUrl) {
        return new ContentEquals(null, expectedUrl);
    }

    /** {@inheritDoc} */
    @Override
    public EnforcerResult enforceInternal(@NonNull final InputStream actualFileInputStream) throws IOException {
        try (val expectedFileContentsReader = new BufferedReader(resolveReader());
             val actualFileContentsReader = new BufferedReader(new InputStreamReader(actualFileInputStream))) {
            if (equals(expectedFileContentsReader, actualFileContentsReader)) {
                return EnforcerResult.passed();
            }
        }
        return EnforcerResult.failed(DEFAULT_MESSAGE);
    }

    /**
     * Resolve the appropriate reader to be used
     *
     * @return the reader of the expected file contents
     * @throws IOException if any error occurs opening file contents from URL
     */
    private Reader resolveReader() throws IOException {
        if (expectedUrl != null) {
            return new InputStreamReader(expectedUrl.openStream());
        } else {
            return new StringReader(expectedFileContents);
        }
    }

    /**
     * Determine if the two buffered readers have identical contents
     *
     * @param expectedReader the expected buffered reader
     * @param actualReader the actual buffered reader
     * @return true if content is identical, false otherwise
     * @throws IOException if any error occurs reading files
     */
    private static boolean equals(final BufferedReader expectedReader, final BufferedReader actualReader) throws IOException {
        if (expectedReader == actualReader) {
            return true;
        }
        String line1 = expectedReader.readLine();
        String line2 = actualReader.readLine();
        while (line1 != null && line1.equals(line2)) {
            line1 = expectedReader.readLine();
            line2 = actualReader.readLine();
        }
        return line1 == null && line2 == null;
    }

}
