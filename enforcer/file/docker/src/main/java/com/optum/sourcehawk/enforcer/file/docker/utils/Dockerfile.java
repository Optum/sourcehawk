package com.optum.sourcehawk.enforcer.file.docker.utils;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;

/**
 * Dockerfile representations
 *
 * @author Brian Wyka
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Dockerfile {

    /**
     * Representation of a FROM token in a Dockerfile
     *
     * @author Brian Wyka
     */
    @Value
    @Builder
    public static class FromToken {

        /**
         * Default tag resolution if none provided
         */
        public static final String TAG_DEFAULT = "latest";

        /**
         * The raw value of the token
         */
        @NonNull
        String rawValue;

        /**
         * The registry host (optional)
         */
        String registryHost;

        /**
         * The image (required)
         */
        @NonNull
        String image;

        /**
         * The tag (optional)
         */
        String tag;

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return rawValue;
        }

    }

}
