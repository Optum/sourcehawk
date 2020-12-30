package com.optum.sourcehawk.exec.fix;

import com.optum.sourcehawk.core.configuration.SourcehawkConfiguration;
import com.optum.sourcehawk.core.protocol.file.FileProtocol;
import com.optum.sourcehawk.core.scan.FixResult;
import com.optum.sourcehawk.core.utils.FileUtils;
import com.optum.sourcehawk.enforcer.EnforcerConstants;
import com.optum.sourcehawk.enforcer.file.FileResolver;
import com.optum.sourcehawk.exec.ConfigurationReader;
import com.optum.sourcehawk.exec.ExecOptions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Entry point into executing scans
 *
 * @author Brian Wyka
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FixExecutor {

    /**
     * Run the fix based on the provided options
     *
     * @param execOptions the scan options
     * @param dryRun whether or not this is a dry run
     * @return the fix result
     */
    public static FixResult fix(final ExecOptions execOptions, final boolean dryRun) {
        return ConfigurationReader.readConfiguration(execOptions.getRepositoryRoot(), execOptions.getConfigurationFileLocation())
                .map(sourcehawkConfiguration -> executeFix(execOptions, sourcehawkConfiguration, dryRun))
                .orElseGet(() -> FixResultFactory.error(execOptions.getConfigurationFileLocation(), "Configuration file not found"));
    }

    /**
     * Execute the scan.  Iterate over all file protocols, and each enforcer within the file protocol and aggregate the results
     *
     * @param execOptions the scan options
     * @param sourcehawkConfiguration the configuration
     * @param dryRun whether or not this is a dry run
     * @return the aggregated scan result
     */
    private static FixResult executeFix(final ExecOptions execOptions, final SourcehawkConfiguration sourcehawkConfiguration, final boolean dryRun) {
        if (sourcehawkConfiguration == null) {
            return FixResultFactory.error(execOptions.getConfigurationFileLocation(), "Scan configuration file not found or remote configuration read issue");
        }
        return sourcehawkConfiguration.getFileProtocols().stream()
                .filter(FileProtocol::isRequired)
                .flatMap(fileProtocol -> fileProtocol.getEnforcers().stream()
                        .flatMap(enforcer -> fixBasedOnEnforcer(execOptions, fileProtocol, dryRun, enforcer)))
                .reduce(FixResult.builder().build(), FixResult::reduce);
    }

    /**
     * Fix the file based on the enforcer
     *
     * @param execOptions the exec options
     * @param fileProtocol the file protocol
     * @param dryRun whether or not this is a dry run
     * @param enforcer the raw enforcer object map
     * @return the scan result
     */
    private static Stream<FixResult> fixBasedOnEnforcer(final ExecOptions execOptions, final FileProtocol fileProtocol, final boolean dryRun, final Map<String, Object> enforcer) {
        final Optional<FileResolver> fileResolverOptional;
        try {
            fileResolverOptional = ConfigurationReader.convertFileEnforcerToFileResolver(enforcer);
        } catch (final IllegalArgumentException e) {
            return Stream.of(FixResultFactory.error(fileProtocol.getRepositoryPath(), String.format("File enforcer invalid: %s", e.getMessage())));
        }
        if (!fileResolverOptional.isPresent()) {
            return Stream.of(FixResultFactory.noResolver(fileProtocol.getRepositoryPath(), String.valueOf(enforcer.get(EnforcerConstants.DESERIALIZATION_TYPE_KEY))));
        }
        final Set<Path> repositoryPaths;
        try {
            repositoryPaths = FileUtils.find(execOptions.getRepositoryRoot().toString(), fileProtocol.getRepositoryPath())
                    .map(Path::toAbsolutePath)
                    .collect(Collectors.toSet());
        } catch (final IOException e) {
            val message = String.format("Error finding file(s) matching [%s]: %s", fileProtocol.getRepositoryPath(), e.getMessage());
            return Stream.of(FixResultFactory.error(fileProtocol.getRepositoryPath(), message));
        }
        if (repositoryPaths.isEmpty()) {
            return Stream.of(FixResultFactory.fileNotFound(fileProtocol));
        }
        return repositoryPaths.stream()
                .map(repositoryPath -> fixFile(execOptions, fileProtocol, dryRun, fileResolverOptional.get(), repositoryPath));
    }

    /**
     * Fix the file based on the protocol
     *
     * @param execOptions the exec options
     * @param fileProtocol the file protocol
     * @param dryRun whether or not this is a dry run
     * @param fileResolver the file resolver
     * @param repositoryPath the repository path
     * @return the fix result
     */
    private static FixResult fixFile(final ExecOptions execOptions, final FileProtocol fileProtocol, final boolean dryRun,
                                     final FileResolver fileResolver, final Path repositoryPath) {
        val relativeRepositoryPath = FileUtils.deriveRelativePath(execOptions.getRepositoryRoot().toString(), repositoryPath.toString());
        try (val fileInputStream = execOptions.getRepositoryFileReader().read(relativeRepositoryPath)
                .orElseThrow(() -> new IOException(String.format("File not found: %s", relativeRepositoryPath)));
             val fixWriter = new StringWriter()) {
            val fixResult = FixResultFactory.resolverResult(fileProtocol, fileResolver.resolve(fileInputStream, fixWriter), dryRun);
            if (fixResult.isFixesApplied() && !dryRun) {
                try (val fileWriter = new FileWriter(repositoryPath.toFile())) {
                    fileWriter.write(fixWriter.toString());
                }
            }
            return fixResult;
        } catch (final IOException e) {
            val message = String.format("Error fixing file for protocol [%s]: %s", fileProtocol.getName(), e.getMessage());
            return FixResultFactory.error(fileProtocol.getRepositoryPath(), message);
        }
    }

}
