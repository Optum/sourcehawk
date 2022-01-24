package com.optum.sourcehawk.enforcer.file.common;

import com.optum.sourcehawk.enforcer.EnforcerResult;
import com.optum.sourcehawk.enforcer.file.AbstractFileEnforcer;
import lombok.NonNull;
import lombok.val;

import java.io.*;
import java.net.URL;

public abstract class AbstractContent extends AbstractFileEnforcer {

    /**
     * The expected file contents
     */
    protected abstract String getExpectedFileContents();

    /**
     * The URL to use for comparison
     */
    protected abstract URL getExpectedUrl();

    protected abstract String getDefaultMessage();

    /**
     * {@inheritDoc}
     */
    @Override
    public EnforcerResult enforceInternal(@NonNull final InputStream actualFileInputStream) throws IOException {
        try (val expectedFileContentsReader = new BufferedReader(resolveReader());
             val actualFileContentsReader = new BufferedReader(new InputStreamReader(actualFileInputStream))) {
            if (equals(expectedFileContentsReader, actualFileContentsReader)) {
                return EnforcerResult.passed();
            }
        }
        return EnforcerResult.failed(getDefaultMessage());
    }

    /**
     * Resolve the appropriate reader to be used
     *
     * @return the reader of the expected file contents
     * @throws IOException if any error occurs opening file contents from URL
     */
    private Reader resolveReader() throws IOException {
        if (getExpectedUrl() != null) {
            return new InputStreamReader(getExpectedUrl().openStream());
        } else {
            return new StringReader(getExpectedFileContents());
        }
    }

    /**
     * Determine if the two buffered readers have identical contents
     *
     * @param expectedReader the expected buffered reader
     * @param actualReader   the actual buffered reader
     * @return true if content is identical, false otherwise
     * @throws IOException if any error occurs reading files
     */
    protected boolean equals(final BufferedReader expectedReader, final BufferedReader actualReader) throws IOException {
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
