package com.optum.sourcehawk.enforcer.file.common;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * An enforcer which is responsible for enforcing that file contains a string
 *
 * @author Brian Wyka
 * @author Christian Oestreich
 */
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = Contains.Builder.class)
@AllArgsConstructor(staticName = "substring")
@Getter
public class Contains extends AbstractContains {

    private static final String MESSAGE_TEMPLATE = "File does not contain the sub string [%s]";

    /**
     * The substring that is expected to be found in the file
     */
    protected final String expectedSubstring;

    protected boolean passes(String actualFileContent) {
        return !actualFileContent.contains(getExpectedSubstring());
    }

    @Override
    protected String getMessageTemplate() {
        return MESSAGE_TEMPLATE;
    }
}