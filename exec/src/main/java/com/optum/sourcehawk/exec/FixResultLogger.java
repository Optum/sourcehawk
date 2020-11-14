package com.optum.sourcehawk.exec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;
import com.kjetland.jackson.jsonSchema.SubclassesResolver;
import com.kjetland.jackson.jsonSchema.SubclassesResolverImpl;
import com.optum.sourcehawk.core.scan.FixResult;
import com.optum.sourcehawk.core.scan.OutputFormat;
import com.optum.sourcehawk.enforcer.file.FileEnforcer;
import com.optum.sourcehawk.enforcer.file.common.StringPropertyEquals;
import com.optum.sourcehawk.enforcer.file.json.JsonPathEquals;
import com.optum.sourcehawk.enforcer.file.maven.MavenBannedProperties;
import com.optum.sourcehawk.protocol.FileProtocol;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.util.Arrays;

/**
 * A logger for fix results
 *
 * @see FixResult
 * @see OutputFormat
 *
 * @author Brian Wyka
 */
@UtilityClass
class FixResultLogger {

    /**
     * Log the result of the fix in the specified format
     *
     * @param fixResult the fix result
     * @param execOptions the scan options
     */
    @SuppressWarnings("squid:S2629")
    void log(final FixResult fixResult, final ExecOptions execOptions) {
        // TODO: implement logging for fix results
    }

    public static void main(String[] args) throws IOException {
        final SubclassesResolver resolver = new SubclassesResolverImpl()
                .withPackagesToScan(Arrays.asList(
                        "com.optum.sourcehawk.enforcer.file"
                ));
        final JsonSchemaGenerator jsonSchemaGenerator = new JsonSchemaGenerator(ConfigurationReader.CONFIGURATION_DESERIALIZER);
        jsonSchemaGenerator.com$kjetland$jackson$jsonSchema$JsonSchemaGenerator$$config.withSubclassesResolver(resolver);
        System.out.print(new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(jsonSchemaGenerator.generateJsonSchema(FileEnforcer.class)));
    }

}
