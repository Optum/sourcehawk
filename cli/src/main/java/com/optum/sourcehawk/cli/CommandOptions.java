package com.optum.sourcehawk.cli;

import com.optum.sourcehawk.core.constants.SourcehawkConstants;
import com.optum.sourcehawk.core.scan.OutputFormat;
import com.optum.sourcehawk.core.scan.Verbosity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import picocli.CommandLine;

import java.net.URL;
import java.nio.file.Path;

/**
 * Command option definitions
 *
 * @author Brian Wyka
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class CommandOptions {

    /**
     * Exec command options
     *
     * @author Brian Wyka
     */
    static class Exec {

        @CommandLine.ArgGroup
        ConfigFile configFile;

        @CommandLine.Option(
                names = {"-v", "--verbosity"},
                description = "Verbosity of output, valid values: ${COMPLETION-CANDIDATES}",
                defaultValue = "HIGH",
                showDefaultValue = CommandLine.Help.Visibility.ALWAYS
        )
        Verbosity verbosity;

        @CommandLine.Option(
                names = {"-f", "--output-format"},
                description = "Output Format, valid values: ${COMPLETION-CANDIDATES}",
                defaultValue = "CONSOLE",
                showDefaultValue = CommandLine.Help.Visibility.ALWAYS
        )
        OutputFormat outputFormat;

        @CommandLine.Option(
                names = {"-w", "--fail-on-warnings"},
                description = "Whether to fail the scan if only warnings are found",
                defaultValue = "false",
                showDefaultValue = CommandLine.Help.Visibility.ALWAYS
        )
        boolean failOnWarnings;

    }

    /**
     * File system options
     *
     * @author Brian Wyka
     */
    static class FileSystem {

        @CommandLine.Parameters(
                index = "0",
                paramLabel = REPOSITORY_ROOT,
                description = "The repository root on the file system to scan relative to, defaults to current directory",
                defaultValue = ".",
                arity = "0..1"
        )
        Path repositoryRoot;
        static final String REPOSITORY_ROOT = "REPOSITORY-ROOT";

    }

    /**
     * Exclusive config file options
     *
     * @author Brian Wyka
     */
    static class ConfigFile {

        @CommandLine.Option(
                names = {OPTION_PATH, OPTION_PATH_LONG},
                paramLabel = "config-file-path",
                description = "The configuration file, can be relative, or absolute",
                defaultValue = SourcehawkConstants.DEFAULT_CONFIG_FILE_NAME,
                showDefaultValue = CommandLine.Help.Visibility.ALWAYS
        )
        Path path;
        static final String OPTION_PATH = "-c";
        static final String OPTION_PATH_LONG = "--config-file";

        @CommandLine.Option(
                names = {"-U", "--config-file-url"},
                paramLabel = "config-file-url",
                description = "A remote URL which resolves to a configuration file"
        )
        URL url;

    }

    /**
     * Github options
     *
     * @author Brian Wyka
     */
    static class Github {

        @CommandLine.Option(
                names = {"-t", "--token"},
                paramLabel = "github-token",
                description = "The Github token for authorizing requests, recommended for preventing rate limiting or accessing private repositories"
        )
        String token;

        @CommandLine.Option(
                names = {"-E", "--enterprise-url"},
                paramLabel = "github-enterprise-url",
                description = "The Github enterprise URL to use instead of public Github, i.e - https://github.example.com"
        )
        URL enterpriseUrl;

        @CommandLine.Parameters(
                paramLabel = COORDINATES_LABEL,
                description = "The Github coordinates - owner/repo@ref combination, i.e - owner/repo, owner/repo@main,  owner/repo@v1.4, or owner/repo@a6de43fa51c",
                arity = "1"
        )
        String coordinates;
        static final String COORDINATES_LABEL = "GITHUB-COORDINATES";

    }

}
