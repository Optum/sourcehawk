package com.optum.sourcehawk.core.scan;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * The result of a fix
 *
 * @author Christian Oestreich
 */
@Value
@Builder
public class FlattenResult implements Serializable {

    private static final long serialVersionUID = -7826662019832933150L;

    @Builder.Default
    byte[] content = null;

    /**
     * Whether or not an error occurred during fix
     */
    @Builder.Default
    boolean error = false;

    /**
     * The number of errors which occurred
     */
    @Builder.Default
    int errorCount = 0;

    /**
     * All of the messages associated with the scan
     * <p>
     * Key: Repository File Path
     * Value: Collection of {@link ScanResult.MessageDescriptor}
     */
    @NonNull
    @Builder.Default
    @SuppressWarnings("squid:S1948") // Lombok generates private modifier
            Map<String, Collection<MessageDescriptor>> messages = Collections.emptyMap();

    /**
     * Messages formatted for reporting
     * <p>
     * Format: [SEVERITY] repositoryFilePath :: message
     */
    @NonNull
    @Builder.Default
    @SuppressWarnings("squid:S1948") // Lombok generates private modifier
            Collection<String> formattedMessages = Collections.emptyList();


    /**
     * Constructs a "passed" instance of {@link ScanResult}
     *
     * @return the scan result
     */
    public static FlattenResult success(final byte[] content) {
        return new FlattenResult(content, false, 0, Collections.emptyMap(), Collections.singletonList("Flatten successful"));
    }


    /**
     * Encapsulates all of the traits of a message
     *
     * @author Brian Wyka
     */
    @Value
    @Builder
    public static class MessageDescriptor {

        @NonNull String repositoryPath;
        @NonNull String message;

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return String.format("%s :: %s", repositoryPath, message);
        }

    }

}
