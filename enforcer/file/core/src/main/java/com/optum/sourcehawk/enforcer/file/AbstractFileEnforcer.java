package com.optum.sourcehawk.enforcer.file;

import com.optum.sourcehawk.enforcer.EnforcerResult;
import lombok.NonNull;
import lombok.val;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Abstract enforcer which makes sure that the provided input stream
 * has available content prior to delegating to implementations
 *
 * @author Brian Wyka
 */
public abstract class AbstractFileEnforcer implements FileEnforcer {

    private static final String ERROR_INPUT_STREAM = "Failed to read file with error [%s]";

    /** {@inheritDoc} */
    @Override
    public EnforcerResult enforce(@NonNull final InputStream actualFileInputStream) throws IOException {
        try {
            actualFileInputStream.available();
        } catch (final IOException e) {
            return EnforcerResult.failed(String.format(ERROR_INPUT_STREAM, e));
        }
        return enforceInternal(actualFileInputStream);
    }

    /**
     * Method which should be defined by implementations to perform specific enforcements
     *
     * @param actualFileInputStream the actual file input stream
     * @return the enforcer result
     * @throws IOException if any error occurs processing the input stream
     */
    protected abstract EnforcerResult enforceInternal(@NonNull final InputStream actualFileInputStream) throws IOException;

    /**
     * Convert the input stream to a string
     *
     * @param inputStream the input stream
     * @return the string
     * @throws IOException if any error occurs reading input stream
     */
    protected static String toString(final InputStream inputStream) throws IOException {
        val stringBuilder = new StringBuilder();
        try (val reader = new BufferedReader(new InputStreamReader(inputStream, Charset.defaultCharset()))) {
            int character = 0;
            while ((character = reader.read()) != -1) {
                stringBuilder.append((char) character);
            }
        }
        return stringBuilder.toString();
    }

}
