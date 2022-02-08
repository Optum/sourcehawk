package com.optum.sourcehawk.enforcer.file.aot

import com.optum.sourcehawk.enforcer.file.aot.SourcehawkFileEnforcerRegistry
import spock.lang.Specification

import java.lang.annotation.Documented
import java.lang.annotation.ElementType
import java.lang.annotation.Inherited
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

class SourcehawkFileEnforcerRegistrySpec extends Specification {

    def "annotation metadata"() {
        expect:
        SourcehawkFileEnforcerRegistry.getAnnotation(Documented)
        SourcehawkFileEnforcerRegistry.getAnnotation(Inherited)
        SourcehawkFileEnforcerRegistry.getAnnotation(Retention).value() == RetentionPolicy.SOURCE
        SourcehawkFileEnforcerRegistry.getAnnotation(Target).value()[0] == ElementType.TYPE
    }

}