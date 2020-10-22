package com.optum.sourcehawk.enforcer.file.common;

import com.optum.sourcehawk.core.utils.StringUtils;
import com.optum.sourcehawk.enforcer.file.AbstractFileEnforcer;
import com.optum.sourcehawk.enforcer.EnforcerResult;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.val;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * An enforcer which is responsible for enforcing that file contains an entire line
 *
 * @author Brian Wyka
 */
@AllArgsConstructor(staticName = "contains")
public class ContainsLine extends AbstractFileEnforcer {

    private static final String MESSAGE_TEMPLATE = "File does not contain the line [%s]";

    /**
     * The line that is expected to be found in the file
     */
    protected final String expectedLine;

    /** {@inheritDoc} */
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