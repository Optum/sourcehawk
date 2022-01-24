package com.optum.sourcehawk.enforcer.file.common;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.optum.sourcehawk.enforcer.EnforcerResult;
import com.optum.sourcehawk.enforcer.file.AbstractFileEnforcer;
import lombok.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * An enforcer which is responsible for enforcing that file contents match exactly
 *
 * @author Brian Wyka
 */
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = ContentEquals.Builder.class)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class ContentEquals extends AbstractContent {

    private static final String DEFAULT_MESSAGE = "File contents do not equal that of the expected file contents";

    /**
     * The expected file contents
     */
    private final String expectedFileContents;

    /**
     * The URL to use for comparison
     */
    private final URL expectedUrl;

    /**
     * Creates an instance of this enforcer to compare contents the provided contents
     *
     * @param expectedFileContents the expected file contents
     * @return the instance of this enforcer
     */
    @SuppressWarnings("squid:S1201")
    public static ContentEquals string(final String expectedFileContents) {
        return new ContentEquals(expectedFileContents, null);
    }

    /**
     * Creates an instance of this enforcer to compare contents with URL contents
     *
     * @param expectedUrl the URL in which to read expected content
     * @return the instance of this enforcer
     */
    public static ContentEquals url(final URL expectedUrl) {
        return new ContentEquals(null, expectedUrl);
    }

    @Override
    protected String getDefaultMessage() {
        return DEFAULT_MESSAGE;
    }
}
