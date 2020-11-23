package com.optum.sourcehawk.enforcer.file.common;

import com.optum.sourcehawk.core.utils.StringUtils;
import com.optum.sourcehawk.enforcer.EnforcerResult;
import com.optum.sourcehawk.enforcer.ResolverResult;
import com.optum.sourcehawk.enforcer.file.AbstractFileEnforcer;
import com.optum.sourcehawk.enforcer.file.FileResolver;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.val;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * An enforcer which is responsible for enforcing that file contains an entire line at a specific line number.
 *
 * @author Brian Wyka
 */
@AllArgsConstructor(staticName = "containsAt")
public class ContainsLineAt extends AbstractFileEnforcer implements FileResolver {

    private static final String MESSAGE_TEMPLATE = "File does not contain the line [%s] at line number [%d]";
    private static final String UPDATE_MESSAGE_TEMPLATE = "File line number [%d] has been updated to value [%s]";

    /**
     * The line that is expected to be found in the file
     */
    protected final String expectedLine;

    /**
     * The line number in which the line is expected to be found within the file
     */
    protected final int expectedLineNumber;

    /** {@inheritDoc} */
    @Override
    public EnforcerResult enforceInternal(@NonNull final InputStream actualFileInputStream) throws IOException {
        return enforceLineAt(actualFileInputStream, expectedLineNumber, getPredicate())
                .orElseGet(() -> EnforcerResult.failed(String.format(MESSAGE_TEMPLATE, expectedLine, expectedLineNumber)));
    }

    private Predicate<String> getPredicate() {
        return actual -> StringUtils.equals(StringUtils.removeNewLines(expectedLine), StringUtils.removeNewLines(actual));
    }

    /**
     * Enforce that the {@code matchPredicate} holds true for the provided {@code fileInputStream} and {@code expectedLineNumber}
     *
     * @param fileInputStream the file input stream
     * @param expectedLineNumber the expected line number
     * @param matchPredicate the match predicate to test the line with
     * @return the enforcer result if match found, otherwise {@link Optional#empty()}
     * @throws IOException if any error occurs reading the input stream
     */
    static Optional<EnforcerResult> enforceLineAt(final InputStream fileInputStream, final int expectedLineNumber, final Predicate<String> matchPredicate) throws IOException {
        try (val bufferedFileReader = new BufferedReader(new InputStreamReader(fileInputStream))) {
            String line;
            int lineNumber = 1;
            while (((line = bufferedFileReader.readLine()) != null) && (lineNumber <= expectedLineNumber)) {
                if (lineNumber == expectedLineNumber) {
                    break;
                }
                lineNumber++;
            }
            if (matchPredicate.test(line)) {
                return Optional.of(EnforcerResult.passed());
            }
        }
        return Optional.empty();
    }

    /** {@inheritDoc} */
    @Override
    public ResolverResult resolve(final @NonNull InputStream fileInputStream, final @NonNull Writer output) throws IOException {
        val resolverResultBuilder = ResolverResult.builder();
        val predicate = getPredicate();
        try (val bufferedFileReader = new BufferedReader(new InputStreamReader(fileInputStream))) {
            String line;
            int lineNumber = 1;
            while (((line = bufferedFileReader.readLine()) != null) && (lineNumber <= expectedLineNumber)) {
                if (lineNumber == expectedLineNumber && !predicate.test(line)) {
                    output.write(expectedLine);
                    resolverResultBuilder.updatesApplied(true)
                            .messages(Collections.singleton(String.format(UPDATE_MESSAGE_TEMPLATE, expectedLineNumber, expectedLine)));
                } else {
                    output.write(line);
                }
                output.write(System.lineSeparator());
                lineNumber++;
            }
        }
        return resolverResultBuilder.build();
    }

}
