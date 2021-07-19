package com.optum.sourcehawk.enforcer.file.http;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.optum.sourcehawk.enforcer.EnforcerResult;
import com.optum.sourcehawk.enforcer.file.AbstractFileEnforcer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.val;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

/**
 * A file enforcer which makes an HTTP request to the configured URL
 * to validate the file contents
 *
 * @author brianwyka
 */
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = ContentHttpValidated.Builder.class)
@AllArgsConstructor(staticName = "validated")
public class ContentHttpValidated extends AbstractFileEnforcer {

    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final String HTTP_RESPONSE_VALIDATION_MESSAGE_TEMPLATE = "Validation failed with reason: %s";
    private static final String HTTP_RESPONSE_ERROR_MESSAGE_TEMPLATE = "HTTP [%s] error performing validation: %s";

    /**
     * The URL in which to make a POST request, to validate the file contents
     */
    private final String url;

    /**
     * The amount of time allowed to connect to URL before timing out
     */
    private final int connectTimeout;

    /**
     * The amount of time allowed to read response from URL before timing out
     */
    private final int readTimeout;

    /** {@inheritDoc} */
    @Override
    protected EnforcerResult enforceInternal(final @NonNull InputStream actualFileInputStream) throws IOException {
        val httpUrlConnection = (HttpURLConnection) new URL(url).openConnection();
        httpUrlConnection.setDoOutput(true);
        httpUrlConnection.setRequestMethod("POST");
        httpUrlConnection.setRequestProperty("Content-Type", "text/plain");
        httpUrlConnection.setRequestProperty("Accept", "text/plain");
        httpUrlConnection.setConnectTimeout(connectTimeout == 0 ? 500 : connectTimeout);
        httpUrlConnection.setReadTimeout(readTimeout == 0 ? 500 : readTimeout);
        try (val outputStream = httpUrlConnection.getOutputStream()) {
            val buffer = new byte[DEFAULT_BUFFER_SIZE];
            int read;
            while ((read = actualFileInputStream.read(buffer, 0, DEFAULT_BUFFER_SIZE)) >= 0) {
                outputStream.write(buffer, 0, read);
            }
        }
        val responseCode = String.valueOf(httpUrlConnection.getResponseCode());
        if (responseCode.startsWith("2")) {
            return EnforcerResult.passed();
        }
        try (val errorStream = httpUrlConnection.getErrorStream();
             val bufferedReader = new BufferedReader(new InputStreamReader(errorStream))) {
            val responseMessage = bufferedReader.lines()
                .collect(Collectors.joining());
            if (responseCode.startsWith("4")) {
                return EnforcerResult.failed(String.format(HTTP_RESPONSE_VALIDATION_MESSAGE_TEMPLATE, responseMessage));
            } else {
                return EnforcerResult.failed(String.format(HTTP_RESPONSE_ERROR_MESSAGE_TEMPLATE, responseCode, responseMessage));
            }
        }
    }

}
