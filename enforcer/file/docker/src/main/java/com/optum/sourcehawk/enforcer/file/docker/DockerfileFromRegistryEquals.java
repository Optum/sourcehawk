package com.optum.sourcehawk.enforcer.file.docker;

import com.optum.sourcehawk.core.utils.StringUtils;
import com.optum.sourcehawk.enforcer.EnforcerResult;
import com.optum.sourcehawk.enforcer.file.docker.utils.Dockerfile;
import lombok.AllArgsConstructor;

/**
 * Enforce that the Dockerfile has a specific host in the FROM line
 *
 * @author Brian Wyka
 */
@AllArgsConstructor(staticName = "equals")
public class DockerfileFromRegistryEquals extends AbstractDockerfileFromTokenEnforcer {

    private static final String INCORRECT_FROM_MESSAGE = "Dockerfile FROM host [%s] does not equal [%s]";
    private static final String MISSING_HOST_MESSAGE = "Dockerfile FROM [%s] is missing host prefix";

    /**
     * The expected registry that should be included in the FROM line
     */
    private final String expectedFromRegistry;

    /**
     * Enforce the from host is as expected
     *
     * @param fromToken the FROM token
     * @return the enforcer result
     */
    @Override
    protected EnforcerResult enforceFromToken(final Dockerfile.FromToken fromToken) {
        if (fromToken.getRegistryHost() == null) {
            return EnforcerResult.failed(String.format(MISSING_HOST_MESSAGE, fromToken.getRawValue()));
        }
        if (StringUtils.equals(expectedFromRegistry, fromToken.getRegistryHost())) {
            return EnforcerResult.passed();
        }
        return EnforcerResult.failed(String.format(INCORRECT_FROM_MESSAGE, fromToken.getRegistryHost(), expectedFromRegistry));
    }

}
