package com.optum.sourcehawk.enforcer.file.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.optum.sourcehawk.core.utils.StringUtils;
import com.optum.sourcehawk.enforcer.file.FileResolver;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * An enforcer implementation which enforces that the result of a JsonPath query does not equal a specific value
 *
 * @author Brian Wyka
 * @author Christian Oestreich
 */
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = JsonValueNotEquals.Builder.class)
@AllArgsConstructor(staticName = "equals")
public class JsonValueNotEquals extends AbstractJsonValue implements FileResolver {

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
    public static JsonValueNotEquals equals(final String jsonPointerExpression, final Object expectedValue) {
        return JsonValueNotEquals.equals(Collections.singletonMap(jsonPointerExpression, expectedValue));
    }

    /**
     * Determine if the {@code jsonNode} value equals that of the {@code expectedValue}
     *
     * @param jsonNode      the JSON node to retrieve the value from
     * @param expectedValue the expected value
     * @return true if they are equal, false otherwise
     */
    protected boolean jsonNodeValueEquals(final JsonNode jsonNode, final Object expectedValue) {
        if (expectedValue instanceof CharSequence) {
            return !StringUtils.equals((CharSequence) expectedValue, jsonNode.textValue());
        }
        if (expectedValue instanceof Number) {
            return expectedValue != jsonNode.numberValue();
        }
        if (expectedValue instanceof Boolean) {
            return (boolean) expectedValue != jsonNode.booleanValue();
        }
        return !Objects.equals(expectedValue.toString(), jsonNode.asText());
    }

    @Override
    protected Map<String, Object> getExpectations() {
        return this.expectations;
    }

    @Override
    protected String getMessageTemplate() {
        return "Execution of pointer expression [%s] yielded result [%s] which does equal [%s]";
    }

    protected boolean skipMissingNode(){
        return true;
    }
}
