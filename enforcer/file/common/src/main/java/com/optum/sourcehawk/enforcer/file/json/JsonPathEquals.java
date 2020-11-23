package com.optum.sourcehawk.enforcer.file.json;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.internal.JsonFormatter;
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
 * @see JsonPath
 *
 * @author Brian Wyka
 */
@AllArgsConstructor(staticName = "equals")
public class JsonPathEquals extends AbstractFileEnforcer implements FileResolver {

    private static final String PARSE_ERROR_TEMPLATE = "Query parsing resulted in error [%s]";
    private static final String QUERY_ERROR_TEMPLATE = "Execution of query [%s] yielded error [%s]";
    private static final String MISSING_MESSAGE_TEMPLATE = "Execution of query [%s] yielded no result";
    private static final String NOT_EQUAL_MESSAGE_TEMPLATE = "Execution of query [%s] yielded result [%s] which is not equal to [%s]";
    private static final String UPDATE_MESSAGE_TEMPLATE = "Query [%s] has been updated with value [%s]";
    private static final String UPDATE_MISSING_MESSAGE_TEMPLATE = "Query [%s] which was missing, has been updated with value [%s]";

    /**
     * Key: The JsonPath query to retrieve the value
     * @see JsonPath
     *
     * Value: The expected value which the query should evaluate to
     */
    private final Map<String, Object> expectations;

    /**
     * Create with a single path query and expected value
     *
     * @param jsonPathQuery the json path query
     * @param expectedValue the expected value
     * @return the enforcer
     */
    public static JsonPathEquals equals(final String jsonPathQuery, final Object expectedValue) {
        return JsonPathEquals.equals(Collections.singletonMap(jsonPathQuery, expectedValue));
    }

    /** {@inheritDoc} */
    @Override
    public EnforcerResult enforceInternal(@NonNull final InputStream actualFileInputStream) {
        final DocumentContext documentContext;
        try {
            documentContext = JsonPath.parse(actualFileInputStream);
        } catch (final Exception e) {
            return EnforcerResult.failed(String.format(PARSE_ERROR_TEMPLATE, e.getMessage()));
        }
        val messages = expectations.entrySet()
                .stream()
                .map(entry -> enforce(documentContext, entry.getKey(), entry.getValue()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        return EnforcerResult.create(messages);
    }

    /**
     * Enforce individual json path queries with expected value
     *
     * @param documentContext the documentation context
     * @param jsonPathQuery the json path query
     * @param expectedValue the expected value
     * @return The message to be added, otherwise {@link Optional#empty()}
     */
    private static Optional<String> enforce(final DocumentContext documentContext, final String jsonPathQuery, final Object expectedValue) {
        try {
            val actualValue = documentContext.read(JsonPath.compile(jsonPathQuery));
            if (Objects.equals(expectedValue, actualValue)) {
                return Optional.empty();
            }
            return Optional.of(String.format(NOT_EQUAL_MESSAGE_TEMPLATE, jsonPathQuery, actualValue, expectedValue));
        } catch (final PathNotFoundException e) {
            return Optional.of(String.format(MISSING_MESSAGE_TEMPLATE, jsonPathQuery));
        } catch (final Exception e) {
            return Optional.of(String.format(QUERY_ERROR_TEMPLATE, jsonPathQuery, e.getMessage()));
        }
    }

    /** {@inheritDoc} */
    @Override
    public ResolverResult resolve(final @NonNull InputStream actualFileInputStream, final @NonNull Writer outputFileWriter) throws IOException {
        final DocumentContext documentContext;
        try {
            documentContext = JsonPath.parse(actualFileInputStream);
        } catch (final Exception e) {
            throw new IOException(e);
        }
        val resolverResult = expectations.entrySet().stream()
                .map(entry -> resolve(documentContext, entry.getKey(), entry.getValue()))
                .reduce(ResolverResult.builder().error(true).build(), ResolverResult::reduce);
        if (resolverResult.isUpdatesApplied()) {
            outputFileWriter.write(JsonFormatter.prettyPrint(documentContext.jsonString()));
        }
        return resolverResult;
    }

    /**
     * Resolve an individual json path query with the expected value
     *
     * @param documentContext the document context
     * @param jsonPathQuery the JSON path query
     * @param expectedValue the expected value
     * @return the resolver result
     */
    private static ResolverResult resolve(final DocumentContext documentContext, final String jsonPathQuery, final Object expectedValue) {
        final JsonPath jsonPath;
        try {
            jsonPath = JsonPath.compile(jsonPathQuery);
            val actualValue = documentContext.read(jsonPath);
            if (Objects.equals(expectedValue, actualValue)) {
                return ResolverResult.NO_UPDATES;
            }
            documentContext.set(jsonPath, expectedValue);
            return ResolverResult.updatesApplied(String.format(UPDATE_MESSAGE_TEMPLATE, jsonPathQuery, expectedValue));
        } catch (final PathNotFoundException e) {
            return resolvePathNotFound(documentContext, jsonPathQuery, expectedValue);
        } catch (final Exception e) {
            return ResolverResult.error(String.format(QUERY_ERROR_TEMPLATE, jsonPathQuery, e.getMessage()));
        }
    }

    /**
     * Resolve for situations where the path is not found
     *
     * @param documentContext the document context
     * @param jsonPathQuery the JSON path query
     * @param expectedValue the expected value
     * @return the resolver result
     */
    private static ResolverResult resolvePathNotFound(final DocumentContext documentContext, final String jsonPathQuery, final Object expectedValue) {
        final String key;
        final Object value;
        if (jsonPathQuery.contains("[")) {
            key = jsonPathQuery.substring(2, jsonPathQuery.indexOf('['));
            value = new Object[] { expectedValue };
        } else {
            key = jsonPathQuery.substring(2);
            value = expectedValue;
        }
        documentContext.put("$", key, value);
        return ResolverResult.updatesApplied(String.format(UPDATE_MISSING_MESSAGE_TEMPLATE, jsonPathQuery, expectedValue));
    }

}
