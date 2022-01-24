package com.optum.sourcehawk.enforcer.file.common;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.optum.sourcehawk.enforcer.EnforcerResult;
import com.optum.sourcehawk.enforcer.file.AbstractFileEnforcer;
import lombok.*;

import java.io.IOException;
import java.io.InputStream;

/**
 * An enforcer which is responsible for enforcing that file contains a string
 *
 * @author Christian Oestreich
 */
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = NotContains.Builder.class)
@AllArgsConstructor(staticName = "substring")
@Getter
public class NotContains extends AbstractContains {

    private static final String MESSAGE_TEMPLATE = "File does contain the sub string [%s]";

    protected final String expectedSubstring;

    protected boolean passes(String actualFileContent) {
        return actualFileContent.contains(expectedSubstring);
    }

    @Override
    protected String getMessageTemplate() {
        return MESSAGE_TEMPLATE;
    }
}