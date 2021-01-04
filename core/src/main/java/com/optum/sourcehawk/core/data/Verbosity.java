package com.optum.sourcehawk.core.data;

import com.optum.sourcehawk.core.utils.StringUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Verbosity
 *
 * @author Brian Wyka
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum Verbosity {

    ZERO(0),
    LOW(1),
    MEDIUM(2),
    HIGH(3);

    private final int level;

    /**
     * Parse the verbosity from a string value, or return {@link #HIGH}
     *
     * @param name the name of the verbosity
     * @return the verbosity derived from {@code name} or the default
     */
    public static Verbosity parse(final String name) {
        if (StringUtils.isBlankOrEmpty(name)) {
            return HIGH;
        }
        if (name.equals("0")) {
            return ZERO;
        }
        if (name.length() <= 3) {
            return Arrays.stream(values())
                    .filter(verbosity -> verbosity.name().startsWith(name.toUpperCase()))
                    .findFirst()
                    .orElse(HIGH);
        }
        try {
            return Verbosity.valueOf(name.toUpperCase());
        } catch (final IllegalArgumentException e) {
            return HIGH;
        }
    }

}
