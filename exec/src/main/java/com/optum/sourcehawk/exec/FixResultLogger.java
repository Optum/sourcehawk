package com.optum.sourcehawk.exec;

import com.optum.sourcehawk.core.scan.FixResult;
import com.optum.sourcehawk.core.scan.OutputFormat;
import lombok.experimental.UtilityClass;

/**
 * A logger for fix results
 *
 * @see FixResult
 * @see OutputFormat
 *
 * @author Brian Wyka
 */
@UtilityClass
class FixResultLogger {

    /**
     * Log the result of the fix in the specified format
     *
     * @param fixResult the fix result
     * @param execOptions the scan options
     */
    @SuppressWarnings("squid:S2629")
    void log(final FixResult fixResult, final ExecOptions execOptions) {
        // TODO: implement logging for fix results
    }

}
