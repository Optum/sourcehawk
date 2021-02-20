package com.optum.sourcehawk.exec.config;

import com.optum.sourcehawk.core.repository.LocalRepositoryFileWriter;
import com.optum.sourcehawk.core.result.FlattenConfigResult;
import com.optum.sourcehawk.core.utils.StringUtils;
import com.optum.sourcehawk.exec.Console;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Optional;

/**
 * A logger for flatten config results
 *
 * @author Christian Oestreich
 * @see com.optum.sourcehawk.core.result.FlattenConfigResult
 */
@UtilityClass
public class FlattenConfigResultLogger {

    /**
     * The default name of the flattened YAML file
     */
    private static final String DEFAULT_OUTPUT_FILE_NAME = "sourcehawk-flattened.yml";

    /**
     * Log the result of the fix in the specified format
     *
     * @param flattenConfigResult the flatten config result
     * @param output              the output location of the results
     */
    public void log(final FlattenConfigResult flattenConfigResult, final Path output) {
        if (output == null || StringUtils.isBlankOrEmpty(output.toString())) {
            handleConsoleOutput(flattenConfigResult);
        } else {
            handleFileSystemOutput(flattenConfigResult, output);
        }
    }

    /**
     * Log the result of the flatten to the file system
     *
     * @param flattenConfigResult the flatten config result
     * @param outputFilePath the output file path
     */
    private static void handleFileSystemOutput(final FlattenConfigResult flattenConfigResult, final Path outputFilePath) {
        try {
            val outputPath = getOutputPath(outputFilePath);
            LocalRepositoryFileWriter.writer().write(outputPath, flattenConfigResult.getContent());
            Console.Err.log(flattenConfigResult.getMessage());
            Console.Err.log("Flattened configuration output to {}", outputPath);
        } catch (final Exception e) {
            Console.Err.log("Error writing flattened configuration to file: %s", e.getMessage());
        }
    }

    /**
     * Get the output path or default path of sourcehawk-flattened.yml if not provided
     *
     * @param outputFilePath The path to output the
     * @return the output file path to use
     */
    private static String getOutputPath(final Path outputFilePath) {
        return Optional.ofNullable(outputFilePath)
                .map(filePath -> StringUtils.defaultString(outputFilePath.toString(), DEFAULT_OUTPUT_FILE_NAME))
                .orElse(DEFAULT_OUTPUT_FILE_NAME);
    }

    /**
     * Log the result of the flatten to the console
     *
     * @param flattenConfigResult the flatten config result
     */
    @SuppressWarnings("squid:S2629")
    private static void handleConsoleOutput(final FlattenConfigResult flattenConfigResult) {
        if (flattenConfigResult != null && flattenConfigResult.getContent() != null) {
            if (flattenConfigResult.isError()) {
                Console.Err.log(flattenConfigResult.getMessage());
            } else {
                Console.Out.log(new String(flattenConfigResult.getContent(), Charset.defaultCharset()));
            }
        } else {
            Console.Err.log("No flattened configuration file produced!");
        }
    }

}
