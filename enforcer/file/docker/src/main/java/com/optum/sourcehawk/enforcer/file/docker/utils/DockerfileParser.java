package com.optum.sourcehawk.enforcer.file.docker.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A parser for Dockerfiles
 *
 * @author Brian Wyka
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DockerfileParser {

    public static final String FROM_TOKEN = "FROM ";
    public static final String FROM_SCRATCH = "scratch";

    /**
     * Collect the values of all lines for the given token
     *
     * @param fileInputStream the file input stream
     * @param token the token to collect
     * @return the token values
     * @throws IOException if any error occurs during file parsing
     */
    public static Collection<String> collectTokenValues(final InputStream fileInputStream, final String token) throws IOException {
        try (val dockerfileReader = new BufferedReader(new InputStreamReader(fileInputStream))) {
            String line;
            val tokenValues = new ArrayList<String>();
            while ((line = dockerfileReader.readLine()) != null) {
                if (line.startsWith(token)) {
                    tokenValues.add(line.substring(token.length()));
                }
            }
            return tokenValues;
        }
    }

}
