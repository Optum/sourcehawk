package com.optum.sourcehawk.enforcer.file.json;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.optum.sourcehawk.core.utils.StringUtils;
import com.optum.sourcehawk.enforcer.EnforcerResult;
import com.optum.sourcehawk.enforcer.ResolverResult;
import com.optum.sourcehawk.enforcer.file.AbstractFileEnforcer;
import com.optum.sourcehawk.enforcer.file.FileResolver;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.val;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * An enforcer implementation which enforces that the result of a JsonPath query equals a specific value
 *
 * @author Brian Wyka
 */
@AllArgsConstructor(staticName = "equals")
public class JsonValueEquals extends AbstractFileEnforcer implements FileResolver {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String READ_ERROR_TEMPLATE = "Reading or parsing file resulted in error [%s]";
    private static final String QUERY_ERROR_TEMPLATE = "Execution of pointer expression [%s] yielded error [%s]";
    private static final String MISSING_MESSAGE_TEMPLATE = "Execution of pointer expression [%s] yielded no result";
    private static final String NOT_EQUAL_MESSAGE_TEMPLATE = "Execution of pointer expression [%s] yielded result [%s] which is not equal to [%s]";
    private static final String UPDATE_MESSAGE_TEMPLATE = "Pointer expression [%s] has been updated with value [%s]";
    private static final String UPDATE_MISSING_MESSAGE_TEMPLATE = "Pointer expression [%s] which was missing, has been set with value [%s]";

    /**
     * Key: The JsonPointer expression to retrieve the value
     *
     * Value: The expected value which the query should evaluate to
     */
    private final Map<String, Object> expectations;

    /**
     * Create with a single path query and expected value
     *
     * @param jsonPointerExpression the JSON pointer expression
     * @param expectedValue the expected value
     * @return the enforcer
     */
    public static JsonValueEquals equals(final String jsonPointerExpression, final Object expectedValue) {
        return JsonValueEquals.equals(Collections.singletonMap(jsonPointerExpression, expectedValue));
    }

    /** {@inheritDoc} */
    @Override
    public EnforcerResult enforceInternal(@NonNull final InputStream actualFileInputStream) {
        final JsonNode jsonNode;
        try {
            jsonNode = OBJECT_MAPPER.readTree(actualFileInputStream);
        } catch (final IOException e) {
            return EnforcerResult.failed(String.format(READ_ERROR_TEMPLATE, e.getMessage()));
        }
        val messages = expectations.entrySet()
                .stream()
                .map(entry -> enforce(jsonNode, entry.getKey(), entry.getValue()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        return EnforcerResult.create(messages);
    }

    /**
     * Enforce individual json path queries with expected value
     *
     * @param jsonNode the JSON node
     * @param jsonPointerExpression  the JSON pointer expression
     * @param expectedValue the expected value
     * @return The message to be added, otherwise {@link Optional#empty()}
     */
    private static Optional<String> enforce(final JsonNode jsonNode, final String jsonPointerExpression, final Object expectedValue) {
        try {
            val actualJsonNode = jsonNode.at(JsonPointer.compile(jsonPointerExpression));
            if (actualJsonNode == null || actualJsonNode.isMissingNode()) {
                return Optional.of(String.format(MISSING_MESSAGE_TEMPLATE, jsonPointerExpression));
            }
            if (jsonNodeValueEquals(actualJsonNode, expectedValue)) {
                return Optional.empty();
            }
            return Optional.of(String.format(NOT_EQUAL_MESSAGE_TEMPLATE, jsonPointerExpression,  actualJsonNode.asText(), expectedValue));
        } catch (final Exception e) {
            return Optional.of(String.format(QUERY_ERROR_TEMPLATE, jsonPointerExpression, e.getMessage()));
        }
    }

    /** {@inheritDoc} */
    @Override
    public ResolverResult resolve(final @NonNull InputStream actualFileInputStream, final @NonNull Writer outputFileWriter) throws IOException {
        final JsonNode rootJsonNode;
        try {
            rootJsonNode = OBJECT_MAPPER.readTree(actualFileInputStream);
        } catch (final IOException e) {
            return ResolverResult.error(String.format(READ_ERROR_TEMPLATE, e.getMessage()));
        }
        val resolverResult = expectations.entrySet().stream()
                .map(entry -> resolve(rootJsonNode, entry.getKey(), entry.getValue()))
                .reduce(ResolverResult.builder().error(true).build(), ResolverResult::reduce);
        if (resolverResult.isUpdatesApplied()) {
            outputFileWriter.write(rootJsonNode.toPrettyString());
        }
        return resolverResult;
    }

    /**
     * Resolve an individual json path query with the expected value
     *
     * @param rootJsonNode the root JSON node
     * @param jsonPointerExpression the JSON pointer expression
     * @param expectedValue the expected value
     * @return the resolver result
     */
    private static ResolverResult resolve(final JsonNode rootJsonNode, final String jsonPointerExpression, final Object expectedValue) {
        try {
            val jsonPointer = JsonPointer.compile(jsonPointerExpression);
            val actualJsonNode = rootJsonNode.at(jsonPointer);
            if (actualJsonNode == null || actualJsonNode.isMissingNode()) {
                return resolveNodeNotFound(actualJsonNode, jsonPointerExpression, expectedValue);
            }
            if (jsonNodeValueEquals(actualJsonNode, expectedValue)) {
                return ResolverResult.NO_UPDATES;
            }
            val parentObjectNode = (ObjectNode) rootJsonNode.at(jsonPointer.head());
            val actualNodeName = actualJsonNode.asToken().name();
            if (expectedValue instanceof CharSequence) {
                parentObjectNode.put(actualNodeName, expectedValue.toString());
            } else if (expectedValue instanceof Number) {
                parentObjectNode.put(actualNodeName, (Number) expectedValue);
            }
            return ResolverResult.updatesApplied(String.format(UPDATE_MESSAGE_TEMPLATE, jsonPointerExpression, expectedValue));
        } catch (final Exception e) {
            return ResolverResult.error(String.format(QUERY_ERROR_TEMPLATE, jsonPointerExpression, e.getMessage()));
        }
    }

    /**
     * Resolve for situations where the node is not found
     *
     * @param actualJsonNode the actual JSON node
     * @param jsonPointerExpression the JSON pointer expression
     * @param expectedValue the expected value
     * @return the resolver result
     */
    private static ResolverResult resolveNodeNotFound(final JsonNode actualJsonNode, final String jsonPointerExpression, final Object expectedValue) {
        final String key;
        final Object value;
        if (jsonPointerExpression.contains("[")) {
            key = jsonPointerExpression.substring(2, jsonPointerExpression.indexOf('['));
            value = new Object[] { expectedValue };
        } else {
            key = jsonPointerExpression.substring(2);
            value = expectedValue;
        }
        documentContext.put("$", key, value);
        return ResolverResult.updatesApplied(String.format(UPDATE_MISSING_MESSAGE_TEMPLATE, jsonPointerExpression, expectedValue));
    }

    /**
     * Determine if the {@code jsonNode} value equals that of the {@code expectedValue}
     *
     * @param jsonNode the JSON node to retrieve the value from
     * @param expectedValue the expected value
     * @return true if they are equal, false otherwise
     */
    private static boolean jsonNodeValueEquals(final JsonNode jsonNode, final Object expectedValue) {
        return (expectedValue instanceof CharSequence && StringUtils.equals((CharSequence) expectedValue, jsonNode.textValue()))
                || (expectedValue instanceof Number && expectedValue == jsonNode.numberValue())
                || (expectedValue instanceof Boolean && (boolean) expectedValue == jsonNode.booleanValue())
                || Objects.equals(expectedValue.toString(), jsonNode.asText());
    }

}
