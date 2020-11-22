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

    /**
     * Parse the from token into an object
     *
     *       docker.io/image:1.0.0
     *       image:1.0.0
     *       image
     *       org/image:1.0.0
     *       docker.io/org/image
     *       docker.io/org/image:1.0.0
     *
     * @param fromToken the from token string
     * @return the from token object
     */
    public static Dockerfile.FromToken parseFromToken(final String fromToken) {
        val builder = Dockerfile.FromToken.builder().rawValue(fromToken);
        val firstForwardSlashIndex = fromToken.indexOf('/');
        int startIndex = 0;
        if (firstForwardSlashIndex > -1) {
            val firstSegment = fromToken.substring(0, firstForwardSlashIndex);
            if (firstSegment.contains(".") || firstSegment.contains(":")) {
                builder.registryHost(firstSegment);
                startIndex = firstForwardSlashIndex + 1;
            }
        }
        val image = fromToken.substring(startIndex);
        val imagePieces = image.split(":");
        if (imagePieces.length == 1) {
            builder.image(image);
        } else {
            builder.image(imagePieces[0]);
            builder.tag(image.substring(image.indexOf(':') + 1));
        }
        return builder.build();
    }

}
