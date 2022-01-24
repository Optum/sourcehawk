package com.optum.sourcehawk.enforcer.file.common;

import com.optum.sourcehawk.enforcer.EnforcerResult;
import com.optum.sourcehawk.enforcer.file.AbstractFileEnforcer;
import lombok.NonNull;
import lombok.val;

import java.io.IOException;
import java.io.InputStream;

/**
 * An abstract class for contains to allow different types of operations
 *
 * @author Christian Oestreich
 */
public abstract class AbstractContains extends AbstractFileEnforcer {

    /**
     * {@inheritDoc}
     */
    @Override
    public EnforcerResult enforceInternal(@NonNull final InputStream actualFileInputStream) throws IOException {
        val actualFileContent = toString(actualFileInputStream);
        if (passes(actualFileContent)) {
            return EnforcerResult.failed(String.format(getMessageTemplate(), getExpectedSubstring()));
        }
        return EnforcerResult.passed();
    }

    protected boolean passes(String actualFileContent) {
        return !actualFileContent.contains(getExpectedSubstring());
    }

    protected abstract String getExpectedSubstring();

    protected abstract String getMessageTemplate();
}