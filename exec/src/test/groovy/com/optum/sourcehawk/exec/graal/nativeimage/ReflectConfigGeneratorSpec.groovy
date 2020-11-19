package com.optum.sourcehawk.exec.graal.nativeimage

import com.optum.sourcehawk.configuration.SourcehawkConfiguration
import com.optum.sourcehawk.enforcer.file.FileEnforcer
import com.optum.sourcehawk.enforcer.file.FileEnforcerRegistry
import com.optum.sourcehawk.exec.FileBaseSpecification
import com.optum.sourcehawk.protocol.FileProtocol
import org.reflections.Reflections

import java.lang.reflect.Modifier
import java.nio.charset.StandardCharsets
import java.util.stream.Collectors

/**
 * The purpose of this spec is to generate a reflection configuration file which whitelists
 * all known uses of reflection, which is required for building a native image
 *
 * https://github.com/oracle/graal/blob/master/substratevm/REFLECTION.md
 */
class ReflectConfigGeneratorSpec extends FileBaseSpecification {

    String nativeImageConfigFilePathPrefix = "target/classes/META-INF/native-image"

    def "generate"() {
        given:
        String generatedReflectionConfigFilePath = "${nativeImageConfigFilePathPrefix}/sourcehawk-generated/reflect-config.json"
        File generatedReflectionConfigFile = new File(generatedReflectionConfigFilePath)
        createParentDirectories(generatedReflectionConfigFile)
        String generatedReflectionConfigTemplate = ReflectConfigGeneratorSpec
                .getClassLoader()
                .getResourceAsStream("reflect-config-template.json")
                .getText(StandardCharsets.UTF_8.name())
        Reflections reflections = new Reflections("com.optum.sourcehawk.enforcer")
        Set<Class<?>> enforcerClasses = reflections.getSubTypesOf(FileEnforcer).stream()
                .filter({!Modifier.isAbstract(it.modifiers) })
                .collect(Collectors.toSet())


        when:
        // Sourcehawk (required to support Jackson Deserialization)
        Set<Class<?>> configurationClasses = new HashSet<>()
        configurationClasses.add(SourcehawkConfiguration)
        configurationClasses.add(FileProtocol)
        configurationClasses.add(FileProtocol.FileProtocolBuilder)
        Set<Class<?>> reflectionClasses = enforcerClasses + configurationClasses

        then:
        reflectionClasses
        reflectionClasses.size() == FileEnforcerRegistry.getEnforcers().size() + configurationClasses.size()
        reflectionClasses.stream().noneMatch({ Modifier.isAbstract(it.modifiers) || Modifier.isInterface(it.modifiers) })

        when:
        FileOutputStream generatedFileOutputStream = new FileOutputStream(generatedReflectionConfigFile, false)
        generatedFileOutputStream.write("[\n".bytes)
        Iterator<Class<?>> reflectionClassesIterator = reflectionClasses.iterator()
        while (reflectionClassesIterator.hasNext()) {
            Class<?> enforcerClass = reflectionClassesIterator.next()
            generatedFileOutputStream.write(generatedReflectionConfigTemplate.replace("_CLASS_", enforcerClass.getName()).bytes)
            if (reflectionClassesIterator.hasNext()) {
                generatedFileOutputStream.write(",\n".bytes)
            }
        }
        generatedFileOutputStream.write("\n]".bytes)
        generatedFileOutputStream.close()

        then:
        generatedReflectionConfigFile.exists()
    }

}
