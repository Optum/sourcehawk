package com.optum.sourcehawk.core.protocol.file;

<<<<<<< HEAD
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
=======
>>>>>>> Simpler enforcer configuration
import com.optum.sourcehawk.core.protocol.Protocol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Collection;
import java.util.Collections;

/**
 * A protocol which allow for defining a set of rules to enforce on a file
 *
 * @author Brian Wyka
 * @author Christian Oestreich
 * @see Protocol
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
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
     * @deprecated use {@link #tags} instead
     */
    @Deprecated
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
<<<<<<< HEAD
    @JsonMerge
    Collection<Map<String, Object>> enforcers = Collections.emptyList();
=======
    Collection<String> enforcers = Collections.emptySet();
>>>>>>> Simpler enforcer configuration

}
