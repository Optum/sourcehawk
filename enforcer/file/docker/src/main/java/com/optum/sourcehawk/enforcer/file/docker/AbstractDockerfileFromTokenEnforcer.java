package com.optum.sourcehawk.enforcer.file.docker;

import com.optum.sourcehawk.enforcer.EnforcerResult;
import com.optum.sourcehawk.enforcer.file.docker.utils.Dockerfile;
import com.optum.sourcehawk.enforcer.file.docker.utils.DockerfileParser;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Abstract file enforcer for Dockerfile FROM token related enforcing
 *
 * @author Brian Wyka
 */
abstract class AbstractDockerfileFromTokenEnforcer extends AbstractDockerfileTokenEnforcer {

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
     * Enforce the FROM image is as expected
     *
     * @param fromValue the FROM value
     * @return the enforcer result
     */
    @Override
    protected EnforcerResult enforceToken(final String fromValue) {
        return enforceFromToken(DockerfileParser.parseFromToken(fromValue));
    }

    /**
     * Enforce the value of the parsed FROM token
     *
     * @param fromToken the parsed FROM token
     * @return the enforcer result
     */
    protected abstract EnforcerResult enforceFromToken(final Dockerfile.FromToken fromToken);

}
