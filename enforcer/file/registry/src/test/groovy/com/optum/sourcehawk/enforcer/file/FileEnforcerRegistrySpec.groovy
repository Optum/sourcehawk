package com.optum.sourcehawk.enforcer.file

import com.optum.sourcehawk.enforcer.file.common.StringPropertyEquals
import com.optum.sourcehawk.enforcer.file.docker.DockerfileFromHasTag
import com.optum.sourcehawk.enforcer.file.maven.MavenBannedProperties
import org.reflections.Reflections
import spock.lang.Specification

import java.lang.reflect.Modifier

class FileEnforcerRegistrySpec extends Specification {

    def "private constructor"() {
        expect:
        new FileEnforcerRegistry()
    }

    def "getEnforcers"() {
        given:
        Reflections reflections = new Reflections("com.optum.sourcehawk.enforcer.file")
        Set<Class<? extends FileEnforcer>> fileEnforcerClasses = reflections.getSubTypesOf(FileEnforcer)
                .findAll { !Modifier.isAbstract(it.getModifiers()) }

        when:
        Map<String, Class<? extends FileEnforcer>> fileEnforcers = FileEnforcerRegistry.getEnforcers()

        then:
        fileEnforcers.size() >= fileEnforcerClasses.size()

        and:
        fileEnforcerClasses.each { Class<? extends FileEnforcer> fileEnforcerClass ->
            String alias = fileEnforcerClass.simpleName.substring(0, 1) + fileEnforcerClass.simpleName.substring(1).replaceAll('(?=[A-Z][a-z])', '-').toUpperCase()
            fileEnforcers[alias] == fileEnforcerClass
            fileEnforcers.containsValue(fileEnforcerClass)
        }
    }

    def "getEnforcerByAlias"() {
        expect:
        FileEnforcerRegistry.getEnforcerByAlias("STRING-PROPERTY-EQUALS").get() == StringPropertyEquals
        FileEnforcerRegistry.getEnforcerByAlias("DOCKERFILE-FROM-HAS-TAG").get() == DockerfileFromHasTag
        FileEnforcerRegistry.getEnforcerByAlias("MAVEN-BANNED-PROPERTIES").get() == MavenBannedProperties
    }

}
