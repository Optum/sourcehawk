package com.optum.sourcehawk.exec;

import com.optum.sourcehawk.core.constants.SourcehawkConstants;
import com.optum.sourcehawk.core.scan.OutputFormat;
import com.optum.sourcehawk.core.scan.Verbosity;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.net.URL;
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
     * The location of the configuration file
     */
    @NonNull
    @Builder.Default
    String configurationFileLocation = SourcehawkConstants.DEFAULT_CONFIG_FILE_NAME;

    /**
     * The scan output format
     */
    @NonNull
    @Builder.Default
    OutputFormat outputFormat = OutputFormat.CONSOLE;

    /**
     * Whether or not to fail on warnings
     */
    @Builder.Default
    boolean failOnWarnings = false;

    /**
     * Whether or not this will be executed against Github
     */
    GithubOptions github;

    /**
     * The Github exec options
     *
     * @author Brian Wyka
     */
    @Value
    @Builder
    public static class GithubOptions {

        /**
         * The default Github ref
         */
        public static final String DEFAULT_REF = "main";

        /**
         * The Github personal access token (optional)
         */
        String token;

        /**
         * THe Github owner
         */
        @NonNull
        String owner;

        /**
         * THe Github repository
         */
        @NonNull
        String repository;

        /**
         * The Github reference
         */
        @NonNull
        @Builder.Default
        String ref = DEFAULT_REF;

        /**
         * The Github Enterprise URL (optional)
         */
        URL enterpriseUrl;

    }

    /**
     * Print a string representation of the exec options
     *
     * @return the string representation of the exec options
     */
    @Override
    public String toString() {
        String string =  System.lineSeparator();
        if (github == null) {
            string += "Repository Root... " + repositoryRoot + System.lineSeparator();
        } else {
            string += "Github............ " + String.format("%s/%s@%s", github.owner, github.repository, github.ref) + System.lineSeparator();
        }
        string += "Config File....... " + configurationFileLocation + System.lineSeparator();
        string += "Verbosity......... " + verbosity + System.lineSeparator();
        string += "Output Format..... " + outputFormat + System.lineSeparator();
        string += "Fail on Warnings.. " +failOnWarnings + System.lineSeparator();
        return string;
    }

}
