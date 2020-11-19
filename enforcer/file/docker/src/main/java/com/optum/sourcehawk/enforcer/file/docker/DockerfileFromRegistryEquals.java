package com.optum.sourcehawk.enforcer.file.docker;

import com.optum.sourcehawk.core.utils.StringUtils;
import com.optum.sourcehawk.enforcer.EnforcerResult;
import com.optum.sourcehawk.enforcer.file.docker.utils.DockerfileParser;
import lombok.AllArgsConstructor;
import lombok.val;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Enforce that the Dockerfile has a specific host in the FROM line
 *
 * @author Brian Wyka
 */
@AllArgsConstructor(staticName = "equals")
public class DockerfileFromRegistryEquals extends AbstractDockerfileTokenEnforcer {

    private static final String INCORRECT_FROM_MESSAGE = "Dockerfile FROM host [%s] does not equal [%s]";
    private static final String MISSING_HOST_MESSAGE = "Dockerfile FROM [%s] is missing host prefix";

    /**
     * The expected registry that should be included in the FROM line
     */
    private final String expectedFromRegistry;

    /** {@inheritDoc} */
    @Override
    protected String getToken() {
        return DockerfileParser.FROM_TOKEN;
    }

    /** {@inheritDoc} */
    @Override
    protected Optional<Predicate<String>> getTokenValueFilter() {
        return Optional.of(tokenValue -> !DockerfileParser.FROM_SCRATCH.equals(tokenValue));
    }

    /**
     * Enforce the from host is as expected
     *
     * @param fromValue the FROM value
     * @return the enforcer result
     */
    @Override
    protected EnforcerResult enforceToken(final String fromValue) {
        if (fromValue.contains("/")) {
            val fromPieces = fromValue.split("/");
            if (fromPieces[0].contains(".")) {
                if (StringUtils.equals(expectedFromRegistry, fromPieces[0])) {
                    return EnforcerResult.passed();
                }
                return EnforcerResult.failed(String.format(INCORRECT_FROM_MESSAGE, fromPieces[0], expectedFromRegistry));
            }
        }
        return EnforcerResult.failed(String.format(MISSING_HOST_MESSAGE, fromValue));
    }

}
