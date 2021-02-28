package com.optum.sourcehawk.enforcer.file.common;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.optum.sourcehawk.enforcer.EnforcerResult;
import com.optum.sourcehawk.enforcer.file.AbstractFileEnforcer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.val;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

/**
 * An enforcer which is responsible for enforcing that file contains an entire line matching a pattern
 *
 * @author Brian Wyka
 */
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = ContainsLineMatching.Builder.class)
@AllArgsConstructor(staticName = "containsMatch")
public class ContainsLineMatching extends AbstractFileEnforcer {

    private static final String MESSAGE_TEMPLATE = "File does not contain line matching pattern [%s]";

    /**
     * The pattern for which a line should match in the file
     */
    protected final Pattern expectedLinePattern;

    /** {@inheritDoc} */
    @Override
    public EnforcerResult enforceInternal(@NonNull final InputStream actualFileInputStream) throws IOException {
        try (val bufferedFileReader = new BufferedReader(new InputStreamReader((actualFileInputStream)))) {
            String line;
            while ((line = bufferedFileReader.readLine()) != null) {
                if (expectedLinePattern.matcher(line).matches()) {
                    return EnforcerResult.passed();
                }
            }
        }
        return EnforcerResult.failed(String.format(MESSAGE_TEMPLATE, expectedLinePattern.pattern()));
    }

}
