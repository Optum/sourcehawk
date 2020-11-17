package com.optum.sourcehawk.exec;

import com.optum.sourcehawk.configuration.SourcehawkConfiguration;
import com.optum.sourcehawk.core.repository.LocalRepositoryFileReader;
import com.optum.sourcehawk.core.repository.RepositoryFileReader;
import com.optum.sourcehawk.core.scan.FixResult;
import com.optum.sourcehawk.enforcer.EnforcerConstants;
import com.optum.sourcehawk.enforcer.file.FileEnforcer;
import com.optum.sourcehawk.enforcer.file.FileResolver;
import com.optum.sourcehawk.protocol.FileProtocol;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

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
        val repositoryFileReader = LocalRepositoryFileReader.create(execOptions.getRepositoryRoot());
        val filteredFileProtocols = sourcehawkConfiguration.getFileProtocols().stream()
                .filter(FileProtocol::isRequired)
                .collect(Collectors.toSet());
        val fileProtocolFixResults = new ArrayList<FixResult>(filteredFileProtocols.size());
        for (val fileProtocol : filteredFileProtocols) {
            try (val stringWriter = new StringWriter()) {
                val fixResult = fixFileProtocol(repositoryFileReader, fileProtocol, stringWriter, dryRun);
                fileProtocolFixResults.add(fixResult);
                if (fixResult.isFixesApplied() && !dryRun) {
                    try (val fileWriter = new FileWriter(new File(fileProtocol.getRepositoryPath()))) {
                        fileWriter.write(stringWriter.toString());
                    }
                }
            } catch (final IOException e) {
                fileProtocolFixResults.add(FixResultFactory.error(fileProtocol.getRepositoryPath(), String.format("Error fixing file protocol: %s", e.getMessage())));
            }
        }
        return fileProtocolFixResults.stream()
                .reduce(FixResult.builder().build(), FixResult::reduce);
    }

    /**
     * Fix the file protocol
     *
     * @param repositoryFileReader the repository file reader
     * @param fileProtocol the file protocol
     * @param fixWriter the fix writer
     * @param dryRun whether or not this is a dry run
     * @return the scan result
     * @throws IOException if any error occurs during file processing
     */
    private static FixResult fixFileProtocol(final RepositoryFileReader repositoryFileReader, final FileProtocol fileProtocol,
                                             final Writer fixWriter, boolean dryRun) throws IOException {
        val fileProtocolFixResults = new ArrayList<FixResult>(fileProtocol.getEnforcers().size());
        for (val enforcer : fileProtocol.getEnforcers()) {
            final Optional<FileResolver> fileResolverOptional;
            try {
                fileResolverOptional = convertToFileResolver(ConfigurationReader.MAPPER.convertValue(enforcer, FileEnforcer.class));
            } catch (final IllegalArgumentException e) {
                return FixResultFactory.error(fileProtocol.getRepositoryPath(), String.format("File enforcer invalid: %s", e.getMessage()));
            }
            if (fileResolverOptional.isPresent()) {
                // TODO: handle glob patterns
                val fileInputStreamOptional = repositoryFileReader.read(fileProtocol.getRepositoryPath());
                if (fileInputStreamOptional.isPresent()) {
                    try (val fileInputStream = fileInputStreamOptional.get()) {
                        fileProtocolFixResults.add(FixResultFactory.resolverResult(fileProtocol, fileResolverOptional.get().resolve(fileInputStream, fixWriter), dryRun));
                    } catch (final IOException e) {
                        val message = String.format("Error fixing file protocol: %s", e.getMessage());
                        fileProtocolFixResults.add(FixResultFactory.error(fileProtocol.getRepositoryPath(), message));
                    }
                } else {
                    fileProtocolFixResults.add(FixResultFactory.fileNotFound(fileProtocol));
                    break;
                }
            } else {
                val fixResult = FixResultFactory.noResolver(fileProtocol.getRepositoryPath(), String.valueOf(enforcer.get(EnforcerConstants.DESERIALIZATION_TYPE_KEY)));
                fileProtocolFixResults.add(fixResult);
            }
        }
        return fileProtocolFixResults.stream()
                .reduce(FixResult.builder().build(), FixResult::reduce);
    }

    /**
     * Convert the file enforcer to a file resolver
     *
     * @param fileEnforcer the file enforcer
     * @return the file resolver if able to be converted, otherwise {@link Optional#empty()}
     */
    private static Optional<FileResolver> convertToFileResolver(final FileEnforcer fileEnforcer) {
        if (fileEnforcer instanceof FileResolver) {
            return Optional.of(fileEnforcer)
                    .map(FileResolver.class::cast);
        }
        return Optional.empty();
    }

}
