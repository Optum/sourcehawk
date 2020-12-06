package com.optum.sourcehawk.exec;

/**
 * Exception thrown when error in parsing configuration
 *
 * @author Christian Oestreich
 */
public class ConfigurationException extends RuntimeException {

    private static final long serialVersionUID = -3780617079858754794L;

    /**
     * Constructs an instance of this exception with the given message
     *
     * @param message the error message
     */
    public ConfigurationException(final String message) {
        super(message);
    }

}
