package com.optum.sourcehawk.core.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.optum.sourcehawk.core.protocol.file.FileProtocol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;

/**
 * Root of all Sourcehawk configuration
 *
 * @author Brian Wyka
 * @author Christian Oestreich
 */
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = SourcehawkConfiguration.Builder.class)
@Value
@AllArgsConstructor(staticName = "of")
public class SourcehawkConfiguration {

    /**
     * The remote files to inherit from in URL form
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // Only deserialize
    Collection<String> configLocations;

    /**
     * The file protocols that should be considered
     */
    Collection<FileProtocol> fileProtocols;

    /**
     * Create an empty configuration instance
     */
    public static SourcehawkConfiguration empty() {
        return of(new LinkedHashSet<>(), new HashSet<>());
    }

}
