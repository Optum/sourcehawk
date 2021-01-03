package com.optum.sourcehawk.core.data;

import com.optum.sourcehawk.core.utils.StringUtils;

/**
 * Severity
 *
 * @author Brian Wyka
 */
public enum Severity {

    INFO,
    WARNING,
    ERROR;

    /**
     * Parse the severity from a string value, or return {@link #ERROR}
     *
     * @param name the name of the severity
     * @return the severity derived from {@code name} or the default
     */
    public static Severity parse(final String name) {
        if (StringUtils.isBlankOrEmpty(name)) {
            return ERROR;
        }
        try {
            return Severity.valueOf(name.toUpperCase());
        } catch (final IllegalArgumentException e) {
            return ERROR;
        }
    }

}
