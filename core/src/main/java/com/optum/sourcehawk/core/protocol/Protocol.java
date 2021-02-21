package com.optum.sourcehawk.core.protocol;


/**
 * Definition of a protocol
 *
 * @author Brian Wyka
 * @author Christian Oestreich
 */
@SuppressWarnings("unused")
public interface Protocol {

    /**
     * The name of the protocol
     *
     * @return the name
     */
    String getName();

    /**
     * An optional description for the protocol
     *
     * @return  the description
     */
    String getDescription();

    /**
     * Any tags that the protocol should be annotated with
     *
     * @return the tags
     */
    String[] getTags();

    /**
     * Whether or not the protocol is required
     *
     * @return true if required, false otherwise
     */
    boolean isRequired();

    /**
     * The severity of the protocol
     *
     * @return the severity
     */
    String getSeverity();

}
