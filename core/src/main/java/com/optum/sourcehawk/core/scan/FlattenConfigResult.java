package com.optum.sourcehawk.core.scan;

import com.optum.sourcehawk.core.utils.StringUtils;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.io.Serializable;

/**
 * The result of a flatten
 *
 * @author Christian Oestreich
 */
@Value
@Builder
public class FlattenConfigResult implements Serializable {

    private static final long serialVersionUID = -7826662019832933150L;

    /**
     * The content that was flattened
     */
    @Builder.Default
    byte[] content = null;

    /**
     * Whether or not an error occurred during fix
     */
    @Builder.Default
    boolean error = false;

    /**
     * Messages formatted for reporting
     * <p>
     * Format: [SEVERITY] repositoryFilePath :: message
     */
    @NonNull
    @Builder.Default
    @SuppressWarnings("squid:S1948") // Lombok generates private modifier
    String formattedMessage = "Flatten successful";


    /**
     * Constructs a "success" instance of {@link FlattenConfigResult}
     *
     * @return the flatten result
     */
    public static FlattenConfigResult success(final byte[] content) {
        return new FlattenConfigResult(content, false, "Flatten successful");
    }

    /**
     * Constructs a "success" instance of {@link FlattenConfigResult}
     *
     * @return the flatten result
     */
    public static FlattenConfigResult error(final String message) {
        return new FlattenConfigResult(null, true, StringUtils.defaultString(message, "No error message provided"));
    }

}
