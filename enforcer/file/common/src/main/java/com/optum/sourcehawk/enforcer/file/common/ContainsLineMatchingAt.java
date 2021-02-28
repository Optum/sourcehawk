package com.optum.sourcehawk.enforcer.file.common;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.optum.sourcehawk.enforcer.EnforcerResult;
import com.optum.sourcehawk.enforcer.file.AbstractFileEnforcer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

/**
 * An enforcer which is responsible for enforcing that file contains an entire line matching a pattern at a specific line number
 *
 * @author Brian Wyka
 */
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = ContainsLineMatchingAt.Builder.class)
@AllArgsConstructor(staticName = "containsMatchAt")
public class ContainsLineMatchingAt extends AbstractFileEnforcer {

    private static final String MESSAGE_TEMPLATE = "File does not contain line matching pattern [%s] at line number [%d]";

    /**
     * The pattern that the line is expected to match in the file
     */
    protected final Pattern expectedLinePattern;

    /**
     * The line number in which the line is expected to be found within the file
     */
    protected final int expectedLineNumber;

    /** {@inheritDoc} */
    @Override
    public EnforcerResult enforceInternal(@NonNull final InputStream actualFileInputStream) throws IOException {
        return ContainsLineAt.enforceLineAt(actualFileInputStream, expectedLineNumber, line -> (line != null) && expectedLinePattern.matcher(line).matches())
                .orElseGet(() -> EnforcerResult.failed(String.format(MESSAGE_TEMPLATE, expectedLinePattern.pattern(), expectedLineNumber)));
    }

}
