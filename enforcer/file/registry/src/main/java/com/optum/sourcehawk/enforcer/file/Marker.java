package com.optum.sourcehawk.enforcer.file;

import com.optum.sourcehawk.enforcer.file.aot.SourcehawkFileEnforcerRegistry;

/**
 * This is a marker interface to indicate that this package should generate a
 * registry of {@link com.optum.sourcehawk.enforcer.file.FileEnforcer}s
 *
 * @author Brian Wyka
 */
@SuppressWarnings("unused")
@SourcehawkFileEnforcerRegistry
public interface Marker { }
