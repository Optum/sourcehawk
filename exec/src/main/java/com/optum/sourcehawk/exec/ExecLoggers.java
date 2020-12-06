package com.optum.sourcehawk.exec;

import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loggers used by the exec module
 *
 * @author Brian Wyka
 */
@UtilityClass
public class ExecLoggers {

    /**
     * Logger for outputting to the console without any log message formatting
     */
    public final Logger CONSOLE_RAW = LoggerFactory.getLogger("CONSOLE-RAW");
    /**
     * Highlighted raw message
     */
    public final Logger HIGHLIGHT = LoggerFactory.getLogger("HIGHLIGHT");
    /**
     * Logger for outputting formatted message to the console
     */
    public final Logger MESSAGE = LoggerFactory.getLogger("MESSAGE");
    /**
     * Logger for outputting formatted message to console with ansi-color
     */
    public final Logger MESSAGE_ANSI = LoggerFactory.getLogger("MESSAGE-ANSI");

}
