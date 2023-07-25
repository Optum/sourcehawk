package com.optum.sourcehawk.enforcer.file.http;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.optum.sourcehawk.enforcer.EnforcerResult;
import com.optum.sourcehawk.enforcer.file.AbstractFileEnforcer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
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
    @Default
    private final int connectTimeout = 500;

    /**
     * The amount of time allowed to read the response from URL before timing out
     */
    @Default
    private final int readTimeout = 500;
    /**
     * The amount of time allowed to read the response from URL before timing out
     */
    @Default
    private final String method = "POST";

    /**
     * The amount of time allowed to read the response from URL before timing out
     */
    @Default
    private final String contentTypeHeader = "text/plain";

    /**
     * The amount of time allowed to read the response from URL before timing out
     */
    @Default
    private final String acceptHeader = "text/plain";


    /** {@inheritDoc} */
    @Override
    protected EnforcerResult enforceInternal(final @NonNull InputStream actualFileInputStream) throws IOException {
        val httpUrlConnection = (HttpURLConnection) new URL(url).openConnection();
        httpUrlConnection.setDoOutput(true);
        httpUrlConnection.setRequestMethod(method);
        httpUrlConnection.setRequestProperty("Content-Type", contentTypeHeader);
        httpUrlConnection.setRequestProperty("Accept", acceptHeader);
        httpUrlConnection.setConnectTimeout(connectTimeout);
        httpUrlConnection.setReadTimeout(readTimeout);
        try (val outputStream = httpUrlConnection.getOutputStream()) {
            val buffer = new byte[DEFAULT_BUFFER_SIZE];
            int read;
            while ((read = actualFileInputStream.read(buffer, 0, DEFAULT_BUFFER_SIZE)) >= 0) {
                outputStream.write(buffer, 0, read);
            }
        }
        val responseCode = httpUrlConnection.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
            return EnforcerResult.passed();
        }
        try (val errorStream = httpUrlConnection.getErrorStream();
             val bufferedReader = new BufferedReader(new InputStreamReader(errorStream))) {
            val responseMessage = bufferedReader.lines()
                .collect(Collectors.joining());
            if (responseCode >= 400 && responseCode < 500) {
                return EnforcerResult.failed(String.format(HTTP_RESPONSE_VALIDATION_MESSAGE_TEMPLATE, responseMessage));
            } else {
                return EnforcerResult.failed(String.format(HTTP_RESPONSE_ERROR_MESSAGE_TEMPLATE, responseCode, responseMessage));
            }
        }
    }

}
