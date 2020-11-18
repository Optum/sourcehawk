package com.optum.sourcehawk.exec;

import com.optum.sourcehawk.core.scan.FlattenResult;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Collections;

/**
 * A factory for creating instances of {@link FlattenResult}
 *
 * @author Christian Oestreich
 */
@Slf4j
@UtilityClass
class FlattenResultFactory {

    /**
     * Create the flatten result for situations where there is an error executing the flatten
     *
     * @param repositoryPath the repository file path
     * @param message        the error message
     * @return the flatten result
     */
    FlattenResult error(final String repositoryPath, final String message) {
        val messageDescriptor = FlattenResult.MessageDescriptor.builder()
                .message(message)
                .repositoryPath(repositoryPath)
                .build();
        return FlattenResult.builder()
                .error(true)
                .errorCount(1)
                .messages(Collections.singletonMap(repositoryPath, Collections.singleton(messageDescriptor)))
                .formattedMessages(Collections.singleton(messageDescriptor.toString()))
                .build();
    }

    /**
     * Generate a flatten result for successful merges
     *
     * @return the file not found flatten result
     */
    public static FlattenResult success(final byte[] content) {
        return FlattenResult.success(content);
    }
}
