package com.optum.sourcehawk.exec.fix;

import com.optum.sourcehawk.core.protocol.file.FileProtocol;
import com.optum.sourcehawk.core.result.FixResult;
import com.optum.sourcehawk.core.utils.CollectionUtils;
import com.optum.sourcehawk.enforcer.ResolverResult;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

/**
 * A factory for creating instances of {@link FixResult}
 *
 * @author Brian Wyka
 */
@UtilityClass
public class FixResultFactory {

    /**
     * Create a fix result based on the file protocol and resolver result
     *
     * @param fileProtocol the file protocol
     * @param resolverResult the result of the resolver
     * @param dryRun whether or not this is a dry run
     * @return the derived fix result
     */
    public FixResult resolverResult(final FileProtocol fileProtocol, final ResolverResult resolverResult, final boolean dryRun) {
        val fixResultBuilder = FixResult.builder()
                .fixesApplied(resolverResult.isUpdatesApplied() && !dryRun);
        if (CollectionUtils.isNotEmpty(resolverResult.getMessages())) {
            val messages = new ArrayList<FixResult.MessageDescriptor>();
            val formattedMessages = new ArrayList<String>();
            for (val message : resolverResult.getMessages()) {
                val messageDescriptor = FixResult.MessageDescriptor.builder()
                        .repositoryPath(fileProtocol.getRepositoryPath())
                        .message(message)
                        .build();
                messages.add(messageDescriptor);
                formattedMessages.add(messageDescriptor.toString());
            }
            fixResultBuilder.formattedMessages(formattedMessages)
                    .messages(Collections.singletonMap(fileProtocol.getRepositoryPath(), messages));
        }
        return fixResultBuilder.fixCount(resolverResult.getFixCount())
                .error(resolverResult.isError())
                .errorCount(resolverResult.getErrorCount())
                .build();
    }

    /**
     * Create the fix result for situations where there is an error executing the fix
     *
     * @param repositoryPath the repository file path
     * @param message the error message
     * @return the fix result
     */
    public FixResult error(final String repositoryPath, final String message) {
        val messageDescriptor = FixResult.MessageDescriptor.builder()
                .message(message)
                .repositoryPath(repositoryPath)
                .build();
        return FixResult.builder()
                .error(true)
                .errorCount(1)
                .messages(Collections.singletonMap(repositoryPath, Collections.singleton(messageDescriptor)))
                .formattedMessages(Collections.singleton(messageDescriptor.toString()))
                .build();
    }

    /**
     * Create the fix result for situations where there is an error executing the fix
     *
     * @param throwable the exception / error which occurred
     * @return the fix result
     */
    public FixResult globalError(final Throwable throwable) {
        return error("GLOBAL", Optional.ofNullable(throwable.getMessage()).orElse(String.format("Unknown error: %s", throwable.getClass().getCanonicalName())));
    }

    /**
     * Create the fix result for situations when there is no resolver to fix the enforcer error
     *
     * @param repositoryPath the repository file path
     * @param fileEnforcer the file enforcer
     * @return the fix result
     */
    public FixResult noResolver(final String repositoryPath, final String fileEnforcer) {
        val message = String.format("No fixes applied, file enforcer %s does not have any resolutions", fileEnforcer);
        val messageDescriptor = FixResult.MessageDescriptor.builder()
                .message(message)
                .repositoryPath(repositoryPath)
                .build();
        return FixResult.builder()
                .noResolver(true)
                .messages(Collections.singletonMap(repositoryPath, Collections.singleton(messageDescriptor)))
                .formattedMessages(Collections.singleton(messageDescriptor.toString()))
                .build();
    }

    /**
     * Generate a fix result for situations where the file is not found
     *
     * @param fileProtocol the file protocol
     * @return the file not found fix result
     */
    public FixResult fileNotFound(final FileProtocol fileProtocol) {
        val messageDescriptor = FixResult.MessageDescriptor.builder()
                .repositoryPath(fileProtocol.getRepositoryPath())
                .message("File not found")
                .build();
        return FixResult.builder()
                .fixesApplied(false)
                .fixCount(0)
                .error(true)
                .errorCount(1)
                .messages(Collections.singletonMap(fileProtocol.getRepositoryPath(), Collections.singleton(messageDescriptor)))
                .formattedMessages(Collections.singleton(messageDescriptor.toString()))
                .build();
    }

}
