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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * An enforcer which is responsible for enforcing that SHA-256 checksum of a file's contents match
 *
 * @author Brian Wyka
 */
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = Sha256ChecksumEquals.Builder.class)
@AllArgsConstructor(staticName = "equals")
public class Sha256ChecksumEquals extends AbstractFileEnforcer {

    private static final String ALGORITHM = "SHA-256";
    private static final String DEFAULT_MESSAGE = "The SHA-256 checksum of the file does not match";

    /**
     * The expected checksum
     */
    private final String expectedChecksum;

    /** {@inheritDoc} */
    @Override
    public EnforcerResult enforceInternal(@NonNull final InputStream actualFileInputStream) throws IOException {
        val actualChecksum = checksum(actualFileInputStream);
        if (expectedChecksum.equals(actualChecksum)) {
            return EnforcerResult.passed();
        }
        return EnforcerResult.failed(DEFAULT_MESSAGE);
    }

    private static String checksum(final InputStream inputStream) throws IOException {
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(ALGORITHM);
        } catch (final NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
        val fileContents = toString(inputStream);
        val encodedHashBytes = digest.digest(fileContents.getBytes(StandardCharsets.UTF_8));
        val hexStringBuilder = new StringBuilder();
        for (val encodedHashByte : encodedHashBytes) {
            val hex = Integer.toHexString(0xff & encodedHashByte);
            if (hex.length() == 1) {
                hexStringBuilder.append('0');
            }
            hexStringBuilder.append(hex);
        }
        return hexStringBuilder.toString();
    }

}
