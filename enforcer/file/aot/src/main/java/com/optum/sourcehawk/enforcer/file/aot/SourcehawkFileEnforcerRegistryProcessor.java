package com.optum.sourcehawk.enforcer.file.aot;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import org.reflections.Reflections;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An annotation processor for generating a file enforcer registry
 *
 * @author Brian Wyka
 */
public class SourcehawkFileEnforcerRegistryProcessor extends AbstractProcessor {

    private static final String FILE_ENFORCER_PACKAGE = "com.optum.sourcehawk.enforcer.file";
    private static final String FILE_ENFORCER_CLASS_NAME = FILE_ENFORCER_PACKAGE + ".FileEnforcer";
    private final Class<?> fileEnforcerClass;
    private final Map<String, Class<?>> fileEnforcerClasses = new HashMap<>();

    /**
     * Constructs an instance of this processor
     */
    public SourcehawkFileEnforcerRegistryProcessor() {
        this(FILE_ENFORCER_CLASS_NAME);
    }

    /**
     * Create the processor based on file enforcer class name
     *
     * @param fileEnforcerClassName the file enforcer class name
     */
    private SourcehawkFileEnforcerRegistryProcessor(final String fileEnforcerClassName) {
        super();
        try {
            this.fileEnforcerClass = Class.forName(fileEnforcerClassName);
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException("Improper classpath setup, make sure the compiler has correct dependencies", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnvironment) {
        annotations.stream()
                .map(roundEnvironment::getElementsAnnotatedWith)
                .flatMap(Collection::stream)
                .map(PackageElement.class::cast)
                .map(this::createFileEnforcers)
                .filter(Objects::nonNull)
                .forEach(fileEnforcerClasses::putAll);
        if (roundEnvironment.processingOver()) {
            try {
                createFileEnforcerRegistryJavaFile(fileEnforcerClasses).writeTo(processingEnv.getFiler());
            } catch (final Exception e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unable to generate file enforcer registry: " + e.toString());
                return false;
            }
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(SourcehawkFileEnforcerRegistry.class.getCanonicalName());
    }

    /** {@inheritDoc} */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * Create the file enforcers
     *
     * @param packageElement the package element
     * @return the file enforcer entry
     */
    private Map<String, Class<?>> createFileEnforcers(final PackageElement packageElement) {
        return Stream.of(packageElement)
                .map(PackageElement::getQualifiedName)
                .map(String::valueOf)
                .map(Reflections::new)
                .map(reflections -> reflections.getSubTypesOf(fileEnforcerClass))
                .flatMap(Collection::stream)
                .filter(clazz -> !java.lang.reflect.Modifier.isAbstract(clazz.getModifiers()))
                .collect(Collectors.toMap(SourcehawkFileEnforcerRegistryProcessor::generateAlias, Function.identity()));
    }

    /**
     * Generate an alias for the file enforcer class
     *
     * @param fileEnforcerClass the file enforcer class
     * @return the alias
     */
    private static String generateAlias(final Class<?> fileEnforcerClass) {
        return fileEnforcerClass.getSimpleName().charAt(0) + fileEnforcerClass.getSimpleName().substring(1).replaceAll("(?=[A-Z][a-z])", "-").toUpperCase();
    }

    /**
     * Create the registry java file based on the file enforcers
     *
     * @param fileEnforcers the file enforcers
     * @return the java file to generate
     */
    private JavaFile createFileEnforcerRegistryJavaFile(final Map<String, Class<?>> fileEnforcers) {
        final TypeName classTypeName = ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(fileEnforcerClass));
        final TypeName fieldTypeName = ParameterizedTypeName.get(ClassName.get(Map.class), TypeName.get(String.class), classTypeName);
        final FieldSpec fieldSpec = FieldSpec.builder(fieldTypeName, "FILE_ENFORCERS")
                .addJavadoc("Mapping of enforcer classes keyed by alias")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .build();
        final ParameterSpec methodParameterSpec =  ParameterSpec.builder(String.class, "alias", Modifier.FINAL).build();
        final MethodSpec getEnforcerByAliasMethodSpec = MethodSpec.methodBuilder("getEnforcerByAlias")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .returns(ParameterizedTypeName.get(ClassName.get(Optional.class), classTypeName))
                .addJavadoc("Get the file enforcer for the provided alias\n\n")
                .addJavadoc("@param alias the file enforcer alias")
                .addJavadoc("@return the file enforcer class")
                .addParameter(methodParameterSpec)
                .addStatement("return $T.ofNullable($L.get($L))", Optional.class, fieldSpec.name, methodParameterSpec.name)
                .build();
        final MethodSpec methodSpec = MethodSpec.methodBuilder("getEnforcers")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .returns(fieldTypeName)
                .addJavadoc("Get the file enforcers\n\n")
                .addJavadoc("@return the file enforcers")
                .addStatement("return $L", fieldSpec.name)
                .build();
        final CodeBlock.Builder staticCodeBlockBuilder = CodeBlock.builder()
                .addStatement("$L = new $T<>()", fieldSpec.name, HashMap.class);
        fileEnforcers.forEach((alias, clazz) -> staticCodeBlockBuilder.addStatement("$L.put($S, $T.class)", fieldSpec.name, alias, clazz));
        final TypeSpec classTypeSpec = TypeSpec.classBuilder("FileEnforcerRegistry")
                .addJavadoc("A registry for all file enforcers\n\n")
                .addJavadoc("@author Brian Wyka")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(fieldSpec)
                .addStaticBlock(staticCodeBlockBuilder.build())
                .addMethod(getEnforcerByAliasMethodSpec)
                .addMethod(methodSpec)
                .build();
        return JavaFile.builder(FILE_ENFORCER_PACKAGE, classTypeSpec)
                .addFileComment("Auto-Generated by sourcehawk-enforcer-file-aot")
                .build();
    }

}