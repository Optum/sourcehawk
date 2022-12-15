package com.optum.sourcehawk.enforcer.file.common;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.optum.sourcehawk.core.utils.StringUtils;
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
 * An enforcer which is responsible for enforcing that file contains an entire line
 *
 * @author Brian Wyka
 */
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = ContainsLine.Builder.class)
@AllArgsConstructor(staticName = "contains")
public class ContainsLine extends AbstractFileEnforcer {

    private static final String MESSAGE_TEMPLATE = "File contains line [%s] failed";

    /**
     * The line that is expected to be found in the file
     */
    protected final String expectedLine;

    /**
     * {@inheritDoc}
     */
    @Override
    public EnforcerResult enforceInternal(@NonNull final InputStream actualFileInputStream) throws IOException {
        try (val bufferedFileReader = new BufferedReader(new InputStreamReader((actualFileInputStream)))) {
            String line;
            while ((line = bufferedFileReader.readLine()) != null) {
                if (StringUtils.equals(StringUtils.removeNewLines(expectedLine), line)) {
                    return EnforcerResult.passed();
                }
            }
        }
        return EnforcerResult.failed(String.format(MESSAGE_TEMPLATE, expectedLine));
    }

}