package com.optum.sourcehawk.enforcer.file.common;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.optum.sourcehawk.enforcer.EnforcerResult;
import com.optum.sourcehawk.enforcer.file.AbstractFileEnforcer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.val;

import java.io.IOException;
import java.io.InputStream;

/**
 * An enforcer which is responsible for enforcing that file contains a string
 *
 * @author Brian Wyka
 */
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = Contains.Builder.class)
@AllArgsConstructor(staticName = "substring")
public class Contains extends AbstractFileEnforcer {

    private static final String MESSAGE_TEMPLATE = "File does not contain the sub string [%s]";

    /**
     * The substring that is expected to be found in the file
     */
    protected final String expectedSubstring;


    /** {@inheritDoc} */
    @Override
    public EnforcerResult enforceInternal(@NonNull final InputStream actualFileInputStream) throws IOException {
        val actualFileContent = toString(actualFileInputStream);
        if (!actualFileContent.contains(expectedSubstring)) {
            return EnforcerResult.failed(String.format(MESSAGE_TEMPLATE, expectedSubstring));
        }
        return EnforcerResult.passed();
    }

}