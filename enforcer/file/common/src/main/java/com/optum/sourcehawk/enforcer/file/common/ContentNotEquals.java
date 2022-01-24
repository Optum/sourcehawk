package com.optum.sourcehawk.enforcer.file.common;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;

/**
 * An enforcer which is responsible for enforcing that file contents match exactly
 *
 * @author Brian Wyka
 */
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = ContentNotEquals.Builder.class)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class ContentNotEquals extends AbstractContent {

    private static final String DEFAULT_MESSAGE = "File contents do equal that of the expected file contents";

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
    public static ContentNotEquals string(final String expectedFileContents) {
        return new ContentNotEquals(expectedFileContents, null);
    }

    /**
     * Creates an instance of this enforcer to compare contents with URL contents
     *
     * @param expectedUrl the URL in which to read expected content
     * @return the instance of this enforcer
     */
    public static ContentNotEquals url(final URL expectedUrl) {
        return new ContentNotEquals(null, expectedUrl);
    }

    @Override
    protected boolean equals(final BufferedReader expectedReader, final BufferedReader actualReader) throws IOException {
        if (expectedReader == actualReader) {
            return false;
        }
        String line1 = expectedReader.readLine();
        String line2 = actualReader.readLine();
        while (line1 != null && !line1.equals(line2)) {
            line1 = expectedReader.readLine();
            line2 = actualReader.readLine();
        }
        return line1 == null && line2 == null;
    }

    @Override
    protected String getDefaultMessage() {
        return DEFAULT_MESSAGE;
    }
}
