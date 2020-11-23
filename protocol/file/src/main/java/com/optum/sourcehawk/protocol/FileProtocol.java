package com.optum.sourcehawk.protocol;

import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * A protocol which allow for defining a set of rules to enforce on a file
 *
 * @author Brian Wyka
 * @author Christian Oestreich
 * @see Protocol
 */
@Value
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@JsonDeserialize(builder = FileProtocol.FileProtocolBuilder.class) // Persuade Mr. Jackson to use builder (which allows defaults during deserialization)
public class FileProtocol implements Protocol {

    /**
     * The name of the protocol
     */
    @NonNull
    @EqualsAndHashCode.Include
    String name;

    /**
     * An optional description for the protocol
     */
    String description;

    /**
     * The protocol group, allowing for associating different protocols together
     */
    String group;

    /**
     * The path to the file in the repository
     */
    @NonNull
    @EqualsAndHashCode.Include
    String repositoryPath;

    /**
     * Whether or not the protocol is required
     */
    @Builder.Default
    boolean required = true;

    /**
     * Any tags that the protocol should be annotated with
     */
    @Builder.Default
    String[] tags = new String[0];

    /**
     * The severity of the protocol
     */
    @NonNull
    @Builder.Default
    @EqualsAndHashCode.Include
    String severity = "ERROR";

    /**
     * A collection of enforcers which will be run on the file
     */
    @NonNull
    @Builder.Default
    @JsonMerge
    Collection<Map<String, Object>> enforcers = Collections.emptyList();

    /**
     * Jackson will use this builder (further initialized by Lombok) to
     * construct this object during deserialization phase
     *
     * @author Brian Wyka
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static class FileProtocolBuilder { }

}
