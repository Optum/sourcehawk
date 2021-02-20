package com.optum.sourcehawk.exec;

import com.optum.sourcehawk.core.constants.SourcehawkConstants;
import com.optum.sourcehawk.core.data.RemoteRef;
import com.optum.sourcehawk.core.repository.LocalRepositoryFileReader;
import com.optum.sourcehawk.core.repository.RepositoryFileReader;
import com.optum.sourcehawk.core.data.OutputFormat;
import com.optum.sourcehawk.core.data.Verbosity;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Execution options to be evaluated
 *
 * @author Brian Wyka
 */
@Value
@Builder(toBuilder = true)
public class ExecOptions {

    /**
     * The root of the repository in which files will be resolved relatively to
     */
    @NonNull
    @Builder.Default
    Path repositoryRoot = Paths.get(".");

    /**
     * The output verbosity
     */
    @NonNull
    @Builder.Default
    Verbosity verbosity = Verbosity.HIGH;

    /**
     * The location of the configuration file, can be on the file system or a URL
     */
    @NonNull
    @Builder.Default
    String configurationFileLocation = SourcehawkConstants.DEFAULT_CONFIG_FILE_NAME;

    /**
     * The scan output format
     */
    @NonNull
    @Builder.Default
    OutputFormat outputFormat = OutputFormat.TEXT;

    /**
     * Whether or not to fail on warnings
     */
    @Builder.Default
    boolean failOnWarnings = false;

    /**
     * Repository file reader
     */
    @NonNull
    @Builder.Default
    RepositoryFileReader repositoryFileReader = LocalRepositoryFileReader.create(Paths.get("."));

    /**
     * The remote reference
     */
    RemoteRef remoteRef;

    /**
     * Print a string representation of the exec options
     *
     * @return the string representation of the exec options
     */
    @Override
    public String toString() {
        String string =  System.lineSeparator();
        if (remoteRef == null) {
            string += "Repository Root... " + repositoryRoot + System.lineSeparator();
        } else {
            string += "Remote Reference.. " + remoteRef + System.lineSeparator();
        }
        string += "Config File....... " + configurationFileLocation + System.lineSeparator();
        string += "Verbosity......... " + verbosity + System.lineSeparator();
        string += "Output Format..... " + outputFormat + System.lineSeparator();
        string += "Fail on Warnings.. " + failOnWarnings + System.lineSeparator();
        return string;
    }

}
