package com.optum.sourcehawk.enforcer.file.json;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * An enforcer implementation which enforces that the result of a JsonPath query equals a specific value
 *
 * @author Brian Wyka
 */
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = JsonValueEquals.Builder.class)
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
     * <p>
     * Value: The expected value which the query should evaluate to
     */
    private final Map<String, Object> expectations;

    /**
     * Create with a single path query and expected value
     *
     * @param jsonPointerExpression the JSON pointer expression
     * @param expectedValue         the expected value
     * @return the enforcer
     */
    public static JsonValueEquals equals(final String jsonPointerExpression, final Object expectedValue) {
        return JsonValueEquals.equals(Collections.singletonMap(jsonPointerExpression, expectedValue));
    }

    /**
     * {@inheritDoc}
     */
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
     * @param jsonNode              the JSON node
     * @param jsonPointerExpression the JSON pointer expression
     * @param expectedValue         the expected value
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
            return Optional.of(String.format(NOT_EQUAL_MESSAGE_TEMPLATE, jsonPointerExpression, actualJsonNode.asText(), expectedValue));
        } catch (final Exception e) {
            return Optional.of(String.format(QUERY_ERROR_TEMPLATE, jsonPointerExpression, e.getMessage()));
        }
    }

    /**
     * {@inheritDoc}
     */
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
     * @param rootJsonNode          the root JSON node
     * @param jsonPointerExpression the JSON pointer expression
     * @param expectedValue         the expected value
     * @return the resolver result
     */
    private static ResolverResult resolve(final JsonNode rootJsonNode, final String jsonPointerExpression, final Object expectedValue) {
        try {
            val jsonPointer = JsonPointer.compile(jsonPointerExpression);
            val actualJsonNode = rootJsonNode.at(jsonPointer);
            final String resolverResultMessage;
            if (actualJsonNode.isMissingNode()) {
                resolverResultMessage = String.format(UPDATE_MISSING_MESSAGE_TEMPLATE, jsonPointerExpression, expectedValue);
            } else {
                resolverResultMessage = String.format(UPDATE_MESSAGE_TEMPLATE, jsonPointerExpression, expectedValue);
            }
            if (jsonNodeValueEquals(actualJsonNode, expectedValue)) {
                return ResolverResult.NO_UPDATES;
            }
            val parentNode = rootJsonNode.at(jsonPointer.head());
            if (parentNode instanceof ObjectNode) {
                updateObjectNodeValue((ObjectNode) parentNode, jsonPointer.last().toString().substring(1), expectedValue);
            } else if (parentNode instanceof ArrayNode) {
                updateArrayNodeValue((ArrayNode) parentNode, Integer.parseInt(jsonPointer.last().toString().substring(1)), expectedValue, actualJsonNode.isMissingNode());
            } else {
                return ResolverResult.error("Update not supported for given pointer expression");
            }
            return ResolverResult.updatesApplied(resolverResultMessage);
        } catch (final Exception e) {
            return ResolverResult.error(String.format(QUERY_ERROR_TEMPLATE, jsonPointerExpression, e.getMessage()));
        }
    }

    /**
     * Update the actual node with the expected value
     *
     * @param parentObjectNode the parent object node
     * @param childNodeName    the child node name
     * @param expectedValue    the expected value
     */
    private static void updateObjectNodeValue(final ObjectNode parentObjectNode, final String childNodeName, final Object expectedValue) {
        if (expectedValue instanceof Boolean) {
            parentObjectNode.put(childNodeName, (boolean) expectedValue);
        } else if (expectedValue instanceof Integer) {
            parentObjectNode.put(childNodeName, (int) expectedValue);
        } else if (expectedValue instanceof Long) {
            parentObjectNode.put(childNodeName, (long) expectedValue);
        } else if (expectedValue instanceof Short) {
            parentObjectNode.put(childNodeName, (short) expectedValue);
        } else if (expectedValue instanceof BigDecimal) {
            parentObjectNode.put(childNodeName, (BigDecimal) expectedValue);
        } else if (expectedValue instanceof BigInteger) {
            parentObjectNode.put(childNodeName, (BigInteger) expectedValue);
        } else {
            parentObjectNode.put(childNodeName, expectedValue.toString());
        }
    }

    /**
     * Update the actual node with the expected value
     *
     * @param parentArrayNode the parent object node
     * @param actualNodeIndex the actual node index in the array
     * @param expectedValue   the expected value
     * @param missing         true if node does not currently exist, false otherwise
     */
    @SuppressWarnings("squid:S3776")
    private static void updateArrayNodeValue(final ArrayNode parentArrayNode, final int actualNodeIndex, final Object expectedValue, final boolean missing) {
        if (expectedValue instanceof Boolean) {
            if (missing) {
                parentArrayNode.insert(actualNodeIndex, (boolean) expectedValue);
            } else {
                parentArrayNode.set(actualNodeIndex, JsonNodeFactory.instance.booleanNode((boolean) expectedValue));
            }
        } else if (expectedValue instanceof Integer) {
            if (missing) {
                parentArrayNode.insert(actualNodeIndex, (int) expectedValue);
            } else {
                parentArrayNode.set(actualNodeIndex, JsonNodeFactory.instance.numberNode((int) expectedValue));
            }
        } else if (expectedValue instanceof Long) {
            if (missing) {
                parentArrayNode.insert(actualNodeIndex, (long) expectedValue);
            } else {
                parentArrayNode.set(actualNodeIndex, JsonNodeFactory.instance.numberNode((long) expectedValue));
            }
        } else if (expectedValue instanceof Short) {
            if (missing) {
                parentArrayNode.insert(actualNodeIndex, (short) expectedValue);
            } else {
                parentArrayNode.set(actualNodeIndex, JsonNodeFactory.instance.numberNode((short) expectedValue));
            }
        } else if (expectedValue instanceof BigDecimal) {
            if (missing) {
                parentArrayNode.insert(actualNodeIndex, (BigDecimal) expectedValue);
            } else {
                parentArrayNode.set(actualNodeIndex, JsonNodeFactory.instance.numberNode((BigDecimal) expectedValue));
            }
        } else if (expectedValue instanceof BigInteger) {
            if (missing) {
                parentArrayNode.insert(actualNodeIndex, (BigInteger) expectedValue);
            } else {
                parentArrayNode.set(actualNodeIndex, JsonNodeFactory.instance.numberNode((BigInteger) expectedValue));
            }
        } else {
            if (missing) {
                parentArrayNode.insert(actualNodeIndex, expectedValue.toString());
            } else {
                parentArrayNode.set(actualNodeIndex, JsonNodeFactory.instance.textNode(expectedValue.toString()));
            }
        }
    }

    /**
     * Determine if the {@code jsonNode} value equals that of the {@code expectedValue}
     *
     * @param jsonNode      the JSON node to retrieve the value from
     * @param expectedValue the expected value
     * @return true if they are equal, false otherwise
     */
    private static boolean jsonNodeValueEquals(final JsonNode jsonNode, final Object expectedValue) {
        return (expectedValue instanceof CharSequence && equalsOrMatches(jsonNode, (CharSequence) expectedValue))
                || (expectedValue instanceof Number && expectedValue == jsonNode.numberValue())
                || (expectedValue instanceof Boolean && (boolean) expectedValue == jsonNode.booleanValue())
                || Objects.equals(expectedValue.toString(), jsonNode.asText())
                || Pattern.compile(jsonNode.asText()).matcher(expectedValue.toString()).matches();
    }

    private static boolean equalsOrMatches(JsonNode jsonNode, CharSequence expectedValue) {
        return StringUtils.equals(expectedValue, jsonNode.textValue())
                || Pattern.compile(expectedValue.toString()).matcher(jsonNode.textValue()).matches();
    }
}
