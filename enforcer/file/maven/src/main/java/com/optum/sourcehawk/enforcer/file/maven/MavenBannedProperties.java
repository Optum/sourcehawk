package com.optum.sourcehawk.enforcer.file.maven;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.optum.sourcehawk.enforcer.EnforcerResult;
import com.optum.sourcehawk.enforcer.file.AbstractFileEnforcer;
import com.optum.sourcehawk.enforcer.file.maven.utils.MavenPomParser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.val;
import org.apache.maven.model.Model;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * An enforcer which enforces that the provided properties are not set
 */
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = MavenBannedProperties.Builder.class)
@AllArgsConstructor(staticName = "banned")
public class MavenBannedProperties extends AbstractFileEnforcer {

    private static final String PARSE_ERROR = "Maven pom.xml parsing resulted in error";
    private static final String BANNED_PROPERTY_ERROR_TEMPLATE = "Banned maven property [%s = %s] found";

    /**
     * The properties which are banned from being set
     *
     * Example:
     *
     * - sonar.skip: true
     */
    @NonNull
    private final Map<String, String> bannedProperties;


    /** {@inheritDoc} */
    @Override
    protected EnforcerResult enforceInternal(@NonNull final InputStream actualFileInputStream) {
        return MavenPomParser.parse(actualFileInputStream)
                .map(Model::getProperties)
                .map(this::enforceBannedProperties)
                .orElseGet(() -> EnforcerResult.failed(PARSE_ERROR));
    }

    /**
     * Enforce the banned properties are not set
     *
     * @param properties the actual pom properties
     * @return the enforcer result
     */
    private EnforcerResult enforceBannedProperties(final Properties properties)  {
        val bannedMatches = properties.stringPropertyNames()
                .stream()
                .filter(bannedProperties::containsKey)
                .filter(propertyName -> bannedProperties.get(propertyName).equals(properties.getProperty(propertyName)))
                .map(propertyName -> Collections.singletonMap(propertyName, properties.getProperty(propertyName)))
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
       if (bannedMatches.isEmpty()) {
           return EnforcerResult.passed();
       }
       val messages = bannedMatches.entrySet()
               .stream()
               .map(property -> String.format(BANNED_PROPERTY_ERROR_TEMPLATE, property.getKey(), property.getValue()))
               .collect(Collectors.toSet());
       return EnforcerResult.builder()
               .passed(false)
               .messages(messages)
               .build();
    }

}
