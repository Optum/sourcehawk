package com.optum.sourcehawk.enforcer.file;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.optum.sourcehawk.enforcer.Enforcer;
import com.optum.sourcehawk.enforcer.EnforcerConstants;

import java.io.InputStream;

/**
 * An interface for file enforcers to adhere to
 *
 * @author Brian Wyka
 */
@JsonTypeInfo( // Tell Mr. Jackson to choose implementation to deserialize
        use = JsonTypeInfo.Id.MINIMAL_CLASS,
        property = EnforcerConstants.DESERIALIZATION_TYPE_KEY
)
public interface FileEnforcer extends Enforcer<InputStream> { }
