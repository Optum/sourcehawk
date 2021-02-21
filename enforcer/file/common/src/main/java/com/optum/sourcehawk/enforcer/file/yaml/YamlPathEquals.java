package com.optum.sourcehawk.enforcer.file.yaml;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.jayway.jsonpath.JsonPath;
import com.optum.sourcehawk.enforcer.EnforcerResult;
import com.optum.sourcehawk.enforcer.file.AbstractFileEnforcer;
import com.optum.sourcehawk.enforcer.file.json.JsonPathEquals;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.val;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;

/**
 * An enforcer which is responsible for enforcing that a yaml file has a specific property with an expected value.  Under
 * the hood, this is delegating to {@link JsonPathEquals} after converting the yaml to json
 *
 * @see JsonPathEquals
 *
 * @author Brian Wyka
 */
@AllArgsConstructor(staticName = "equals")
public class YamlPathEquals extends AbstractFileEnforcer {

    private static final ObjectMapper YAML_MAPPER = YAMLMapper.builder().build();
    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder().build();

    /**
     * Key: The Yaml query to retrieve the value
     * @see JsonPath
     *
     * Value: The expected value which the query should evaluate to
     */
    private final Map<String, Object> expectations;

    /**
     * Create with a single path query and expected value
     *
     * @param yamlPathQuery the yaml path query
     * @param expectedValue the expected value
     * @return the enforcer
     */
    public static YamlPathEquals equals(final String yamlPathQuery, final Object expectedValue) {
        return YamlPathEquals.equals(Collections.singletonMap(yamlPathQuery, expectedValue));
    }

    /** {@inheritDoc} */
    @Override
    public EnforcerResult enforceInternal(@NonNull final InputStream actualFileInputStream) throws IOException {
        val yamlMap = YAML_MAPPER.readValue(actualFileInputStream, new TypeReference<Map<String, Object>>() {});
        val json = OBJECT_MAPPER.writeValueAsString(yamlMap);
        try (val jsonInputStream = new ByteArrayInputStream(json.getBytes(Charset.defaultCharset()))) {
            return JsonPathEquals.equals(expectations).enforce(jsonInputStream);
        }
    }

}
