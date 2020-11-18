package com.optum.sourcehawk.core.scan;

import com.optum.sourcehawk.core.utils.MapUtils;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.val;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

/**
 * The result of a fix
 *
 * @author Brian Wyka
 */
@Value
@Builder
public class FixResult implements Serializable {

    private static final long serialVersionUID = -7826662019832933150L;

    /**
     * Whether or not there was a resolver to use
     */
    @Builder.Default
    boolean noResolver = false;

    /**
     * Whether or not fixes were applied
     */
    @Builder.Default
    boolean fixesApplied = false;

    /**
     * Whether or not an error occurred during fix
     */
    @Builder.Default
    boolean error = false;

    /**
     * The number of fixes made
     */
    @Builder.Default
    int fixCount = 0;

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
     * Reduce two {@link FixResult}s into one
     *
     * @param one the first fix result
     * @param two the second fix result
     * @return the reduced fix result
     */
    public static FixResult reduce(final FixResult one, final FixResult two) {
        if (one == null) {
            return two != null ? two : FixResult.builder()
                    .error(true)
                    .errorCount(1)
                    .formattedMessages(Collections.singletonList("Fix results appear to be empty"))
                    .build();
        }
        if (two == null) {
            return one;
        }
        val formattedMessages = new HashSet<>(one.formattedMessages);
        formattedMessages.addAll(two.formattedMessages);
        if (formattedMessages.size() < (one.formattedMessages.size() + two.formattedMessages.size())) {
            return one; // They are the same
        }
        return FixResult.builder()
                .fixesApplied(one.fixesApplied || two.fixesApplied)
                .fixCount(one.fixCount + two.fixCount)
                .noResolver(one.noResolver || two.noResolver)
                .error(one.error && two.error)
                .errorCount(one.errorCount + two.errorCount)
                .messages(MapUtils.mergeCollectionValues(one.messages, two.messages))
                .formattedMessages(formattedMessages)
                .build();
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
