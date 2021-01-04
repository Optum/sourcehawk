package com.optum.sourcehawk.core.data;

import com.optum.sourcehawk.core.utils.StringUtils;

/**
 * An enumeration of the types of output formats
 *
 * @author Brian Wyka
 */
public enum OutputFormat {

    CONSOLE,
    TEXT,
    JSON,
    MARKDOWN;

    /**
     * Parse the scan output format from a string value, or return the default
     *
     * @param name the name of the scan output format
     * @return the scan output format derived from {@code name} or the provided default
     */
    public static OutputFormat parse(final String name) {
        if (StringUtils.isBlankOrEmpty(name)) {
            return CONSOLE;
        }
        try {
            return OutputFormat.valueOf(name.toUpperCase());
        } catch (final IllegalArgumentException e) {
            return CONSOLE;
        }
    }

}
