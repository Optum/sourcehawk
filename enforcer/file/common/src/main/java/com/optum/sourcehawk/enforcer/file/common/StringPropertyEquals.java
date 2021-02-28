package com.optum.sourcehawk.enforcer.file.common;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.optum.sourcehawk.core.utils.ModifiableProperties;
import com.optum.sourcehawk.core.utils.StringUtils;
import com.optum.sourcehawk.enforcer.EnforcerResult;
import com.optum.sourcehawk.enforcer.ResolverResult;
import com.optum.sourcehawk.enforcer.file.AbstractFileEnforcer;
import com.optum.sourcehawk.enforcer.file.FileResolver;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.val;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Objects;
import java.util.Properties;

/**
 * An enforcer which is responsible for enforcing that a specific property has an expected value
 *
 * @see Properties
 *
 * @author Brian Wyka
 */
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = StringPropertyEquals.Builder.class)
@AllArgsConstructor(staticName = "equals")
public class StringPropertyEquals extends AbstractFileEnforcer implements FileResolver {

    private static final String MISSING_MESSAGE_TEMPLATE = "Property [%s] is missing";
    private static final String NULL_MESSAGE_TEMPLATE = "Property [%s] is null";
    private static final String NOT_EQUAL_MESSAGE_TEMPLATE = "Property [%s] with value [%s] does not equal [%s]";
    private static final String ADD_MESSAGE_TEMPLATE = "Property [%s] has been added with value [%s]";
    private static final String UPDATE_MESSAGE_TEMPLATE = "Property [%s] with value [%s] has been updated to value [%s]";

    /**
     * The name of the property to evaluate
     */
    private final String propertyName;

    /**
     * The expected value of the property
     */
    private final String expectedPropertyValue;

    /** {@inheritDoc} */
    @Override
    public EnforcerResult enforceInternal(@NonNull final InputStream actualFileInputStream) throws IOException {
        val actualProperties = new Properties();
        actualProperties.load(actualFileInputStream);
        if (!actualProperties.containsKey(propertyName)) {
            return EnforcerResult.failed(String.format(MISSING_MESSAGE_TEMPLATE, propertyName));
        }
        val actualPropertyValue = actualProperties.getProperty(propertyName);
        if (Objects.isNull(actualPropertyValue) || "null".equalsIgnoreCase(actualPropertyValue)) {
            return EnforcerResult.failed(String.format(NULL_MESSAGE_TEMPLATE, propertyName));
        }
        if (!StringUtils.equals(expectedPropertyValue, actualPropertyValue)) {
            return EnforcerResult.failed(String.format(NOT_EQUAL_MESSAGE_TEMPLATE, propertyName, actualPropertyValue, expectedPropertyValue));
        }
        return EnforcerResult.passed();
    }

    /** {@inheritDoc} */
    @Override
    public ResolverResult resolve(final @NonNull InputStream fileInputStream, final @NonNull Writer outputFileWriter) throws IOException {
        val modifiableProperties = ModifiableProperties.create(fileInputStream);
        val actualPropertyValue = modifiableProperties.getProperty(propertyName);
        if (modifiableProperties.containsKey(propertyName) && StringUtils.equals(expectedPropertyValue, actualPropertyValue)) {
            return ResolverResult.NO_UPDATES;
        }
        final String message;
        if (modifiableProperties.containsKey(propertyName)) {
            message = String.format(UPDATE_MESSAGE_TEMPLATE, propertyName, actualPropertyValue, expectedPropertyValue);
            modifiableProperties.setProperty(propertyName, expectedPropertyValue);
        } else {
            message = String.format(ADD_MESSAGE_TEMPLATE, propertyName, expectedPropertyValue);
            modifiableProperties.add(propertyName, expectedPropertyValue);
        }
        modifiableProperties.store(outputFileWriter, null);
        return ResolverResult.updatesApplied(message);
    }

}
