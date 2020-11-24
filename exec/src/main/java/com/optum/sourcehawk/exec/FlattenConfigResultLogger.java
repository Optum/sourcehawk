package com.optum.sourcehawk.exec;

import com.optum.sourcehawk.core.repository.LocalRepositoryFileWriter;
import com.optum.sourcehawk.core.scan.FlattenConfigResult;
import com.optum.sourcehawk.core.utils.StringUtils;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.nio.file.Path;

/**
 * A logger for flatten config results
 *
 * @author Christian Oestreich
 * @see com.optum.sourcehawk.core.scan.FlattenConfigResult
 */
@UtilityClass
class FlattenConfigResultLogger {

    private static final String SOURCEHAWK_FLATTENED_YML = "sourcehawk-flattened.yml";

    /**
     * Log the result of the fix in the specified format
     *
     * @param flattenConfigResult the flatten config result
     * @param output              the output location of the results
     */
    void log(final FlattenConfigResult flattenConfigResult, final Path output) {
        if (output == null || StringUtils.isBlankOrEmpty(output.toString())) {
            handleConsoleOutput(flattenConfigResult);
        } else {
            handleFileSystemOutput(flattenConfigResult, output);
        }
    }

    /**
     * Log the result of the flatten to the file system
     *
     * @param flattenResult      the flatten result
     * @param repositoryFilePath the repository file path
     */
    private static void handleFileSystemOutput(final FlattenConfigResult flattenResult, final Path repositoryFilePath) {
        try {
            val outputPath = getOutputPath(repositoryFilePath);
            LocalRepositoryFileWriter.writer().write(outputPath, flattenResult.getContent());
            Sourcehawk.CONSOLE_RAW_LOGGER.info(flattenResult.getFormattedMessage());
            Sourcehawk.CONSOLE_RAW_LOGGER.info("Output to {}", outputPath);
        } catch (Exception e) {
            Sourcehawk.CONSOLE_RAW_LOGGER.error("Could not flatten file due to {}", e.getMessage());
        }
    }

    /**
     * Get the output path or default path of sourcehawk-flattened.yml
     * if not provided
     *
     * @param repositoryFilePath The path to output the
     * @return The output file name to use
     */
    private static String getOutputPath(final Path repositoryFilePath) {
        if (repositoryFilePath != null) {
            return StringUtils.defaultString(repositoryFilePath.toString(), SOURCEHAWK_FLATTENED_YML);
        }
        return SOURCEHAWK_FLATTENED_YML;
    }

    /**
     * Log the result of the flatten to the console
     *
     * @param flattenResult the flatten result
     */
    private static void handleConsoleOutput(final FlattenConfigResult flattenResult) {
        if (flattenResult != null && flattenResult.getContent() != null) {
            if (flattenResult.isError()) {
                Sourcehawk.CONSOLE_RAW_LOGGER.error(new String(flattenResult.getContent()));
            } else {
                Sourcehawk.CONSOLE_RAW_LOGGER.info(new String(flattenResult.getContent()));
            }
        } else {
            Sourcehawk.CONSOLE_RAW_LOGGER.error("No flattened file produced!");
        }
    }

}
