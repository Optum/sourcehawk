package com.optum.sourcehawk.exec.scan;

import com.optum.sourcehawk.core.configuration.SourcehawkConfiguration;
import com.optum.sourcehawk.core.protocol.file.FileProtocol;
import com.optum.sourcehawk.core.result.ScanResult;
import com.optum.sourcehawk.core.data.Severity;
import com.optum.sourcehawk.core.utils.CollectionUtils;
import com.optum.sourcehawk.core.utils.FileUtils;
import com.optum.sourcehawk.core.utils.Try;
import com.optum.sourcehawk.enforcer.file.FileEnforcer;
import com.optum.sourcehawk.exec.ConfigurationReader;
import com.optum.sourcehawk.exec.ExecOptions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Entry point into executing scans
 *
 * @author Brian Wyka
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ScanExecutor {

    /**
     * Run the scan based on the provided options
     *
     * @param execOptions the scan options
     * @return the scan result
     */
    public static ScanResult scan(final ExecOptions execOptions) {
        return ConfigurationReader.readConfiguration(execOptions.getRepositoryRoot(), execOptions.getConfigurationFileLocation())
                .map(sourcehawkConfiguration -> processRequiredFileProtocols(execOptions, sourcehawkConfiguration))
                .orElseGet(() -> ScanResultFactory.error(execOptions.getConfigurationFileLocation(), "Configuration file not found"));
    }

    /**
     * Process all the required file protocols.  Iterate over all file protocols, and enforcers and aggregate the results
     *
     * @param execOptions             the scan options
     * @param sourcehawkConfiguration the configuration
     * @return the aggregated scan result
     */
    private static ScanResult processRequiredFileProtocols(final ExecOptions execOptions, final SourcehawkConfiguration sourcehawkConfiguration) {
        return sourcehawkConfiguration.getFileProtocols().stream()
                .filter(FileProtocol::isRequired)
                .filter(fileProtocol -> execOptions.getTags().isEmpty() || Arrays.stream(fileProtocol.getTags()).anyMatch(execOptions.getTags()::contains))
                .map(fileProtocol -> processFileProtocol(execOptions, fileProtocol))
                .reduce(ScanResult.passed(), ScanResult::reduce);
    }

    /**
     * Process the file protocol based on the exec options with the file produced by the repository file reader
     *
     * @param execOptions          the exec options
     * @param fileProtocol         the file protocol
     * @return the scan result
     */
    private static ScanResult processFileProtocol(final ExecOptions execOptions, final FileProtocol fileProtocol) {
        if (CollectionUtils.isEmpty(fileProtocol.getEnforcers())) {
            if (FileUtils.isGlobPattern(fileProtocol.getRepositoryPath())) {
                val message = "Error enforcing file protocol: glob patterns can only be used when there is at least one enforcer";
                return ScanResultFactory.error(fileProtocol.getRepositoryPath(), message);
            }
            return enforceFileExists(execOptions, fileProtocol);
        }
        return Try.attemptOrDefault(
                () -> enforceFileProtocol(execOptions, fileProtocol),
                e -> ScanResultFactory.error(fileProtocol.getRepositoryPath(), String.format("Error enforcing file protocol: %s", e.getMessage()))
        );
    }

    /**
     * Enforce the file exists when the file protocol has no enforcers
     *
     * @param execOptions          the exec options
     * @param fileProtocol         the file protocol
     * @return the scan result
     */
    private static ScanResult enforceFileExists(final ExecOptions execOptions, final FileProtocol fileProtocol) {
        try {
            if (execOptions.getRepositoryFileReader().exists(fileProtocol.getRepositoryPath())) {
                return ScanResult.passed();
            }
            return ScanResultFactory.fileNotFound(execOptions, fileProtocol);
        } catch (final IOException e) {
            return ScanResultFactory.error(fileProtocol.getRepositoryPath(), String.format("Unable to obtain file input stream: %s", e.getMessage()));
        }
    }

    /**
     * Enforce the file protocol and aggregate the results
     *
     * @param execOptions          the exec options
     * @param fileProtocol         the file protocol
     * @return the scan result
     * @throws IOException if any error occurs during file processing
     */
    private static ScanResult enforceFileProtocol(final ExecOptions execOptions, final FileProtocol fileProtocol) throws IOException {
        val fileProtocolScanResults = new ArrayList<ScanResult>(fileProtocol.getEnforcers().size());
        for (val enforcer : fileProtocol.getEnforcers()) {
            final FileEnforcer fileEnforcer;
            try {
                fileEnforcer = ConfigurationReader.parseFileEnforcer(enforcer);
            } catch (final IllegalArgumentException e) {
                fileProtocolScanResults.add(ScanResultFactory.error(fileProtocol.getRepositoryPath(), String.format("File enforcer invalid: %s", e.getMessage())));
                continue;
            }
            if (execOptions.getRepositoryFileReader().supportsGlobPatterns() && FileUtils.isGlobPattern(fileProtocol.getRepositoryPath())) {
                fileProtocolScanResults.addAll(executeFileEnforcerOnGlob(execOptions, fileProtocol, fileEnforcer));
            } else {
                fileProtocolScanResults.add(executeFileEnforcer(execOptions, fileProtocol.getRepositoryPath(), fileProtocol.getSeverity(), fileEnforcer));
            }
        }
        return fileProtocolScanResults.stream()
                .reduce(ScanResult.passed(), ScanResult::reduce);
    }

    /**
     * Execute the file enforcer on all files matched by the glob pattern provided by the file protocol repository path
     *
     * @param execOptions the exec options
     * @param fileProtocol the file protocol containing the repository file path glob pattern and severity
     * @param fileEnforcer the file enforcer
     * @return the collection of scan results
     * @throws IOException if any error occurs enforcing the file protocol
     */
    private static Collection<ScanResult> executeFileEnforcerOnGlob(final ExecOptions execOptions, final FileProtocol fileProtocol,
                                                                    final FileEnforcer fileEnforcer) throws IOException {
        val repositoryPaths = FileUtils.find(execOptions.getRepositoryRoot().toString(), fileProtocol.getRepositoryPath())
                .map(Path::toAbsolutePath)
                .map(Path::toString)
                .map(absoluteRepositoryFilePath -> FileUtils.deriveRelativePath(execOptions.getRepositoryRoot().toString(), absoluteRepositoryFilePath))
                .collect(Collectors.toSet());
        if (repositoryPaths.isEmpty()) {
            return Collections.singleton(ScanResultFactory.fileNotFound(execOptions, fileProtocol));
        }
        val fileEnforcerScanResults = new ArrayList<ScanResult>(repositoryPaths.size());
        for (val repositoryPath : repositoryPaths) {
            fileEnforcerScanResults.add(executeFileEnforcer(execOptions, repositoryPath, fileProtocol.getSeverity(), fileEnforcer));
        }
        return fileEnforcerScanResults;
    }

    /**
     * Execute the file enforcer to produce the scan result
     *
     * @param execOptions the exec options
     * @param repositoryFilePath the repository file path
     * @param severity the severity of the file protocol
     * @param fileEnforcer the file enforcer to execute
     * @return the scan result
     * @throws IOException if any error occurs accessing the file or executing enforcer
     */
    private static ScanResult executeFileEnforcer(final ExecOptions execOptions, final String repositoryFilePath, final String severity,
                                                  final FileEnforcer fileEnforcer) throws IOException {
        try (val fileInputStream = execOptions.getRepositoryFileReader().read(repositoryFilePath)
                .orElseThrow(() -> new IOException(String.format("File not found: %s", repositoryFilePath)))) {
            val enforcerResult = fileEnforcer.enforce(fileInputStream);
            return ScanResultFactory.enforcerResult(execOptions, repositoryFilePath, Severity.parse(severity), enforcerResult);
        }
    }

}
