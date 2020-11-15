package com.optum.sourcehawk.enforcer.file.aot;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation used by annotation processing to identify file enforcers for code generation
 *
 * @apiNote Internal use only for annotation processing
 * @author Brian Wyka
 */
@Documented
@Inherited
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PACKAGE)
public @interface SourcehawkFileEnforcerRegistry { }
