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
 * An enforcer implementation which enforces that the result of a JsonPath query equals a specific value
 *
 * @author Brian Wyka
 * @author Christian Oestreich
 */
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = JsonValueEquals.Builder.class)
@AllArgsConstructor(staticName = "equals")
public class JsonValueEquals extends AbstractJsonValue implements FileResolver {

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

    @Override
    protected Map<String, Object> getExpectations() {
        return this.expectations;
    }

    protected String getMessageTemplate(){
        return "Execution of pointer expression [%s] yielded result [%s] which is not equal to [%s]";
    }
}
