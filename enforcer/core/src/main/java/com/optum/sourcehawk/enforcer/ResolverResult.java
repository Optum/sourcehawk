package com.optum.sourcehawk.enforcer;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.val;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * The result of a resolver resolution
 *
 * @author Brian Wyka
 */
@Value
@Builder
public class ResolverResult {

    /**
     * A "No Updates" instance of the result
     */
    public static final ResolverResult NO_UPDATES = builder().build();

    /**
     * Whether or not the updated have been applied
     */
    @Builder.Default
    boolean updatesApplied = false;

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
     * A message associated with the result
     */
    @NonNull
    @Builder.Default
    Collection<String> messages = Collections.emptySet();

    /**
     * Creates an "updates applied" instance of the result with the single provided message
     *
     * @param message the message pertaining to fixes applied
     * @return the resolver result
     */
    public static ResolverResult updatesApplied(final String message) {
        return builder().updatesApplied(true).fixCount(1).messages(Collections.singleton(message)).build();
    }

    /**
     * Creates an "error" instance of the result with the single provided message
     *
     * @param message the message pertaining to fixes applied
     * @return the resolver result
     */
    public static ResolverResult error(final String message) {
        return builder().updatesApplied(false).error(true).errorCount(1).messages(Collections.singleton(message)).build();
    }

    /**
     * Reduce two {@link ResolverResult}s into one
     *
     * @param one the first resolver result
     * @param two the second resolver result
     * @return the reduced resolver result
     */
    public static ResolverResult reduce(final ResolverResult one, final ResolverResult two) {
        val messages = new HashSet<>(one.messages);
        messages.addAll(two.messages);
        if (messages.size() < (one.messages.size() + two.messages.size())) {
            return one; // They are the same
        }
        return ResolverResult.builder()
                .updatesApplied(one.updatesApplied || two.updatesApplied)
                .fixCount(one.fixCount + two.fixCount)
                .error(one.error && two.error)
                .errorCount(one.errorCount + two.errorCount)
                .messages(messages)
                .build();
    }

}
