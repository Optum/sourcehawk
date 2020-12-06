package com.optum.sourcehawk.exec.scan;

import com.optum.sourcehawk.core.protocol.file.FileProtocol;
import com.optum.sourcehawk.core.scan.ScanResult;
import com.optum.sourcehawk.core.scan.Severity;
import com.optum.sourcehawk.enforcer.EnforcerResult;
import com.optum.sourcehawk.exec.ExecOptions;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.IntConsumer;

/**
 * A factory for creating instances of {@link ScanResult}
 *
 * @author Brian Wyka
 */
@Slf4j
@UtilityClass
public class ScanResultFactory {

    /**
     * Create a scan result based on the file protocol and enforcer result
     *
     * @param execOptions the exec options
     * @param repositoryPath the path to the file in the repository
     * @param severity the severity of the file protocol
     * @param enforcerResult the result of the enforcer
     * @return the derived scan result
     */
    public ScanResult enforcerResult(final ExecOptions execOptions, final String repositoryPath, Severity severity, final EnforcerResult enforcerResult) {
        val messages = new ArrayList<ScanResult.MessageDescriptor>();
        val formattedMessages = new ArrayList<String>();
        for (val message: enforcerResult.getMessages()) {
            val messageDescriptor = ScanResult.MessageDescriptor.builder()
                    .severity(severity.name())
                    .repositoryPath(repositoryPath)
                    .message(message)
                    .build();
            messages.add(messageDescriptor);
            formattedMessages.add(messageDescriptor.toString());
        }
        val scanResultBuilder = ScanResult.builder();
        if (enforcerResult.isPassed() || (Severity.WARNING == severity && !execOptions.isFailOnWarnings())) {
            scanResultBuilder.passed(true);
        }
        if (!messages.isEmpty()) {
            scanResultBuilder.messages(Collections.singletonMap(repositoryPath, messages));
        }
        if (!formattedMessages.isEmpty()) {
            scanResultBuilder.formattedMessages(formattedMessages);
        }
        return acceptCount(scanResultBuilder, severity, formattedMessages.size())
                .build();
    }

    /**
     * Create the scan result for situations where there is an error executing the scan
     *
     * @param repositoryPath the path to the file in the repository
     * @param message the error message
     * @return the scan result
     */
    public ScanResult error(final String repositoryPath, final String message) {
        val messageDescriptor = ScanResult.MessageDescriptor.builder()
                .message(message)
                .repositoryPath(repositoryPath)
                .severity(Severity.ERROR.name())
                .build();
        return ScanResult.builder()
                .passed(false)
                .messages(Collections.singletonMap(repositoryPath, Collections.singleton(messageDescriptor)))
                .formattedMessages(Collections.singleton(message))
                .errorCount(1)
                .build();
    }

    /**
     * Generate a scan result for situations where the file is not found
     *
     * @param execOptions the exec options
     * @param fileProtocol the file protocol
     * @return the file not found scan result
     */
    public ScanResult fileNotFound(final ExecOptions execOptions, final FileProtocol fileProtocol) {
        val severity = Severity.parse(fileProtocol.getSeverity());
        val messageDescriptor = ScanResult.MessageDescriptor.builder()
                .severity(fileProtocol.getSeverity())
                .repositoryPath(fileProtocol.getRepositoryPath())
                .message("File not found")
                .build();
        val scanResultBuilder = ScanResult.builder()
                .passed(Severity.WARNING.equals(severity) && !execOptions.isFailOnWarnings())
                .messages(Collections.singletonMap(fileProtocol.getRepositoryPath(), Collections.singleton(messageDescriptor)))
                .formattedMessages(Collections.singleton(messageDescriptor.toString()));
        return acceptCount(scanResultBuilder, Severity.parse(fileProtocol.getSeverity()), 1)
                .build();
    }

    /**
     * Accept the scan result count and apply it to the appropriate field
     *
     * @param scanResultBuilder the scan result builder
     * @param severity the severity
     * @param count the count
     * @return the builder
     */
    private ScanResult.ScanResultBuilder acceptCount(final ScanResult.ScanResultBuilder scanResultBuilder, final Severity severity, final int count) {
        final IntConsumer countConsumer;
        switch (severity) {
            case ERROR:
                countConsumer = scanResultBuilder::errorCount;
                break;
            case WARNING:
                countConsumer = scanResultBuilder::warningCount;
                break;
            default:
                countConsumer = c -> {};
        }
        countConsumer.accept(count);
        return scanResultBuilder;
    }

}
