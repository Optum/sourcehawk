package com.optum.sourcehawk.enforcer.file.aot

import com.optum.sourcehawk.enforcer.file.TestFileEnforcer
import com.optum.sourcehawk.enforcer.file.aot.SourcehawkFileEnforcerRegistry
import com.optum.sourcehawk.enforcer.file.aot.SourcehawkFileEnforcerRegistryProcessor
import org.spockframework.util.IoUtil
import spock.lang.Specification

import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Name
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic
import javax.tools.JavaFileObject

class SourcehawkFileEnforcerRegistryProcessorSpec extends Specification {

    def "constructor"() {
        expect:
        new SourcehawkFileEnforcerRegistryProcessor()
    }

    def "constructor - class not found"() {
        when:
        new SourcehawkFileEnforcerRegistryProcessor("com.optum.sourcehawk.enforcer.file.NotExistingFileEnforcer")

        then:
        thrown(IllegalStateException)
    }

    def "META-INF/services/javax.annotation.processing.Processor"() {
        expect:
        IoUtil.getResourceAsStream("/META-INF/services/javax.annotation.processing.Processor").text == SourcehawkFileEnforcerRegistryProcessor.canonicalName
    }

    def "init"() {
        given:
        ProcessingEnvironment mockProcessingEnvironment = Mock()
        AbstractProcessor sourcehawkFileEnforcerRegistryProcessor = new SourcehawkFileEnforcerRegistryProcessor()

        when:
        sourcehawkFileEnforcerRegistryProcessor.init(mockProcessingEnvironment)

        then:
        0 * _

        and:
        noExceptionThrown()
    }

    def "process - not over"() {
        given:
        TypeElement mockTypeElement = Mock()
        Set<? extends TypeElement> annotations = [ mockTypeElement ] as Set
        RoundEnvironment mockRoundEnvironment = Mock()
        AbstractProcessor sourcehawkFileEnforcerRegistryProcessor = new SourcehawkFileEnforcerRegistryProcessor()

        PackageElement mockPackageElement = Mock()
        Name mockPackageName = Mock()

        when:
        sourcehawkFileEnforcerRegistryProcessor.init(Mock(ProcessingEnvironment))
        boolean process = sourcehawkFileEnforcerRegistryProcessor.process(annotations, mockRoundEnvironment)

        then:
        1 * mockRoundEnvironment.getElementsAnnotatedWith(mockTypeElement) >> [ mockPackageElement ]
        1 * mockPackageElement.getQualifiedName() >> mockPackageName
        1 * mockPackageName.toString() >> "com.optum.sourcehawk.enforcer.file"
        1 * mockRoundEnvironment.processingOver() >> false
        0 * _

        and:
        process
    }

    def "process - over"() {
        given:
        ProcessingEnvironment mockProcessingEnvironment = Mock()
        TypeElement mockTypeElement = Mock()
        Set<? extends TypeElement> annotations = [ mockTypeElement ] as Set
        RoundEnvironment mockRoundEnvironment = Mock()
        AbstractProcessor sourcehawkFileEnforcerRegistryProcessor = new SourcehawkFileEnforcerRegistryProcessor()

        PackageElement mockPackageElement = Mock()
        Name mockPackageName = Mock()
        Filer mockFiler = Mock()
        JavaFileObject mockJavaFileObject = Mock()

        Writer javaFileWriter = new StringWriter()

        when:
        sourcehawkFileEnforcerRegistryProcessor.init(mockProcessingEnvironment)
        boolean process = sourcehawkFileEnforcerRegistryProcessor.process(annotations, mockRoundEnvironment)

        then:
        1 * mockRoundEnvironment.getElementsAnnotatedWith(mockTypeElement) >> [ mockPackageElement ]
        1 * mockPackageElement.getQualifiedName() >> mockPackageName
        1 * mockPackageName.toString() >> "com.optum.sourcehawk.enforcer.file"
        1 * mockRoundEnvironment.processingOver() >> true
        1 * mockProcessingEnvironment.getFiler() >> mockFiler
        1 * mockFiler.createSourceFile("com.optum.sourcehawk.enforcer.file.FileEnforcerRegistry", []) >> mockJavaFileObject
        1 * mockJavaFileObject.openWriter() >> javaFileWriter
        0 * _

        and:
        process

        when:
        String javaFileContents = javaFileWriter.toString()

        then:
        javaFileContents == IoUtil.getResourceAsStream("/FileEnforcerRegistry.java.txt").text
    }

    def "process - over (error during code generation)"() {
        given:
        ProcessingEnvironment mockProcessingEnvironment = Mock()
        TypeElement mockTypeElement = Mock()
        Set<? extends TypeElement> annotations = [ mockTypeElement ] as Set
        RoundEnvironment mockRoundEnvironment = Mock()
        AbstractProcessor sourcehawkFileEnforcerRegistryProcessor = new SourcehawkFileEnforcerRegistryProcessor(TestFileEnforcer.class.canonicalName)

        PackageElement mockPackageElement = Mock()
        Name mockPackageName = Mock()
        Filer mockFiler = Mock()
        Messager mockMessager = Mock()

        when:
        sourcehawkFileEnforcerRegistryProcessor.init(mockProcessingEnvironment)
        boolean process = sourcehawkFileEnforcerRegistryProcessor.process(annotations, mockRoundEnvironment)

        then:
        1 * mockRoundEnvironment.getElementsAnnotatedWith(mockTypeElement) >> [ mockPackageElement ]
        1 * mockPackageElement.getQualifiedName() >> mockPackageName
        1 * mockPackageName.toString() >> "com.optum.sourcehawk.enforcer.file"
        1 * mockRoundEnvironment.processingOver() >> true
        1 * mockProcessingEnvironment.getFiler() >> mockFiler
        1 * mockFiler.createSourceFile("com.optum.sourcehawk.enforcer.file.FileEnforcerRegistry", []) >> null
        1 * mockProcessingEnvironment.getMessager() >> mockMessager
        1 * mockMessager.printMessage(Diagnostic.Kind.ERROR, 'Unable to generate file enforcer registry: java.lang.NullPointerException')
        0 * _

        and:
        !process
    }

    def "getSupportedAnnotationTypes"() {
        given:
        Set<String> supportedAnnotationTypes = new SourcehawkFileEnforcerRegistryProcessor().getSupportedAnnotationTypes()

        expect:
        supportedAnnotationTypes
        supportedAnnotationTypes.size() == 1
        supportedAnnotationTypes[0] == SourcehawkFileEnforcerRegistry.canonicalName
    }

    def "getSupportedSourceVersion"() {
        expect:
        new SourcehawkFileEnforcerRegistryProcessor().getSupportedSourceVersion() == SourceVersion.latestSupported()
    }

    def "generateAlias"() {
        expect:
        new SourcehawkFileEnforcerRegistryProcessor().generateAlias(TestFileEnforcer) == "TEST-FILE-ENFORCER"
    }

}
