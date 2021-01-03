package com.optum.sourcehawk.core.result;

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
 * The result of a scan
 *
 * @author Brian Wyka
 */
@Value
@Builder
public class ScanResult implements Serializable {

    private static final long serialVersionUID = 187759610747848247L;

    /**
     * Whether or not the scan passed
     */
    @Builder.Default
    boolean passed = false;

    /**
     * The number of errors encountered during the scan
     */
    @Builder.Default
    int errorCount = 0;

    /**
     * The number of warnings encountered during the scan
     */
    @Builder.Default
    int warningCount = 0;

    /**
     * All of the messages associated with the scan
     *
     * Key: Repository File Path
     * Value: Collection of {@link MessageDescriptor}
     */
    @Builder.Default
    @SuppressWarnings("squid:S1948") // Lombok generates private modifier
    Map<String, Collection<MessageDescriptor>> messages = Collections.emptyMap();

    /**
     * Messages formatted for reporting
     *
     * Format: [SEVERITY] repositoryFilePath :: message
     */
    @Builder.Default
    @SuppressWarnings("squid:S1948") // Lombok generates private modifier
    Collection<String> formattedMessages = Collections.emptyList();

    /**
     * Determine if the result is passed, AND has no warnings
     *
     * @return true if passed and has no warnings, false otherwise
     */
    public boolean isPassedWithNoWarnings() {
        return passed && warningCount == 0;
    }

    /**
     * Constructs a "passed" instance of {@link ScanResult}
     *
     * @return the scan result
     */
    public static ScanResult passed() {
        return new ScanResult(true, 0, 0, Collections.emptyMap(), Collections.emptyList());
    }

    /**
     * Reduce two {@link ScanResult}s into one
     *
     * @param one the first scan result
     * @param two the second scan result
     * @return the reduced scan result
     */
    public static ScanResult reduce(final ScanResult one, final ScanResult two) {
        val formattedMessages = new HashSet<>(one.getFormattedMessages());
        formattedMessages.addAll(two.getFormattedMessages());
        return ScanResult.builder()
                .passed(one.passed && two.passed)
                .errorCount(one.errorCount + two.errorCount)
                .warningCount(one.warningCount + two.warningCount)
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

        @NonNull String severity;
        @NonNull String repositoryPath;
        @NonNull String message;

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return String.format("[%s] %s :: %s", severity, repositoryPath, message);
        }

    }

}
