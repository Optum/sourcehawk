package com.optum.sourcehawk.core.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonMerge;
import com.optum.sourcehawk.core.protocol.file.FileProtocol;
import lombok.Value;

import java.util.Collection;

/**
 * Root of all Sourcehawk configuration
 *
 * @author Brian Wyka
 * @author Christian Oestreich
 */
@Value(staticConstructor = "of")
public class SourcehawkConfiguration {

    /**
     * The remote files to inherit from in URL form
     *
     * JsonIgnore is added so during flatten the config locations are not included into the output
     */
    @JsonIgnore
    @JsonMerge
    Collection<String> configLocations;

    /**
     * The file protocols that should be considered
     */
    @JsonMerge
    Collection<FileProtocol> fileProtocols;

}
