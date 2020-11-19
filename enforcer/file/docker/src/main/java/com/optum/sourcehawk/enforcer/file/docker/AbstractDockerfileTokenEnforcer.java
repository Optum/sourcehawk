package com.optum.sourcehawk.enforcer.file.docker;

import com.optum.sourcehawk.core.utils.CollectionUtils;
import com.optum.sourcehawk.enforcer.EnforcerResult;
import com.optum.sourcehawk.enforcer.file.AbstractFileEnforcer;
import com.optum.sourcehawk.enforcer.file.docker.utils.DockerfileParser;
import lombok.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Abstract file enforcer for Dockerfile related enforcing
 *
 * @author Brian Wyka
 */
abstract class AbstractDockerfileTokenEnforcer extends AbstractFileEnforcer {

    protected static final String MISSING_MESSAGE = "Dockerfile is missing %s line";

    /** {@inheritDoc} */
    @Override
    protected EnforcerResult enforceInternal(@NonNull final InputStream actualFileInputStream) throws IOException {
        return enforceToken(actualFileInputStream, getToken(), this::enforceToken, getTokenValueFilter().orElseGet(() -> s -> true));
    }

    /**
     * Enforce a token value is as expected
     *
     * @param fileInputStream the file input stream
     * @param token the token
     * @param tokenValueEnforcer the token value enforcer
     * @param tokenValueFilter the token value filter (nullable)
     * @return the enforcer result, otherwise {@link Optional#empty()} if none found
     * @throws IOException if any error occurs parsing Dockerfile
     */
    protected EnforcerResult enforceToken(final InputStream fileInputStream, final String token,
                                          final Function<String, EnforcerResult> tokenValueEnforcer,
                                          final Predicate<String> tokenValueFilter) throws IOException {
        return Optional.of(DockerfileParser.collectTokenValues(fileInputStream, token))
                .filter(CollectionUtils::isNotEmpty)
                .map(tokenValues -> tokenValues.stream()
                        .filter(tokenValueFilter)
                        .map(tokenValueEnforcer)
                        .reduce(EnforcerResult.passed(), EnforcerResult::reduce))
                .orElseGet(() -> EnforcerResult.failed(String.format(MISSING_MESSAGE, token.trim())));
    }

    /**
     * Get the name of the token to enforce
     *
     * @return the token name
     */
    protected abstract String getToken();

    /**
     * Get the filter for the token value if anything should be omitted
     *
     * @return the token value filter or {@link Optional#empty()} if not applicable
     */
    protected abstract Optional<Predicate<String>> getTokenValueFilter();

    /**
     * Enforce the token value
     *
     * @param tokenValue the token value to enforce
     * @return the enforcer result
     */
    protected abstract EnforcerResult enforceToken(final String tokenValue);

}
