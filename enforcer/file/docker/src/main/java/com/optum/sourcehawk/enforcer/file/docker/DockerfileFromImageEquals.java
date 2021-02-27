package com.optum.sourcehawk.enforcer.file.docker;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.optum.sourcehawk.enforcer.EnforcerResult;
import com.optum.sourcehawk.enforcer.file.docker.utils.Dockerfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.val;

/**
 * Enforce that the Dockerfile has a specific image in the FROM line
 *
 * @author Brian Wyka
 */
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = DockerfileFromImageEquals.Builder.class)
@AllArgsConstructor(staticName = "equals")
public class DockerfileFromImageEquals extends AbstractDockerfileFromTokenEnforcer {

    private static final String INCORRECT_FROM_MESSAGE = "Dockerfile FROM image [%s] does not equal [%s]";

    /**
     * The expected image that should be included in the FROM line
     */
    private final String expectedFromImage;

    /**
     * Enforce the FROM image is as expected
     *
     * @param fromToken the FROM token
     * @return the enforcer result
     */
    @Override
    protected EnforcerResult enforceFromToken(final Dockerfile.FromToken fromToken) {
        if (fromToken.getImage().equals(expectedFromImage)) {
            return EnforcerResult.passed();
        }
        return EnforcerResult.failed(String.format(INCORRECT_FROM_MESSAGE, fromToken.getImage(), expectedFromImage));
    }

}
